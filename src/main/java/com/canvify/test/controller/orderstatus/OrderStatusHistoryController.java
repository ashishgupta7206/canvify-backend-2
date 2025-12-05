package com.canvify.test.controller.orderstatus;

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

    @PostMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> addHistory(@PathVariable Long orderId,
                                                     @RequestParam String oldStatus,
                                                     @RequestParam String newStatus,
                                                     @RequestParam(required = false) String updatedBy,
                                                     @RequestParam(required = false) String remark) {
        return ResponseEntity.ok(historyService.addHistory(orderId, oldStatus, newStatus, updatedBy, remark));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<?>> getHistory(@PathVariable Long orderId) {
        return ResponseEntity.ok(historyService.getHistoryForOrder(orderId));
    }
}