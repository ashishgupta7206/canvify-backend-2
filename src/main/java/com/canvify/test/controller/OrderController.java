package com.canvify.test.controller;

import com.canvify.test.request.order.CreateOrderRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createOrder(
            @RequestBody CreateOrderRequest req
    ) {
        return ResponseEntity.ok(orderService.placeOrder(req));
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<?>> getOrder(
//            @PathVariable Long id
//    ) {
//        return ResponseEntity.ok(orderService.getOrder(id));
//    }
//
//    @GetMapping("/my")
//    public ResponseEntity<ApiResponse<?>> myOrders() {
//        return ResponseEntity.ok(orderService.getMyOrders());
//    }
//
//    @PutMapping("/{id}/cancel")
//    public ResponseEntity<ApiResponse<?>> cancelOrder(
//            @PathVariable Long id
//    ) {
//        return ResponseEntity.ok(orderService.cancelOrder(id));
//    }

    @PostMapping("/preview")
    public ResponseEntity<ApiResponse<?>> previewOrder(
            @RequestBody CreateOrderRequest req
    ) {
        return ResponseEntity.ok(orderService.previewOrder(req));
    }

}