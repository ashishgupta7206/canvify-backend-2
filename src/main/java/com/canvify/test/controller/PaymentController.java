package com.canvify.test.controller;

import com.canvify.test.dto.payment.ProviderWebhookDTO;
import com.canvify.test.request.payment.CreatePaymentRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
            @RequestBody ProviderWebhookDTO webhook,
            HttpServletRequest request
    ) throws Exception {

        String signature =
                request.getHeader("X-Razorpay-Signature");

        String rawPayload = new BufferedReader(request.getReader())
                .lines()
                .collect(Collectors.joining(System.lineSeparator()));

        paymentService.handleProviderWebhook(
                webhook,
                signature,
                rawPayload
        );

        // Razorpay expects plain 200 OK
        return ResponseEntity.ok("OK");
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
}
