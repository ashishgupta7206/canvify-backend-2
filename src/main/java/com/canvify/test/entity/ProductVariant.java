package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.canvify.test.enums.ProductType;
import com.canvify.test.enums.productVariantMktStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "m_product_variant", indexes = {
        @Index(name = "idx_variant_product", columnList = "product_id"),
        @Index(name = "idx_variant_sku", columnList = "sku")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductVariant extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore
    private Product product;

    @Column(name = "sku", length = 100, unique = true)
    private String sku;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "mrp", precision = 10, scale = 2)
    private BigDecimal mrp;

    @Column(name = "discount_percent", precision = 5, scale = 2)
    private BigDecimal discountPercent;

    @Column(name = "stock_qty")
    private Integer stockQty;

    @Column(name = "size", length = 100)
    private String size;

    @Column(name = "weight", length = 100)
    private String weight;

    @Column(name = "color", length = 100)
    private String color;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "category_sort_order")
    private Long categorySortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nutrition_info_id")
    @JsonIgnore
    private NutritionInfo nutritionInfo;

    @Column(name = "rating")
    private String rating;

    @Column(name = "storage_instructions")
    private String storageInstructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private ProductType productType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "m_product_variant_combo",
            joinColumns = @JoinColumn(name = "combo_variant_id"),
            inverseJoinColumns = @JoinColumn(name = "child_variant_id")
    )
    private List<ProductVariant> listOfVariantInCombo;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_variant_mkt_status")
    private productVariantMktStatus productVariantMktStatus;

    @Column(name = "product_variant_mkt_status_sort_order")
    private Long productVariantMktStatusSortOrder;

    @Column(name= "sort_order")
    private Long sortOrder;

}
