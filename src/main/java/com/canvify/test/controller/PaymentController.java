package com.canvify.test.controller;

import com.canvify.test.dto.payment.ProviderWebhookDTO;
import com.canvify.test.request.payment.CreatePaymentRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.payment.VerifyPaymentRequest;
import com.canvify.test.service.payment.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    // -------------------------------------------------
    // CREATE PAYMENT (CLIENT → SERVER)
    // -------------------------------------------------
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createPayment(
            @RequestBody CreatePaymentRequest req
    ) {
        return ResponseEntity.ok(paymentService.createPayment(req));
    }

    // -------------------------------------------------
    // RAZORPAY WEBHOOK
    // -------------------------------------------------
    @PostMapping("/webhook/razorpay")
    public ResponseEntity<String> razorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) throws Exception {

        ProviderWebhookDTO webhook = objectMapper.readValue(rawPayload, ProviderWebhookDTO.class);

        ApiResponse<?> response = paymentService.handleProviderWebhook(webhook, signature, rawPayload);

        return ResponseEntity.ok(response.getMessage());
    }


    // -------------------------------------------------
    // LIST PAYMENTS FOR ORDER
    // -------------------------------------------------
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<?>> getPaymentsForOrder(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                paymentService.getPaymentsForOrder(orderId)
        );
    }

    @PostMapping("/verify")
    public ApiResponse<?> verifyPayment(@RequestBody VerifyPaymentRequest req) {
        return paymentService.verifyPayment(req);
    }
}
