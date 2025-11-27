package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "t_delivery_assignment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAssignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private DeliveryPartner partner;

    @Column(name = "assigned_on")
    private LocalDateTime assignedOn;

    @Column(name = "delivered_on")
    private LocalDateTime deliveredOn;

    @Column(name = "status", length = 50)
    private String status;
}
