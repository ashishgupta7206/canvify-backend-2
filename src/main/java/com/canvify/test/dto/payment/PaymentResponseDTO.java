package com.canvify.test.dto.payment;

import lombok.Data;

@Data
public class PaymentResponseDTO {
    private Long paymentId;
    private String paymentReferenceId; // provider order id / client token
    private String providerClientToken; // token needed by frontend
}