package com.canvify.test.controller.payment;

import com.canvify.test.dto.payment.ProviderWebhookDTO;
import com.canvify.test.request.payment.CreatePaymentRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createPayment(@RequestBody CreatePaymentRequest req) {
        return ResponseEntity.ok(paymentService.createPayment(req));
    }

    @PostMapping("/webhook/razorpay")
    public ResponseEntity<String> razorpayWebhook(@RequestBody ProviderWebhookDTO webhook, HttpServletRequest request) throws Exception {
        String signature = request.getHeader("X-Razorpay-Signature");
        String payload = new BufferedReader(request.getReader()).lines().collect(Collectors.joining(System.lineSeparator()));
        paymentService.handleProviderWebhook(webhook, signature, payload);
        // provider expects 200 OK plain body sometimes
        return ResponseEntity.ok("ok");
    }

    @GetMapping("/order/{id}")
    public ResponseEntity<ApiResponse<?>> getByOrder(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentsForOrder(id));
    }
}