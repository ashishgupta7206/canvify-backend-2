package com.canvify.test.controller.order;

import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createOrder(
            @RequestBody CreateOrderRequest req,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(orderService.placeOrder(req, user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> getOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(orderService.getOrder(id, user));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> myOrders(
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(orderService.getMyOrders(user));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<?>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user
    ) {
        return ResponseEntity.ok(orderService.cancelOrder(id, user));
    }
}