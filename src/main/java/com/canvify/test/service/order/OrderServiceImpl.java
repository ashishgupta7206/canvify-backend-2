package com.canvify.test.service.order;

import com.canvify.test.dto.order.OrderDTO;
import com.canvify.test.dto.order.OrderItemDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.DiscountType;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.repository.*;
import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.request.order.OrderItemRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.UserContext;
import com.canvify.test.service.inventory.InventoryService;
import com.canvify.test.service.orderstatus.OrderStatusManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.*;

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

    @Override
    @Transactional
    public ApiResponse<?> placeOrder(CreateOrderRequest req) {

        CustomUserDetails currentUser = userContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error("User not authenticated");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Invalid address"));

        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.PLACED);
        orderRepository.save(order);


        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // -----------------------
        // RESERVE STOCK
        // -----------------------
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

        // -----------------------
        // COUPON VALIDATION
        // -----------------------
        BigDecimal discountAmount = BigDecimal.ZERO;
        Coupon appliedCoupon = null;

        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {

            Coupon coupon = couponRepository
                    .findByCodeAndActiveFlagTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                            req.getCouponCode(),
                            LocalDateTime.now(),
                            LocalDateTime.now()
                    )
                    .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));

            // Total usage limit
            if (coupon.getUsageLimit() != null) {
                long used = couponUsageRepository
                        .countByCouponIdAndBitDeletedFlagFalse(coupon.getId());
                if (used >= coupon.getUsageLimit()) {
                    throw new RuntimeException("Coupon usage limit reached");
                }
            }

            // Per-user usage
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

        BigDecimal payableAmount = totalAmount.subtract(discountAmount);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayableAmount(payableAmount);

        order = orderRepository.save(order);

        // -----------------------
        // SAVE COUPON USAGE
        // -----------------------
        if (appliedCoupon != null) {
            CouponUsage usage = new CouponUsage();
            usage.setCoupon(appliedCoupon);
            usage.setUser(user);
            usage.setOrder(order);
            usage.setUsedOn(LocalDateTime.now());

            couponUsageRepository.save(usage);
        }

        return ApiResponse.success(convertToDTO(order), "Order placed successfully");
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
}