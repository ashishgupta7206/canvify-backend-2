package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "m_delivery_partner",
        indexes = {
                @Index(name = "idx_delivery_partner_name", columnList = "name")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPartner extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "service_area")
    private String serviceArea;

    @Column(name = "commission_percent", precision = 5, scale = 2)
    private BigDecimal commissionPercent;
}
