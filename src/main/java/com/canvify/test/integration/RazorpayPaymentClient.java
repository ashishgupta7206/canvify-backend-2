package com.canvify.test.integration;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component("RAZORPAY")
public class RazorpayPaymentClient implements PaymentProviderClient {

    private final RazorpayClient razorpay;
    private final String razorpayKeyId;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public RazorpayPaymentClient(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret
    ) throws RazorpayException {
        this.razorpay = new RazorpayClient(keyId, keySecret);
        this.razorpayKeyId = keyId; // ✅ store public key
    }

    // -------------------------------------------------
    // CREATE PAYMENT ORDER
    // -------------------------------------------------
    @Override
    public Map<String, Object> createPaymentOrder(
            String providerOrderId,
            BigDecimal amount,
            String currency,
            String receipt
    ) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", receipt);
            orderRequest.put("payment_capture", 1);

            Order order = razorpay.orders.create(orderRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.get("id"));
            response.put("amount", order.get("amount"));
            response.put("currency", order.get("currency"));
            response.put("key", razorpayKeyId); // ✅ FIXED
            response.put("status", order.get("status"));
            response.put("receipt", order.get("receipt"));

            return response;

        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create Razorpay order", e);
        }
    }

    // -------------------------------------------------
    // VERIFY WEBHOOK SIGNATURE
    // -------------------------------------------------
    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        try {
            return Utils.verifyWebhookSignature(payload, signatureHeader, webhookSecret);
        } catch (RazorpayException e) {
            return false;
        }
    }

    // -------------------------------------------------
    // REFUND PAYMENT
    // -------------------------------------------------
    @Override
    public Map<String, Object> refundPayment(
            String paymentReferenceId,
            BigDecimal amount,
            String reason
    ) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).longValue());
            refundRequest.put("notes", new JSONObject().put("reason", reason));

            Refund refund = razorpay.payments.refund(paymentReferenceId, refundRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("refundId", refund.get("id"));
            response.put("status", refund.get("status"));
            response.put("amount", refund.get("amount"));
            response.put("currency", refund.get("currency"));

            return response;

        } catch (RazorpayException e) {
            throw new RuntimeException("Refund failed", e);
        }
    }
}
