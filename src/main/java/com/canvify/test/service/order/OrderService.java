package com.canvify.test.service.order;

import com.canvify.test.dto.order.OrderDTO;
import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;

public interface OrderService {

    ApiResponse<?> placeOrder(CreateOrderRequest req);

    ApiResponse<?> getOrder(Long orderId);

    ApiResponse<?> getMyOrders();

    ApiResponse<?> cancelOrder(Long orderId);

    ApiResponse<?> previewOrder(CreateOrderRequest req);
}