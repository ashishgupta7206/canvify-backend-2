package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.enums.RefundStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_refund",
        indexes = {
                @Index(name = "idx_refund_provider_ref", columnList = "provider_refund_id"),
                @Index(name = "idx_refund_payment", columnList = "payment_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Refund extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Parent payment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonIgnore
    private Payment payment;

    // Optional return linkage
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id")
    @JsonIgnore
    private Return returnRequest;

    // Razorpay refund id
    @Column(name = "provider_refund_id", unique = true)
    private String providerRefundId;

    // Razorpay payment id
    @Column(name = "provider_payment_id", nullable = false)
    private String providerPaymentId;

    @Column(name = "refund_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "refunded_on")
    private LocalDateTime refundedOn;
}
