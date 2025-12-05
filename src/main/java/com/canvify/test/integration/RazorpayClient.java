package com.canvify.test.integration;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Component("RAZORPAY")
public class RazorpayClient implements PaymentProviderClient {

    @Override
    public Map<String, Object> createPaymentOrder(String providerOrderId, BigDecimal amount, String currency, String receipt) {
        // Stub — replace with actual Razorpay SDK call to create order.
        Map<String,Object> m = new HashMap<>();
        m.put("orderId", "order_" + providerOrderId);
        m.put("clientSecret", "client_token_stub");
        return m;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        // Stub — verify signature using Razorpay secret
        return true;
    }

    @Override
    public Map<String, Object> refundPayment(String paymentReferenceId, BigDecimal amount, String reason) {
        // Stub — call Razorpay refund API
        Map<String,Object> r = new HashMap<>();
        r.put("refundId", "refund_" + paymentReferenceId);
        r.put("status", "SUCCESS");
        return r;
    }
}