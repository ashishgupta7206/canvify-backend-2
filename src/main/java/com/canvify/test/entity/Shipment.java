package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_shipment")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shipment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    @Column(name = "courier_name")
    private String courierName;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "shipped_on")
    private LocalDateTime shippedOn;

    @Column(name = "delivered_on")
    private LocalDateTime deliveredOn;

    @Column(name = "delivery_status", length = 50)
    private String deliveryStatus;
}
