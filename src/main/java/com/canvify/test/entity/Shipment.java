package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "t_shipment",
        indexes = {
                @Index(name = "idx_shipment_order", columnList = "order_id"),
                @Index(name = "idx_shipment_tracking", columnList = "tracking_number")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shipment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Orders order;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "tracking_number", unique = true)
    private String trackingNumber;

    @Column(name = "shipped_on")
    private LocalDateTime shippedOn;

    @Column(name = "delivered_on")
    private LocalDateTime deliveredOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status", length = 50)
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;
}
