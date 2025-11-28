package com.canvify.test.service.coupon;

import com.canvify.test.dto.coupon.CouponDTO;
import com.canvify.test.request.coupon.ApplyCouponRequest;
import com.canvify.test.request.coupon.CouponRequest;
import com.canvify.test.response.ApiResponse;

public interface CouponService {

    ApiResponse<?> createCoupon(CouponRequest request);

    ApiResponse<?> updateCoupon(Long id, CouponRequest request);

    ApiResponse<?> deleteCoupon(Long id);

    ApiResponse<?> getCoupon(String code);

    ApiResponse<?> applyCoupon(ApplyCouponRequest request);
}