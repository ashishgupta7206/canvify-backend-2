package com.canvify.test.request.coupon;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplyCouponRequest {
    private String couponCode;
    private BigDecimal cartAmount;
    private Long userId; // null for guest users
}