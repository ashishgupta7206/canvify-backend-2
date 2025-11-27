package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "t_return")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Return extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id")
    private OrderItems orderItem;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "return_status", length = 50)
    private String returnStatus;

    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "created_on")
    private LocalDateTime createdOn;
}
