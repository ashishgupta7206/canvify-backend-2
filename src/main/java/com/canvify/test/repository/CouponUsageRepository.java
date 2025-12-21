package com.canvify.test.repository;

import com.canvify.test.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    long countByCouponIdAndBitDeletedFlagFalse(Long couponId);

    long countByCouponIdAndUserIdAndBitDeletedFlagFalse(Long couponId, Long userId);

    Optional<CouponUsage> findByOrderIdAndBitDeletedFlagFalse(Long orderId);

    List<CouponUsage> findByUserId(Long userId);
}