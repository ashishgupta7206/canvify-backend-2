package com.canvify.test.controller;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.coupon.ApplyCouponRequest;
import com.canvify.test.request.coupon.CouponRequest;
import com.canvify.test.service.coupon.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /* ================= ADMIN APIs ================= */

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CouponRequest req) {
        return ResponseEntity.ok(couponService.createCoupon(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> update(
            @PathVariable Long id,
            @Valid @RequestBody CouponRequest req) {

        return ResponseEntity.ok(couponService.updateCoupon(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> delete(@PathVariable Long id) {
        return ResponseEntity.ok(couponService.deleteCoupon(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<?>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(couponService.getCoupon(code));
    }

    /* ================= PUBLIC API ================= */

    @PostMapping("/apply")
    public ResponseEntity<ApiResponse<?>> apply(@Valid @RequestBody ApplyCouponRequest req) {
        return ResponseEntity.ok(couponService.applyCoupon(req));
    }
}
