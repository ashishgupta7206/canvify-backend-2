package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "t_stock_ledger")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockLedger extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;

    @Column(name = "change_type", length = 100)
    private String changeType;

    @Column(name = "quantity_change")
    private Integer quantityChange;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "created_on")
    private LocalDateTime createdOn;
}
