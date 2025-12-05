package com.canvify.test.controller.coupon;

import com.canvify.test.request.coupon.ApplyCouponRequest;
import com.canvify.test.request.coupon.CouponRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.coupon.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@RequestBody CouponRequest req) {
        return ResponseEntity.ok(couponService.createCoupon(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable Long id, @RequestBody CouponRequest req) {
        return ResponseEntity.ok(couponService.updateCoupon(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.deleteCoupon(id));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<?>> get(@PathVariable String code) {
        return ResponseEntity.ok(couponService.getCoupon(code));
    }

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<?>> apply(@RequestBody ApplyCouponRequest req) {
        return ResponseEntity.ok(couponService.applyCoupon(req));
    }
}