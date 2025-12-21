package com.canvify.test.service.refund;

import com.canvify.test.dto.refund.RefundResponseDTO;
import com.canvify.test.entity.Payment;
import com.canvify.test.entity.Refund;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.enums.RefundStatus;
import com.canvify.test.integration.PaymentProviderClient;
import com.canvify.test.repository.PaymentRepository;
import com.canvify.test.repository.RefundRepository;
import com.canvify.test.request.refund.RefundRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.orderstatus.OrderStatusManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext; // ✅ FIXED
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusManager orderStatusManager;
    private final ApplicationContext ctx; // ✅ FIXED

    @Override
    @Transactional
    public ApiResponse<RefundResponseDTO> initiateRefund(RefundRequest req) {

        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Prevent duplicate refunds
        boolean exists = refundRepository
                .existsByPaymentIdAndStatus(payment.getId(), RefundStatus.PENDING);

        if (exists) {
            return ApiResponse.error("Refund already in progress");
        }

        Refund refund = new Refund();
        refund.setPayment(payment);

        // ❌ OLD (wrong)
        // refund.setProviderPaymentId(payment.getPaymentReferenceId());

        // ✅ NEW (correct)
        refund.setProviderPaymentId(payment.getProviderPaymentId());

        refund.setRefundAmount(req.getAmount());
        refund.setStatus(RefundStatus.PENDING);
        refund = refundRepository.save(refund);

        PaymentProviderClient client =
                ctx.getBean(payment.getPaymentProvider().name(), PaymentProviderClient.class);

        // ❌ OLD
        // client.refundPayment(payment.getPaymentReferenceId(), ...)

        // ✅ NEW (Razorpay requires payment_id)
        Map<String, Object> res =
                client.refundPayment(
                        payment.getProviderPaymentId(),
                        req.getAmount(),
                        req.getReason()
                );

        String providerRefundId = (String) res.get("refundId");
        String status = String.valueOf(res.get("status"));

        refund.setProviderRefundId(providerRefundId);

        if ("SUCCESS".equalsIgnoreCase(status)) {
            refund.setStatus(RefundStatus.SUCCESS);
            refund.setRefundedOn(LocalDateTime.now());

            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        } else {
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason("Provider refund failed");
        }

        refundRepository.save(refund);

        RefundResponseDTO dto = new RefundResponseDTO();
        dto.setRefundId(refund.getId());
        dto.setProviderRefundId(providerRefundId);
        dto.setStatus(refund.getStatus().name());

        return ApiResponse.success(dto, "Refund processed");
    }
}
