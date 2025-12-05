package com.canvify.test.integration;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentProviderClient {

    /**
     * Create payment/order in provider and return provider-specific response.
     * Should return map containing keys like "orderId" and "clientSecret" etc.
     */
    Map<String, Object> createPaymentOrder(String providerOrderId, BigDecimal amount, String currency, String receipt);

    /**
     * Verify provider webhook signature and payload. Return true if valid.
     */
    boolean verifyWebhookSignature(String payload, String signatureHeader);

    /**
     * Process refund request with provider, return provider refund id & status.
     */
    Map<String, Object> refundPayment(String paymentReferenceId, BigDecimal amount, String reason);
}