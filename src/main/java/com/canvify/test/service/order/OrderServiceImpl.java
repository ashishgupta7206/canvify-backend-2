package com.canvify.test.service.order;

import com.canvify.test.dto.order.OrderDTO;
import com.canvify.test.dto.order.OrderItemDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.DiscountType;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.exception.BadRequestException;
import com.canvify.test.exception.NotFoundException;
import com.canvify.test.repository.*;
import com.canvify.test.request.auth.LoginRequest;
import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.request.order.OrderItemRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.PasswordGenerator;
import com.canvify.test.security.UserContext;
import com.canvify.test.service.impl.AuthServiceImpl;
import com.canvify.test.service.inventory.InventoryService;
import com.canvify.test.service.orderstatus.OrderStatusManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CouponRepository couponRepository;
    private final InventoryService inventoryService;
    private final CouponUsageRepository couponUsageRepository;
    private final UserContext userContext;
    private final OrderStatusManager orderStatusManager;
    private final RoleRepository roleRepository;
    private final AuthServiceImpl authServiceImpl;

    @Override
    @Transactional
    public ApiResponse<?> placeOrder(CreateOrderRequest req) {

        // ------------------------------------------------
        // BASIC VALIDATIONS
        // ------------------------------------------------
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new NotFoundException("Order must contain at least one item");
        }

        // ------------------------------------------------
        // RESOLVE USER (LOGGED-IN OR GUEST)
        // ------------------------------------------------
        User user;
        CustomUserDetails currentUser = userContext.getCurrentUser();
        String token = "";

        if (currentUser != null) {

            user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Invalid user"));

        } else {
            // ---------- GUEST FLOW ----------
            if (req.getAddress() == null || (req.getAddress().getMobile() == null || req.getAddress().getMobile().isBlank())) {
                throw new NotFoundException("Mobile number is required for guest checkout");
            }

            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new NotFoundException("ROLE_USER not configured"));

            String password = PasswordGenerator.generate(12);
            String userName = "GUEST" + authServiceImpl.generateNextUsername();
            user = userRepository.findByMobileNumber(req.getAddress().getMobile())
                    .orElseGet(() -> {
                        User guest = new User();
                        guest.setMobileNumber(req.getAddress().getMobile());
                        guest.setName(req.getAddress().getFullName());
                        guest.setEmail(req.getEmailId());
                        guest.setRole(role);
                        guest.setUsername(userName);
                        guest.setPassword(password);
                        return userRepository.save(guest);
                    });

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setIdentifier(req.getAddress().getMobile());
            loginRequest.setPassword(password);
            token = authServiceImpl.loginForregister(loginRequest).getAccessToken();

        }
        currentUser = userContext.getCurrentUser();


        // ------------------------------------------------
        // RESOLVE ADDRESS (ID OR CREATE)
        // ------------------------------------------------
        Address address;

        if (req.getAddressId() != null) {

            // Fetch existing address
            address = addressRepository.findById(req.getAddressId())
                    .orElseThrow(() -> new RuntimeException("Invalid address"));

            // Ownership check
            if (!address.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Address does not belong to user");
            }

            // 🔹 If address object is also present → UPDATE
            if (req.getAddress() != null) {
                address.setFullName(req.getAddress().getFullName());
                address.setMobile(req.getAddress().getMobile());
                address.setPincode(req.getAddress().getPincode());
                address.setCity(req.getAddress().getCity());
                address.setState(req.getAddress().getState());
                address.setAddressLine1(req.getAddress().getAddressLine1());
                address.setAddressLine2(req.getAddress().getAddressLine2());
                address.setLandmark(req.getAddress().getLandmark());
                address.setAddressType(req.getAddress().getAddressType());

                address = addressRepository.save(address);
            }

        } else if (req.getAddress() != null) {

            // 🔹 Create new address
            Address newAddress = new Address();
            newAddress.setUser(user);
            newAddress.setFullName(req.getAddress().getFullName());
            newAddress.setMobile(req.getAddress().getMobile());
            newAddress.setPincode(req.getAddress().getPincode());
            newAddress.setCity(req.getAddress().getCity());
            newAddress.setState(req.getAddress().getState());
            newAddress.setAddressLine1(req.getAddress().getAddressLine1());
            newAddress.setAddressLine2(req.getAddress().getAddressLine2());
            newAddress.setLandmark(req.getAddress().getLandmark());
            newAddress.setAddressType(req.getAddress().getAddressType());

            address = addressRepository.save(newAddress);

        } else {
            throw new RuntimeException("Address or AddressId is required");
        }


        // ------------------------------------------------
        // CREATE ORDER (INITIAL)
        // ------------------------------------------------
        Orders order;

        try {
            order = new Orders();
            order.setUser(user);
            order.setAddress(address);
            order.setOrderDate(LocalDateTime.now());
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setStatus(OrderStatus.PLACED);
            // order.setIsGuestOrder(user.getRole() == Role.GUEST);
            // order.setGuestMobile(user.getMobileNumber());

            order = orderRepository.save(order);

        } catch (Exception ex) {

            // 🔴 LOG FULL ERROR (very important)
            log.error("Failed to create order for userId={}",
                    user != null ? user.getId() : null, ex);

            // ❌ Convert to business-safe exception
            throw new RuntimeException("Failed to create order. Please try again.");
        }

        // ------------------------------------------------
        // RESERVE STOCK + CALCULATE TOTAL
        // ------------------------------------------------
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest i : req.getItems()) {

            ProductVariant variant = productVariantRepository.findById(i.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Invalid product variant"));

            inventoryService.reserveStock(
                    variant.getId(),
                    i.getQuantity(),
                    order.getId()
            );

            BigDecimal price = variant.getPrice();
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(i.getQuantity()));

            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductVariant(variant);
            item.setQuantity(i.getQuantity());
            item.setPriceAtTime(price);
            item.setTotalPrice(itemTotal);

            orderItems.add(item);
        }

        orderItemRepository.saveAll(orderItems);

        // ------------------------------------------------
        // COUPON VALIDATION (LOGGED-IN USERS ONLY)
        // ------------------------------------------------
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;

        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {

            if (Objects.equals(user.getRole().getName(), "ROLE_GUEST")) {
                throw new RuntimeException("Please login to apply coupon");
            }

            Coupon coupon = couponRepository
                    .findByCodeAndActiveFlagTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                            req.getCouponCode(),
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
                    .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));

            if (coupon.getUsageLimit() != null) {
                long used = couponUsageRepository
                        .countByCouponIdAndBitDeletedFlagFalse(coupon.getId());
                if (used >= coupon.getUsageLimit()) {
                    throw new RuntimeException("Coupon usage limit reached");
                }
            }

            if (coupon.getPerUserLimit() != null) {
                long userUsed = couponUsageRepository
                        .countByCouponIdAndUserIdAndBitDeletedFlagFalse(
                                coupon.getId(), user.getId());
                if (userUsed >= coupon.getPerUserLimit()) {
                    throw new RuntimeException("Coupon already used");
                }
            }

            if (coupon.getMinOrderValue() != null &&
                    totalAmount.compareTo(coupon.getMinOrderValue()) < 0) {
                throw new RuntimeException("Cart amount is less than minimum order value");
            }

            if (coupon.getDiscountType() == DiscountType.FLAT) {
                discountAmount = coupon.getDiscountValue();
            } else {
                discountAmount = totalAmount
                        .multiply(coupon.getDiscountValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }

            if (coupon.getMaxDiscount() != null &&
                    discountAmount.compareTo(coupon.getMaxDiscount()) > 0) {
                discountAmount = coupon.getMaxDiscount();
            }

            appliedCoupon = coupon;
        }

        // ------------------------------------------------
        // DELIVERY CHARGE + FINAL AMOUNT
        // ------------------------------------------------
        BigDecimal deliveryCharge = calculateDeliveryCharge(totalAmount);

        BigDecimal payableAmount = totalAmount
                .subtract(discountAmount)
                .add(deliveryCharge);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setDeliveryCharge(deliveryCharge);
        order.setPayableAmount(payableAmount);

        order = orderRepository.save(order);

        // ------------------------------------------------
        // SAVE COUPON USAGE
        // ------------------------------------------------
        if (appliedCoupon != null) {
            CouponUsage usage = new CouponUsage();
            usage.setCoupon(appliedCoupon);
            usage.setUser(user);
            usage.setOrder(order);
            usage.setUsedOn(LocalDateTime.now());
            couponUsageRepository.save(usage);
        }

        return ApiResponse.success(convertToDTO(order,token), "Order placed successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> previewOrder(CreateOrderRequest req) {

        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new NotFoundException("Cart is empty");
        }

        CustomUserDetails currentUser = userContext.getCurrentUser();
        Long userId = userContext.getUserId();


        // ---------------------------------------
        // ADDRESS (ONLY IF LOGGED IN)
        // ---------------------------------------
        Address address = null;

        if (currentUser != null && req.getAddressId() != null) {
            address = addressRepository.findByIdAndBitDeletedFlagFalse(req.getAddressId())
                    .orElseThrow(() -> new NotFoundException("Invalid address"));

            if (!address.getUser().getId().equals(currentUser.getId())) {
                throw new BadRequestException("Address does not belong to user");
            }
        }
        if (currentUser != null && req.getAddressId() == null) {

            List<Address> addressList = addressRepository.findByUserIdAndBitDeletedFlagFalse(userId);
            if(!addressList.isEmpty()){
                address = addressList.get(0);
            }
        }

        // ---------------------------------------
        // ITEMS + TOTAL
        // ---------------------------------------
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Map<String, Object>> itemList = new ArrayList<>();

        for (OrderItemRequest i : req.getItems()) {

            ProductVariant variant = productVariantRepository.findById(i.getProductVariantId())
                    .orElseThrow(() -> new NotFoundException("Invalid product variant"));

            BigDecimal price = variant.getPrice();
            BigDecimal itemTotal = price.multiply(BigDecimal.valueOf(i.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            Map<String, Object> item = new HashMap<>();
            item.put("variantId", variant.getId());
            item.put("productName", variant.getProduct().getName());
            item.put("variantLabel", variant.getSize());
            item.put("quantity", i.getQuantity());
            item.put("price", price);
            item.put("total", itemTotal);

            itemList.add(item);
        }

        // ---------------------------------------
        // DELIVERY CHARGE
        // ---------------------------------------
        BigDecimal deliveryCharge = calculateDeliveryCharge(totalAmount);

        // ---------------------------------------
        // FINAL PAYABLE
        // ---------------------------------------
        BigDecimal payableAmount = totalAmount.add(deliveryCharge);

        // ---------------------------------------
        // RESPONSE
        // ---------------------------------------
        Map<String, Object> response = new HashMap<>();

        if (address != null) {
            Map<String, Object> addr = new HashMap<>();
            addr.put("id", address.getId());
            addr.put("fullName", address.getFullName());
            addr.put("mobile", address.getMobile());
            addr.put("addressLine1", address.getAddressLine1());
            addr.put("city", address.getCity());
            addr.put("state", address.getState());
            addr.put("pincode", address.getPincode());
            response.put("address", addr);
        }

        response.put("items", itemList);
        response.put("totalAmount", totalAmount);
        response.put("deliveryCharge", deliveryCharge);
        response.put("discountAmount", BigDecimal.ZERO);
        response.put("payableAmount", payableAmount);

        return ApiResponse.success(response, "Order preview generated");
    }



    @Override
    public ApiResponse<?> getOrder(Long orderId) {
        CustomUserDetails user = userContext.getCurrentUser();
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("Unauthorized");
        }

        return ApiResponse.success(convertToDTO(order));
    }

    @Override
    public ApiResponse<?> getMyOrders() {
        CustomUserDetails user = userContext.getCurrentUser();
        List<Orders> orders = orderRepository.findByUserIdAndBitDeletedFlagFalse(user.getId());
        List<OrderDTO> list = orders.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    @Override
    @Transactional
    public ApiResponse<?> cancelOrder(Long orderId) {

        CustomUserDetails currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("Unauthorized");
        }

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            return ApiResponse.error("Unauthorized");
        }

        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            return ApiResponse.error("Order cannot be cancelled");
        }

        // -----------------------
        // RELEASE STOCK
        // -----------------------
        List<OrderItem> items =
                orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(orderId);

        for (OrderItem item : items) {
            inventoryService.releaseReservedStock(
                    item.getProductVariant().getId(),
                    item.getQuantity(),
                    order.getId()
            );
        }

        // -----------------------
        // ROLLBACK COUPON USAGE
        // -----------------------
        Optional<CouponUsage> usageOpt =
                couponUsageRepository.findByOrderIdAndBitDeletedFlagFalse(orderId);

        usageOpt.ifPresent(usage -> {
            usage.setBitDeletedFlag(true);
            couponUsageRepository.save(usage);
        });

        order = orderRepository.save(order);

        orderStatusManager.changeStatus(
                order,
                OrderStatus.PLACED,
                "Order created"
        );


        return ApiResponse.success(null, "Order cancelled successfully");
    }


    private OrderDTO convertToDTO(Orders order) {

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setPayableAmount(order.getPayableAmount());

        dto.setAddressSummary(order.getAddress().getFullName() + ", " + order.getAddress().getAddressLine1());

        List<OrderItem> items = orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

        dto.setItems(
                items.stream().map(i -> {
                    OrderItemDTO d = new OrderItemDTO();
                    d.setId(i.getId());
                    d.setVariantId(i.getProductVariant().getId());
                    d.setProductName(i.getProductVariant().getProduct().getName());
                    d.setVariantLabel(i.getProductVariant().getSize());
                    d.setPriceAtTime(i.getPriceAtTime().doubleValue());
                    d.setTotalPrice(i.getTotalPrice().doubleValue());
                    d.setQuantity(i.getQuantity());
                    return d;
                }).collect(Collectors.toList())
        );

        return dto;
    }

    private OrderDTO convertToDTO(Orders order,String accessToken) {

        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setPayableAmount(order.getPayableAmount());
        dto.setAccessToken(accessToken);

        dto.setAddressSummary(order.getAddress().getFullName() + ", " + order.getAddress().getAddressLine1());

        List<OrderItem> items = orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

        dto.setItems(
                items.stream().map(i -> {
                    OrderItemDTO d = new OrderItemDTO();
                    d.setId(i.getId());
                    d.setVariantId(i.getProductVariant().getId());
                    d.setProductName(i.getProductVariant().getProduct().getName());
                    d.setVariantLabel(i.getProductVariant().getSize());
                    d.setPriceAtTime(i.getPriceAtTime().doubleValue());
                    d.setTotalPrice(i.getTotalPrice().doubleValue());
                    d.setQuantity(i.getQuantity());
                    return d;
                }).collect(Collectors.toList())
        );

        return dto;
    }

    public BigDecimal calculateDeliveryCharge(BigDecimal orderValue) {

        if (orderValue.compareTo(BigDecimal.valueOf(999)) >= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(40);
    }

}