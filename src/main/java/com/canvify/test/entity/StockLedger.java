package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import com.canvify.test.enums.StockChangeType;
import com.canvify.test.enums.StockReferenceType;

@Entity
@Table(name = "t_stock_ledger")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StockLedger extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", length = 50, nullable = false)
    private StockChangeType changeType;

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private StockReferenceType referenceType;
}

