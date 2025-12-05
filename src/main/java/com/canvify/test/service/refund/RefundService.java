package com.canvify.test.service.refund;

import com.canvify.test.dto.refund.RefundResponseDTO;
import com.canvify.test.request.refund.RefundRequest;
import com.canvify.test.model.ApiResponse;

public interface RefundService {
    ApiResponse<RefundResponseDTO> initiateRefund(RefundRequest req);
    ApiResponse<?> getRefundsForPayment(Long paymentId);
}