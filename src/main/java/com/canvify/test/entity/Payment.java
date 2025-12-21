package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.PaymentMethod;
import com.canvify.test.enums.PaymentProvider;
import com.canvify.test.enums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "t_payment",
        indexes = {
                @Index(name = "idx_payment_provider_order", columnList = "provider_order_id"),
                @Index(name = "idx_payment_provider_payment", columnList = "provider_payment_id")
        }
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -----------------------------
    // RELATIONS
    // -----------------------------
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Orders order;

    // -----------------------------
    // PROVIDER DETAILS
    // -----------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false)
    private PaymentProvider paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    // Razorpay order_id (created before payment)
    @Column(name = "provider_order_id", length = 100, unique = true)
    private String providerOrderId;

    // Razorpay payment_id (received in webhook)
    @Column(name = "provider_payment_id", length = 100, unique = true)
    private String providerPaymentId;

    // -----------------------------
    // AMOUNT DETAILS
    // -----------------------------
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", length = 10, nullable = false)
    private String currency = "INR";

    // -----------------------------
    // STATUS
    // -----------------------------
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    // -----------------------------
    // FAILURE DETAILS (optional)
    // -----------------------------
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
}


