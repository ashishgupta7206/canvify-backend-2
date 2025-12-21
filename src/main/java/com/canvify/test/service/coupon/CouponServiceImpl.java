package com.canvify.test.service.coupon;

import com.canvify.test.dto.coupon.CouponApplyResponse;
import com.canvify.test.dto.coupon.CouponDTO;
import com.canvify.test.entity.Coupon;
import com.canvify.test.enums.DiscountType;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.repository.CouponRepository;
import com.canvify.test.repository.CouponUsageRepository;
import com.canvify.test.request.coupon.ApplyCouponRequest;
import com.canvify.test.request.coupon.CouponRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserContext userContext;

    /* ================= CRUD ================= */

    @Override
    public ApiResponse<?> createCoupon(CouponRequest req) {

        if (couponRepository.existsByCode(req.getCode())) {
            return ApiResponse.error("Coupon code already exists");
        }

        Coupon coupon = new Coupon();
        updateEntity(coupon, req);

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

        return ApiResponse.success(null,"Coupon deleted successfully");
    }

    @Override
    public ApiResponse<?> getCoupon(String code) {

        Coupon coupon = couponRepository.findByCodeAndBitDeletedFlagFalse(code)
                .orElseThrow(() -> new RuntimeException("Coupon not found"));

        return ApiResponse.success(mapToDTO(coupon));
    }

    /* ================= APPLY COUPON ================= */

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<?> applyCoupon(ApplyCouponRequest req) {

        Coupon coupon = couponRepository
                .findByCodeAndActiveFlagTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
                        req.getCouponCode(),
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new RuntimeException("Invalid or expired coupon"));

        // Total usage limit
        if (coupon.getUsageLimit() != null) {
            long used = couponUsageRepository
                    .countByCouponIdAndBitDeletedFlagFalse(coupon.getId());

            if (used >= coupon.getUsageLimit()) {
                return ApiResponse.error("Coupon usage limit reached");
            }
        }

        // Per-user usage
        CustomUserDetails user = userContext.getCurrentUser();
        if (user != null && coupon.getPerUserLimit() != null) {
            long userUsed = couponUsageRepository
                    .countByCouponIdAndUserIdAndBitDeletedFlagFalse(
                            coupon.getId(), user.getId());

            if (userUsed >= coupon.getPerUserLimit()) {
                return ApiResponse.error("You have already used this coupon");
            }
        }

        // Min order value
        if (coupon.getMinOrderValue() != null &&
                req.getCartAmount().compareTo(coupon.getMinOrderValue()) < 0) {
            return ApiResponse.error("Cart amount is less than minimum order value");
        }

        // Calculate discount
        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.FLAT) {
            discount = coupon.getDiscountValue();
        } else {
            discount = req.getCartAmount()
                    .multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        // Cap max discount
        if (coupon.getMaxDiscount() != null &&
                discount.compareTo(coupon.getMaxDiscount()) > 0) {
            discount = coupon.getMaxDiscount();
        }

        BigDecimal payable = req.getCartAmount().subtract(discount);

        // ✅ NO DB WRITE HERE
        return ApiResponse.success(
                new CouponApplyResponse(
                        coupon.getCode(),
                        discount.doubleValue(),
                        payable.doubleValue()
                ),
                "Coupon applied successfully"
        );
    }


    /* ================= MAPPERS ================= */

    private void updateEntity(Coupon c, CouponRequest req) {
        c.setCode(req.getCode());
        c.setDescription(req.getDescription());
        c.setDiscountType(req.getDiscountType());
        c.setDiscountValue(req.getDiscountValue());
        c.setMinOrderValue(req.getMinOrderValue());
        c.setMaxDiscount(req.getMaxDiscount());
        c.setValidFrom(req.getValidFrom());
        c.setValidTo(req.getValidTo());
        c.setUsageLimit(req.getUsageLimit());
        c.setPerUserLimit(req.getPerUserLimit());
        c.setActiveFlag(req.getActiveFlag());
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
