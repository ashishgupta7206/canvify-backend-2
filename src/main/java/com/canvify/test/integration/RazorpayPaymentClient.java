package com.canvify.test.integration;

import com.razorpay.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component("RAZORPAY")
public class RazorpayPaymentClient implements PaymentProviderClient {

    private final RazorpayClient razorpay;
    private final String razorpayKeyId;
    private final String razorpayKeySecret; // ✅ store secret

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public RazorpayPaymentClient(
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret
    ) throws RazorpayException {
        this.razorpay = new RazorpayClient(keyId, keySecret);
        this.razorpayKeyId = keyId;
        this.razorpayKeySecret = keySecret; // ✅ FIX
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
            response.put("key", razorpayKeyId);
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

    // -------------------------------------------------
    // VERIFY PAYMENT SIGNATURE (Frontend handler)
    // -------------------------------------------------
    @Override
    public boolean verifyPaymentSignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );

            sha256_HMAC.init(secretKey);

            byte[] hash = sha256_HMAC.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            String generatedSignature = bytesToHex(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
