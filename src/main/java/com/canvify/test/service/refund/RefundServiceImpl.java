package com.canvify.test.service.refund;

import com.canvify.test.dto.refund.RefundResponseDTO;
import com.canvify.test.entity.Refund;
import com.canvify.test.entity.Payment;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.integration.PaymentProviderClient;
import com.canvify.test.repository.PaymentRepository;
import com.canvify.test.repository.RefundRepository;
import com.canvify.test.request.refund.RefundRequest;
import com.canvify.test.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final org.springframework.context.ApplicationContext ctx;

    @Override
    @Transactional
    public ApiResponse<RefundResponseDTO> initiateRefund(RefundRequest req) {
        Payment payment = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // create Refund record with status pending
        Refund refund = new Refund();
        refund.setPayment(payment);
        // optional: link returnRequest if provided
        // refund.setReturnRequest(...);
        refund.setRefundedAmount(req.getAmount());
        refund.setRefundDate(java.time.LocalDateTime.now());
        refund.setStatus(PaymentStatus.PENDING);
        refund = refundRepository.save(refund);

        // call provider
        String providerBean = payment.getPaymentProvider().name();
        PaymentProviderClient client = ctx.getBean(providerBean, PaymentProviderClient.class);

        Map<String,Object> res = client.refundPayment(payment.getPaymentReferenceId(), req.getAmount(), req.getReason());

        String refundId = (String) res.get("refundId");
        String status = (String) res.getOrDefault("status", "PENDING");

        refund.setRefundReferenceId(refundId);
        refund.setStatus(PaymentStatus.valueOf(status));
        refundRepository.save(refund);

        // update payment status if fully refunded
        if ("SUCCESS".equalsIgnoreCase(status) || "REFUNDED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);

            // optionally update order and order status history
        }

        RefundResponseDTO dto = new RefundResponseDTO();
        dto.setRefundId(refund.getId());
        dto.setRefundReferenceId(refundId);
        dto.setStatus(status);

        return ApiResponse.success(dto, "Refund initiated");
    }

    @Override
    public ApiResponse<?> getRefundsForPayment(Long paymentId) {
        var list = refundRepository.findByPaymentIdAndBitDeletedFlagFalse(paymentId);
        return ApiResponse.success(list);
    }
}