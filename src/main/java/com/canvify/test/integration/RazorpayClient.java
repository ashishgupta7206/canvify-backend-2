package com.canvify.test.integration;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Component("RAZORPAY")
public class RazorpayClient implements PaymentProviderClient {

    @Override
    public Map<String, Object> createPaymentOrder(
            String providerOrderId,
            BigDecimal amount,
            String currency,
            String receipt
    ) {
        // TODO: Replace with Razorpay SDK
        Map<String, Object> m = new HashMap<>();
        m.put("orderId", "rzp_order_" + providerOrderId); // razorpay order_id
        m.put("clientSecret", "rzp_client_secret_stub");
        return m;
    }

    @Override
    public boolean verifyWebhookSignature(String payload, String signatureHeader) {
        // TODO: Razorpay signature verification
        return true;
    }

    @Override
    public Map<String, Object> refundPayment(
            String paymentReferenceId,
            BigDecimal amount,
            String reason
    ) {
        Map<String, Object> r = new HashMap<>();
        r.put("refundId", "refund_" + paymentReferenceId);
        r.put("status", "SUCCESS");
        return r;
    }
}
