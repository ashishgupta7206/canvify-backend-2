package com.canvify.test.service.coupon;

import com.canvify.test.dto.coupon.CouponDTO;
import com.canvify.test.entity.Coupon;
import com.canvify.test.entity.CouponUsage;
import com.canvify.test.enums.DiscountType;
import com.canvify.test.repository.CouponRepository;
import com.canvify.test.repository.CouponUsageRepository;
import com.canvify.test.request.coupon.ApplyCouponRequest;
import com.canvify.test.request.coupon.CouponRequest;
import com.canvify.test.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Override
    public ApiResponse<?> createCoupon(CouponRequest req) {
        if (couponRepository.existsByCode(req.getCode())) {
            return ApiResponse.error("Coupon code already exists");
        }

        Coupon coupon = mapToEntity(req);
        couponRepository.save(coupon);

        return ApiResponse.success(mapToDTO(coupon), "Coupon created successfully");
    }

    @Override
    public ApiResponse<?> updateCoupon(Long id, CouponRequest req) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invalid coupon ID"));

        updateEntity(coupon, req);
        couponRepository.save(coupon);

        return ApiResponse.success(mapToDTO(coupon), "Coupon updated successfully");
    }

    @Override
    public ApiResponse<?> deleteCoupon(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invalid coupon ID"));

        coupon.setBitDeletedFlag(true);
        couponRepository.save(coupon);

        return ApiResponse.success(null, "Coupon deleted successfully");
    }

    @Override
    public ApiResponse<?> getCoupon(String code) {
        Coupon coupon = couponRepository.findByCodeAndBitDeletedFlagFalse(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        return ApiResponse.success(mapToDTO(coupon));
    }

    @Override
    public ApiResponse<?> applyCoupon(ApplyCouponRequest req) {

        Coupon coupon = couponRepository
                .findByCodeAndActiveFlagTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        req.getCouponCode(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));

        // Usage limit check
        long totalUsed = couponUsageRepository.countByCouponIdAndBitDeletedFlagFalse(coupon.getId());
        if (coupon.getUsageLimit() != null && totalUsed >= coupon.getUsageLimit()) {
            return ApiResponse.error("This coupon has reached its usage limit.");
        }

        // Per-user usage check
        if (req.getUserId() != null && coupon.getPerUserLimit() != null) {
            long userUsed = couponUsageRepository.countByCouponIdAndUserIdAndBitDeletedFlagFalse(
                    coupon.getId(), req.getUserId()
            );

            if (userUsed >= coupon.getPerUserLimit()) {
                return ApiResponse.error("You have already used this coupon.");
            }
        }

        // Minimum order value check
        if (req.getCartAmount().compareTo(coupon.getMinOrderValue()) < 0) {
            return ApiResponse.error("Cart amount is less than minimum order value for coupon.");
        }

        // Calculate discount
        BigDecimal discount = BigDecimal.ZERO;

        if (coupon.getDiscountType() == DiscountType.FLAT) {
            discount = coupon.getDiscountValue();
        } else {
            discount = req.getCartAmount().multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100));
        }

        // Cap max discount
        if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }

        BigDecimal payable = req.getCartAmount().subtract(discount);

        return ApiResponse.success(
                new com.canvify.test.dto.coupon.CouponApplyResponse(
                        req.getCouponCode(),
                        discount.doubleValue(),
                        payable.doubleValue()
                ),
                "Coupon applied successfully"
        );
    }

    // Mapping methods
    private Coupon mapToEntity(CouponRequest req) {
        Coupon c = new Coupon();
        updateEntity(c, req);
        return c;
    }

    private void updateEntity(Coupon coupon, CouponRequest req) {
        coupon.setCode(req.getCode());
        coupon.setDescription(req.getDescription());
        coupon.setDiscountType(req.getDiscountType());
        coupon.setDiscountValue(req.getDiscountValue());
        coupon.setMinOrderValue(req.getMinOrderValue());
        coupon.setMaxDiscount(req.getMaxDiscount());
        coupon.setValidFrom(req.getValidFrom());
        coupon.setValidTo(req.getValidTo());
        coupon.setUsageLimit(req.getUsageLimit());
        coupon.setPerUserLimit(req.getPerUserLimit());
        coupon.setActiveFlag(req.getActiveFlag());
    }

    private CouponDTO mapToDTO(Coupon c) {
        CouponDTO dto = new CouponDTO();
        dto.setId(c.getId());
        dto.setCode(c.getCode());
        dto.setDescription(c.getDescription());
        dto.setDiscountType(c.getDiscountType());
        dto.setDiscountValue(c.getDiscountValue());
        dto.setMinOrderValue(c.getMinOrderValue());
        dto.setMaxDiscount(c.getMaxDiscount());
        dto.setValidFrom(c.getValidFrom());
        dto.setValidTo(c.getValidTo());
        dto.setUsageLimit(c.getUsageLimit());
        dto.setPerUserLimit(c.getPerUserLimit());
        dto.setActiveFlag(c.getActiveFlag());
        return dto;
    }
}