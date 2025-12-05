package com.canvify.test.request.refund;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundRequest {
    private Long paymentId;
    private Long returnId;         // optional, link to return
    private BigDecimal amount;     // amount to refund
    private String reason;
}