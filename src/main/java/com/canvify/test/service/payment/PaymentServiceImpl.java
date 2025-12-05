package com.canvify.test.service.payment;

import com.canvify.test.dto.payment.PaymentResponseDTO;
import com.canvify.test.dto.payment.ProviderWebhookDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.integration.PaymentProviderClient;
import com.canvify.test.repository.OrderRepository;
import com.canvify.test.repository.PaymentRepository;
import com.canvify.test.repository.RefundRepository;
import com.canvify.test.request.payment.CreatePaymentRequest;
import com.canvify.test.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final org.springframework.context.ApplicationContext ctx; // to get provider bean by name

    @Override
    @Transactional
    public ApiResponse<?> createPayment(CreatePaymentRequest req) {
        Orders order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // optionally validate payable amount matches
        if (req.getAmount() == null || req.getAmount().compareTo(order.getPayableAmount()) != 0) {
            // you can allow mismatch or reject — here we ensure equality
            return ApiResponse.error("Amount mismatch");
        }

        // create a Payment row with status PENDING
        Payment p = new Payment();
        p.setOrder(order);
        p.setPaymentProvider(req.getProvider());
        p.setPaymentMethod(req.getMethod());
        p.setAmount(req.getAmount());
        p.setStatus(PaymentStatus.PENDING);
        p = paymentRepository.save(p);

        // call provider adapter
        String providerBeanName = req.getProvider().name(); // e.g. RAZORPAY -> bean named "RAZORPAY"
        PaymentProviderClient providerClient = ctx.getBean(providerBeanName, PaymentProviderClient.class);

        String providerOrderId = "ORD-" + UUID.randomUUID(); // receipt / id for provider
        Map<String, Object> res = providerClient.createPaymentOrder(providerOrderId, req.getAmount(), req.getCurrency(), providerOrderId);

        // save provider reference id
        String providerOrderRef = (String) res.get("orderId");
        p.setPaymentReferenceId(providerOrderRef);
        paymentRepository.save(p);

        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(p.getId());
        dto.setPaymentReferenceId(providerOrderRef);
        dto.setProviderClientToken((String) res.get("clientSecret"));

        return ApiResponse.success(dto, "Payment created");
    }

    @Override
    @Transactional
    public ApiResponse<?> handleProviderWebhook(ProviderWebhookDTO webhook, String signatureHeader, String rawPayload) {
        // get provider client based on webhook payload or configured provider
        // For simplicity assume the payload contains provider name in webhook.event or route uses provider-specific webhook endpoint
        // Stub: pick RAZORPAY
        PaymentProviderClient client = ctx.getBean("RAZORPAY", PaymentProviderClient.class);

        // verify signature
        boolean valid = client.verifyWebhookSignature(rawPayload, signatureHeader);
        if (!valid) return ApiResponse.error("Invalid webhook signature");

        // parse the event — this is simplified
        Map<String,Object> data = webhook.getData();
        String event = webhook.getEvent();

        // Example: payment captured event with payment id
        if ("payment.captured".equalsIgnoreCase(event) || "payment.success".equalsIgnoreCase(event)) {
            String providerPaymentRef = (String) data.get("payment_id"); // depends on provider payload
            // find Payment
            Payment payment = paymentRepository.findByPaymentReferenceIdAndBitDeletedFlagFalse(providerPaymentRef)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // update payment
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(java.time.LocalDateTime.now());
            paymentRepository.save(payment);

            // update order payment status
            Orders order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            // optionally move order status forward
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);

            // add order status history (call OrderStatusHistoryService or repository)
            // omitted here for brevity

            return ApiResponse.success("Payment processed");
        }

        if ("payment.failed".equalsIgnoreCase(event)) {
            String providerPaymentRef = (String) data.get("payment_id");
            Payment payment = paymentRepository.findByPaymentReferenceIdAndBitDeletedFlagFalse(providerPaymentRef)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            Orders order = payment.getOrder();
            order.setPaymentStatus(PaymentStatus.FAILED);
            orderRepository.save(order);

            return ApiResponse.success("Payment failed processed");
        }

        return ApiResponse.success("Webhook ignored");
    }

    @Override
    public ApiResponse<?> getPaymentsForOrder(Long orderId) {
        var list = paymentRepository.findByOrderIdAndBitDeletedFlagFalse(orderId);
        return ApiResponse.success(list);
    }
}