package com.canvify.test.repository;

import com.canvify.test.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeAndBitDeletedFlagFalse(String code);

    boolean existsByCode(String code);

    // active + within date range
    Optional<Coupon> findByCodeAndActiveFlagTrueAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            String code,
            LocalDateTime from,
            LocalDateTime to
    );
}