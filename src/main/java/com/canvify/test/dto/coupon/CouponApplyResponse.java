package com.canvify.test.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CouponApplyResponse {
    private String couponCode;
    private Double discountAmount;
    private Double payableAmount;
}