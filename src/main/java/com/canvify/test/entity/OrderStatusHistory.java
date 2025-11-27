package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_order_status_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusHistory extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Orders order;

    @Column(name = "old_status", length = 50)
    private String oldStatus;

    @Column(name = "new_status", length = 50)
    private String newStatus;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
}
