package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.DeliveryAssignmentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "t_delivery_assignment",
        indexes = {
                @Index(name = "idx_delivery_assign_order", columnList = "order_id"),
                @Index(name = "idx_delivery_assign_partner", columnList = "partner_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryAssignment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    private DeliveryPartner partner;

    @Column(name = "assigned_on")
    private LocalDateTime assignedOn;

    @Column(name = "delivered_on")
    private LocalDateTime deliveredOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private DeliveryAssignmentStatus status = DeliveryAssignmentStatus.ASSIGNED;
}
