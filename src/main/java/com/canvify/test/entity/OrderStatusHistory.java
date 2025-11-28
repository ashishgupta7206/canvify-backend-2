package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_order_status_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderStatusHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Orders order;

    @Enumerated(EnumType.STRING)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    private OrderStatus newStatus;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}

