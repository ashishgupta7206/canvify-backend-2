package com.canvify.test.controller;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.orderstatus.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/history")
@RequiredArgsConstructor
public class OrderStatusHistoryController {

    private final OrderStatusHistoryService historyService;

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getHistory(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        historyService.getHistoryForOrder(orderId)
                )
        );
    }
}
