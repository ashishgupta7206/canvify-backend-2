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
@Table(name = "t_payment")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Payment extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Orders order;

    @Enumerated(EnumType.STRING)
    private PaymentProvider paymentProvider;

    @Column(name = "payment_reference_id")
    private String paymentReferenceId;

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
}

