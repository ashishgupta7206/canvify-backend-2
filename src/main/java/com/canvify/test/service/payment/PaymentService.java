package com.canvify.test.service.payment;

import com.canvify.test.dto.payment.PaymentResponseDTO;
import com.canvify.test.dto.payment.ProviderWebhookDTO;
import com.canvify.test.request.payment.CreatePaymentRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.payment.VerifyPaymentRequest;

public interface PaymentService {
    ApiResponse<?> createPayment(CreatePaymentRequest req);
    ApiResponse<?> handleProviderWebhook(ProviderWebhookDTO webhook, String signatureHeader, String rawPayload);
    ApiResponse<?> getPaymentsForOrder(Long orderId);
    ApiResponse<?> verifyPayment(VerifyPaymentRequest req);

}