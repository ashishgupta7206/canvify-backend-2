package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "m_coupon")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Coupon extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType; // enum

    private Double discountValue;

    private Double minOrderValue;

    private Double maxDiscount;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;

    private Integer usageLimit;

    private Integer perUserLimit;

    private Boolean activeFlag;
}
