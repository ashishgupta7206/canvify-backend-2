package com.canvify.test.service.payment;

import com.canvify.test.dto.payment.PaymentResponseDTO;
import com.canvify.test.dto.payment.ProviderWebhookDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.integration.PaymentProviderClient;
import com.canvify.test.repository.*;
import com.canvify.test.request.payment.CreatePaymentRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.inventory.InventoryService;
import com.canvify.test.service.orderstatus.OrderStatusManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final InventoryService inventoryService;
    private final OrderStatusManager orderStatusManager;
    private final OrderItemRepository orderItemRepository;
    private final org.springframework.context.ApplicationContext ctx;

    // -------------------------------------------------
    // CREATE PAYMENT (CLIENT SIDE INITIATION)
    // -------------------------------------------------
    @Override
    @Transactional
    public ApiResponse<?> createPayment(CreatePaymentRequest req) {

        Orders order = orderRepository.findByIdAndBitDeletedFlagFalse(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PLACED) {
            return ApiResponse.error("Order not eligible for payment");
        }

        if (req.getAmount().compareTo(order.getPayableAmount()) != 0) {
            return ApiResponse.error("Amount mismatch");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentProvider(req.getProvider());
        payment.setPaymentMethod(req.getMethod());
        payment.setAmount(req.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);

        PaymentProviderClient provider =
                ctx.getBean(req.getProvider().name(), PaymentProviderClient.class);

        String providerOrderId = "ORD-" + UUID.randomUUID();

        Map<String, Object> res = provider.createPaymentOrder(
                providerOrderId,
                req.getAmount(),
                req.getCurrency(),
                providerOrderId
        );

        payment.setProviderOrderId((String) res.get("orderId"));
        paymentRepository.save(payment);

        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(payment.getId());
        dto.setPaymentReferenceId(payment.getProviderOrderId());
        dto.setProviderClientToken((String) res.get("clientSecret"));

        return ApiResponse.success(dto, "Payment initiated");
    }

    // -------------------------------------------------
    // RAZORPAY WEBHOOK
    // -------------------------------------------------
    @Override
    @Transactional
    public ApiResponse<?> handleProviderWebhook(
            ProviderWebhookDTO webhook,
            String signatureHeader,
            String rawPayload
    ) {

        PaymentProviderClient client =
                ctx.getBean("RAZORPAY", PaymentProviderClient.class);

        if (!client.verifyWebhookSignature(rawPayload, signatureHeader)) {
            return ApiResponse.error("Invalid webhook signature");
        }

        String event = webhook.getEvent();
        Map<String, Object> data = webhook.getData();

        String providerPaymentId = (String) data.get("payment_id");

        Payment payment = paymentRepository
                .findByProviderPaymentIdAndBitDeletedFlagFalse(providerPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return ApiResponse.success("Already processed");
        }

        payment.setProviderPaymentId(providerPaymentId);

        Orders order = payment.getOrder();

        // ========================
        // PAYMENT SUCCESS
        // ========================
        if ("payment.captured".equalsIgnoreCase(event)
                || "payment.success".equalsIgnoreCase(event)) {

            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);

            // 🔁 INVENTORY: RESERVE → SALE
            List<OrderItem> items =
                    orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

            for (OrderItem item : items) {
                inventoryService.reduceStock(
                        item.getProductVariant().getId(),
                        item.getQuantity(),
                        "Order paid",
                        order.getId()
                );
            }

            // 🔁 ORDER STATUS (STATE MACHINE)
            orderStatusManager.changeStatus(
                    order,
                    OrderStatus.PAID,
                    "Payment successful"
            );

            return ApiResponse.success("Payment processed");
        }

        // ========================
        // PAYMENT FAILED
        // ========================
        if ("payment.failed".equalsIgnoreCase(event)) {

            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // 🔁 RELEASE RESERVED STOCK
            List<OrderItem> items =
                    orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

            for (OrderItem item : items) {
                inventoryService.releaseReservedStock(
                        item.getProductVariant().getId(),
                        item.getQuantity(),
                        order.getId()
                );
            }

            // 🔁 ROLLBACK COUPON
            couponUsageRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId())
                    .ifPresent(u -> {
                        u.setBitDeletedFlag(true);
                        couponUsageRepository.save(u);
                    });

            return ApiResponse.success("Payment failed handled");
        }

        return ApiResponse.success("Webhook ignored");
    }

    // -------------------------------------------------
    // LIST PAYMENTS FOR ORDER
    // -------------------------------------------------
    @Override
    public ApiResponse<?> getPaymentsForOrder(Long orderId) {
        return ApiResponse.success(
                paymentRepository.findByOrderIdAndBitDeletedFlagFalse(orderId)
        );
    }
}
