package com.canvify.test.controller.refund;

import com.canvify.test.request.refund.RefundRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.refund.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createRefund(@RequestBody RefundRequest req) {
        return ResponseEntity.ok(refundService.initiateRefund(req));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<?>> getForPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(refundService.getRefundsForPayment(paymentId));
    }
}