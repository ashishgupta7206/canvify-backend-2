package com.canvify.test.request.payment;

import com.canvify.test.enums.PaymentMethod;
import com.canvify.test.enums.PaymentProvider;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod method;     // CARD/UPI/COD etc. (if needed)
    private PaymentProvider provider; // RAZORPAY/STRIPE...
    private String currency = "INR";
}