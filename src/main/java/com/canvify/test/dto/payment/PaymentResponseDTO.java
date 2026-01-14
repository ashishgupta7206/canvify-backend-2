package com.canvify.test.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentResponseDTO {

    private Long paymentId;

    private String paymentReferenceId; // Razorpay order_id

    private String providerKey; // Razorpay key_id (PUBLIC)

    private BigDecimal amount;

    private String currency;
}
