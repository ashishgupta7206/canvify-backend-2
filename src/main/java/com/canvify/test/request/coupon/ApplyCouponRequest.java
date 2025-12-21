package com.canvify.test.request.coupon;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyCouponRequest {

    @NotBlank
    private String couponCode;

    @NotNull
    private BigDecimal cartAmount;
}
