package com.canvify.test.dto.refund;

import lombok.Data;

@Data
public class RefundResponseDTO {
    private Long refundId;
    private String providerRefundId;
    private String status;
}
