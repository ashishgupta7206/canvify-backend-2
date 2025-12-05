package com.canvify.test.dto.refund;

import lombok.Data;

@Data
public class RefundResponseDTO {
    private Long refundId;
    private String refundReferenceId; // provider refund id
    private String status;
}