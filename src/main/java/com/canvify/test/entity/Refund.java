package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_refund")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Refund extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id")
    private Return returnRequest;

    @Column(name = "refund_reference_id")
    private String refundReferenceId;

    @Column(name = "refunded_amount", precision = 10, scale = 2)
    private BigDecimal refundedAmount;

    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    @Column(name = "status", length = 50)
    private String status;
}
