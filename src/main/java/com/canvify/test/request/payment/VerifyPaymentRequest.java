package com.canvify.test.request.payment;

import lombok.Data;

@Data
public class VerifyPaymentRequest {

    private Long orderId;

    // Razorpay returns these in handler response
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
}

