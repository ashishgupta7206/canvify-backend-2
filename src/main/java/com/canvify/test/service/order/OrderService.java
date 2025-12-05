package com.canvify.test.service.order;

import com.canvify.test.dto.order.OrderDTO;
import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;

public interface OrderService {

    ApiResponse<?> placeOrder(CreateOrderRequest req, CustomUserDetails user);

    ApiResponse<?> getOrder(Long orderId, CustomUserDetails user);

    ApiResponse<?> getMyOrders(CustomUserDetails user);

    ApiResponse<?> cancelOrder(Long orderId, CustomUserDetails user);
}