package com.canvify.test.dto.coupon;

import com.canvify.test.enums.DiscountType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponDTO {
    private Long id;
    private String code;
    private String description;
    private DiscountType discountType;
    private Double discountValue;
    private Double minOrderValue;
    private Double maxDiscount;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer usageLimit;
    private Integer perUserLimit;
    private Boolean activeFlag;
}