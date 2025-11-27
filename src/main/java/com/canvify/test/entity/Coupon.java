package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "discount_type", length = 20) // percentage/flat
    private String discountType;

    @Column(name = "discount_value", precision = 10, scale = 2)
    private Double discountValue;

    @Column(name = "min_order_value", precision = 10, scale = 2)
    private Double minOrderValue;

    @Column(name = "max_discount", precision = 10, scale = 2)
    private Double maxDiscount;

    @Column(name = "valid_from")
    private java.time.LocalDateTime validFrom;

    @Column(name = "valid_to")
    private java.time.LocalDateTime validTo;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "per_user_limit")
    private Integer perUserLimit;

    @Column(name = "active_flag")
    private Boolean activeFlag;
}
