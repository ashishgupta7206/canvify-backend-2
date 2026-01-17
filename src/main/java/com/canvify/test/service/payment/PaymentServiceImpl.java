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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final InventoryService inventoryService;
    private final OrderStatusManager orderStatusManager;
    private final OrderItemRepository orderItemRepository;
    private final org.springframework.context.ApplicationContext ctx;
    private final ObjectMapper objectMapper;


    // -------------------------------------------------
    // CREATE PAYMENT (CLIENT SIDE INITIATION)
    // -------------------------------------------------
    @Override
    @Transactional
    public ApiResponse<?> createPayment(CreatePaymentRequest req) {

        log.info("==> [PAYMENT][CREATE] Request received | orderId={} | amount={} | currency={} | provider={} | method={}",
                req.getOrderId(), req.getAmount(), req.getCurrency(), req.getProvider(), req.getMethod());

        Orders order = orderRepository.findByIdAndBitDeletedFlagFalse(req.getOrderId())
                .orElseThrow(() -> {
                    log.error("[PAYMENT][CREATE] Order not found | orderId={}", req.getOrderId());
                    return new RuntimeException("Order not found");
                });

        log.debug("[PAYMENT][CREATE] Order fetched | orderId={} | status={} | payableAmount={}",
                order.getId(), order.getStatus(), order.getPayableAmount());

        if (order.getStatus() != OrderStatus.PLACED) {
            log.warn("[PAYMENT][CREATE] Order not eligible for payment | orderId={} | status={}",
                    order.getId(), order.getStatus());
            return ApiResponse.error("Order not eligible for payment");
        }

        if (req.getAmount().compareTo(order.getPayableAmount()) != 0) {
            log.warn("[PAYMENT][CREATE] Amount mismatch | orderId={} | reqAmount={} | payableAmount={}",
                    order.getId(), req.getAmount(), order.getPayableAmount());
            return ApiResponse.error("Amount mismatch");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentProvider(req.getProvider());
        payment.setPaymentMethod(req.getMethod());
        payment.setAmount(req.getAmount());
        payment.setStatus(PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);

        log.info("[PAYMENT][CREATE] Payment created in DB | paymentId={} | status={} | amount={}",
                payment.getId(), payment.getStatus(), payment.getAmount());

        PaymentProviderClient provider =
                ctx.getBean("RAZORPAY", PaymentProviderClient.class);

        String providerOrderId = "ORD-" + UUID.randomUUID();
        log.info("[PAYMENT][CREATE] Creating provider order | localProviderOrderId={} | amount={} | currency={}",
                providerOrderId, req.getAmount(), req.getCurrency());

        Map<String, Object> res = provider.createPaymentOrder(
                providerOrderId,
                req.getAmount(),
                req.getCurrency(),
                providerOrderId
        );

        log.debug("[PAYMENT][CREATE] Provider response received | response={}", res);

        payment.setProviderOrderId((String) res.get("orderId"));
        paymentRepository.save(payment);

        log.info("[PAYMENT][CREATE] Provider order mapped to payment | paymentId={} | providerOrderId={}",
                payment.getId(), payment.getProviderOrderId());

        PaymentResponseDTO dto = new PaymentResponseDTO();
        dto.setPaymentId(payment.getId());
        dto.setPaymentReferenceId(payment.getProviderOrderId());
        dto.setProviderKey((String) res.get("key"));
        dto.setAmount(req.getAmount());
        dto.setCurrency(req.getCurrency());

        log.info("<== [PAYMENT][CREATE] Payment initiated successfully | paymentId={} | providerOrderId={}",
                payment.getId(), payment.getProviderOrderId());

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

        log.info("==> [PAYMENT][WEBHOOK] Webhook received | signatureHeaderPresent={} | payloadLength={}",
                signatureHeader != null, rawPayload != null ? rawPayload.length() : 0);

        PaymentProviderClient client =
                ctx.getBean("RAZORPAY", PaymentProviderClient.class);

        boolean signatureOk = client.verifyWebhookSignature(rawPayload, signatureHeader);

        if (!signatureOk) {
            log.error("[PAYMENT][WEBHOOK] Invalid webhook signature | signatureHeader={}", signatureHeader);
            return ApiResponse.error("Invalid webhook signature");
        }

        log.info("[PAYMENT][WEBHOOK] Signature verified successfully");

        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            String event = root.path("event").asText();
            log.info("[PAYMENT][WEBHOOK] Event received | event={}", event);

            JsonNode paymentEntity = root.path("payload")
                    .path("payment")
                    .path("entity");

            String providerOrderId = paymentEntity.path("order_id").asText(null);
            String providerPaymentId = paymentEntity.path("id").asText(null);

            Long paidAmount = paymentEntity.path("amount").isMissingNode()
                    ? null
                    : paymentEntity.path("amount").asLong();

            log.debug("[PAYMENT][WEBHOOK] Parsed payment entity | providerOrderId={} | providerPaymentId={} | paidAmount={}",
                    providerOrderId, providerPaymentId, paidAmount);

            if (providerOrderId == null) {
                log.error("[PAYMENT][WEBHOOK] Missing provider order id in webhook payload");
                return ApiResponse.error("Missing provider order id in webhook");
            }

            Payment payment = paymentRepository
                    .findByProviderOrderIdAndBitDeletedFlagFalse(providerOrderId)
                    .orElseThrow(() -> {
                        log.error("[PAYMENT][WEBHOOK] Payment not found for providerOrderId={}", providerOrderId);
                        return new RuntimeException("Payment not found");
                    });

            log.info("[PAYMENT][WEBHOOK] Payment found | paymentId={} | status={} | providerOrderId={}",
                    payment.getId(), payment.getStatus(), providerOrderId);

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("[PAYMENT][WEBHOOK] Already processed payment | paymentId={} | providerOrderId={}",
                        payment.getId(), providerOrderId);
                return ApiResponse.success("Already processed");
            }

            payment.setProviderPaymentId(providerPaymentId);

            Orders order = payment.getOrder();
            log.debug("[PAYMENT][WEBHOOK] Related order fetched | orderId={} | orderStatus={}",
                    order.getId(), order.getStatus());

            // ========================
            // PAYMENT SUCCESS
            // ========================
            if ("payment.captured".equalsIgnoreCase(event)) {

                Long expectedAmount = payment.getAmount()
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();

                log.info("[PAYMENT][WEBHOOK][SUCCESS] Captured event | paymentId={} | expectedAmount={} | paidAmount={}",
                        payment.getId(), expectedAmount, paidAmount);

                if (paidAmount == null || !expectedAmount.equals(paidAmount)) {
                    log.error("[PAYMENT][WEBHOOK][SUCCESS] Amount mismatch | paymentId={} | expectedAmount={} | paidAmount={}",
                            payment.getId(), expectedAmount, paidAmount);
                    return ApiResponse.error("Payment amount mismatch");
                }

                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);

                log.info("[PAYMENT][WEBHOOK][SUCCESS] Payment marked SUCCESS | paymentId={} | paymentDate={}",
                        payment.getId(), payment.getPaymentDate());

                List<OrderItem> items =
                        orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

                log.info("[PAYMENT][WEBHOOK][SUCCESS] Reducing stock | orderId={} | itemCount={}",
                        order.getId(), items.size());

                for (OrderItem item : items) {
                    log.debug("[PAYMENT][WEBHOOK][SUCCESS] Reducing stock | variantId={} | qty={} | orderId={}",
                            item.getProductVariant().getId(), item.getQuantity(), order.getId());

                    inventoryService.reduceStock(
                            item.getProductVariant().getId(),
                            item.getQuantity(),
                            "Order paid",
                            order.getId()
                    );
                }

                log.info("[PAYMENT][WEBHOOK][SUCCESS] Changing order status to PAID | orderId={}", order.getId());

                orderStatusManager.changeStatus(
                        order,
                        OrderStatus.PAID,
                        "Payment successful"
                );

                log.info("<== [PAYMENT][WEBHOOK][SUCCESS] Payment processed successfully | paymentId={} | orderId={}",
                        payment.getId(), order.getId());

                return ApiResponse.success("Payment processed");
            }

            // ========================
            // PAYMENT FAILED
            // ========================
            if ("payment.failed".equalsIgnoreCase(event)) {

                log.warn("[PAYMENT][WEBHOOK][FAILED] Payment failed event received | paymentId={} | orderId={}",
                        payment.getId(), order.getId());

                payment.setStatus(PaymentStatus.FAILED);
                paymentRepository.save(payment);

                log.info("[PAYMENT][WEBHOOK][FAILED] Payment marked FAILED | paymentId={}", payment.getId());

                List<OrderItem> items =
                        orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

                log.info("[PAYMENT][WEBHOOK][FAILED] Releasing reserved stock | orderId={} | itemCount={}",
                        order.getId(), items.size());

                for (OrderItem item : items) {
                    log.debug("[PAYMENT][WEBHOOK][FAILED] Releasing reserved stock | variantId={} | qty={} | orderId={}",
                            item.getProductVariant().getId(), item.getQuantity(), order.getId());

                    inventoryService.releaseReservedStock(
                            item.getProductVariant().getId(),
                            item.getQuantity(),
                            order.getId()
                    );
                }

                couponUsageRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId())
                        .ifPresent(u -> {
                            log.info("[PAYMENT][WEBHOOK][FAILED] Coupon usage found, deleting usage | couponUsageId={} | orderId={}",
                                    u.getId(), order.getId());

                            u.setBitDeletedFlag(true);
                            couponUsageRepository.save(u);
                        });

                log.info("<== [PAYMENT][WEBHOOK][FAILED] Payment failed handled successfully | paymentId={} | orderId={}",
                        payment.getId(), order.getId());

                return ApiResponse.success("Payment failed handled");
            }

            log.info("[PAYMENT][WEBHOOK] Event ignored | event={} | providerOrderId={}", event, providerOrderId);
            return ApiResponse.success("Webhook ignored");

        } catch (Exception e) {
            log.error("[PAYMENT][WEBHOOK] Webhook processing failed | error={}", e.getMessage(), e);
            return ApiResponse.error("Webhook processing failed: " + e.getMessage());
        }
    }


    // -------------------------------------------------
    // LIST PAYMENTS FOR ORDER
    // -------------------------------------------------
    @Override
    public ApiResponse<?> getPaymentsForOrder(Long orderId) {
        log.info("==> [PAYMENT][LIST] Fetching payments for order | orderId={}", orderId);

        List<Payment> payments = paymentRepository.findByOrderIdAndBitDeletedFlagFalse(orderId);

        log.info("<== [PAYMENT][LIST] Payments fetched | orderId={} | count={}", orderId, payments.size());

        return ApiResponse.success(payments);
    }
}
