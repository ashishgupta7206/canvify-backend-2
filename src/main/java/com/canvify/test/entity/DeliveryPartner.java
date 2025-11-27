package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "m_delivery_partner")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryPartner extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "mobile", length = 50)
    private String mobile;

    @Column(name = "service_area")
    private String serviceArea;

    @Column(name = "commission_percent", precision = 5, scale = 2)
    private BigDecimal commissionPercent;
}
