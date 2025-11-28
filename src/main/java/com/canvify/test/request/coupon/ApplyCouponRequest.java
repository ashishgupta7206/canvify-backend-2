package com.canvify.test.request.coupon;

import lombok.Data;

@Data
public class ApplyCouponRequest {
    private String couponCode;
    private Double cartAmount;
    private Long userId; // null for guest users
}