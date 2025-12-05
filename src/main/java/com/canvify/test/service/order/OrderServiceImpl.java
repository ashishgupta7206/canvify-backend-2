package com.canvify.test.service.order;

import com.canvify.test.dto.order.OrderDTO;
import com.canvify.test.dto.order.OrderItemDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.repository.*;
import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.request.order.OrderItemRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Override
    @Transactional
    public ApiResponse<?> placeOrder(CreateOrderRequest req, CustomUserDetails currentUser) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Invalid user"));

        Address address = addressRepository.findById(req.getAddressId())
                .orElseThrow(() -> new RuntimeException("Invalid address"));

        Orders order = new Orders();
        order.setUser(user);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;

        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest i : req.getItems()) {

            ProductVariant variant = productVariantRepository.findById(i.getProductVariantId())
                    .orElseThrow(() -> new RuntimeException("Invalid product variant"));

            // STOCK check
            if (variant.getStockQty() < i.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + variant.getSku());
            }

            // Deduct stock
            variant.setStockQty(variant.getStockQty() - i.getQuantity());
            productVariantRepository.save(variant);

            BigDecimal price = variant.getPrice();
            BigDecimal total = price.multiply(BigDecimal.valueOf(i.getQuantity()));

            totalAmount = totalAmount.add(total);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductVariant(variant);
            orderItem.setQuantity(i.getQuantity());
            orderItem.setPriceAtTime(price);
            orderItem.setTotalPrice(total);

            orderItems.add(orderItem);
        }

        orderItemRepository.saveAll(orderItems);

        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayableAmount(totalAmount);

        order = orderRepository.save(order);

        return ApiResponse.success(convertToDTO(order), "Order placed successfully");
    }

    @Override
    public ApiResponse<?> getOrder(Long orderId, CustomUserDetails user) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("Unauthorized");
        }

        return ApiResponse.success(convertToDTO(order));
    }

    @Override
    public ApiResponse<?> getMyOrders(CustomUserDetails user) {
        List<Orders> orders = orderRepository.findByUserIdAndBitDeletedFlagFalse(user.getId());
        List<OrderDTO> list = orders.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ApiResponse.success(list);
    }

    @Override
    @Transactional
    public ApiResponse<?> cancelOrder(Long orderId, CustomUserDetails user) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            return ApiResponse.error("Unauthorized");
        }

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            return ApiResponse.error("Order cannot be cancelled at this stage");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

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