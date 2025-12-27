package com.canvify.test.dto.product;

import com.canvify.test.enums.ProductType;
import com.canvify.test.enums.productVariantMktStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantDTO {

    private Long id;
    private String sku;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPercent;
    private Integer stockQty;
    private String size;
    private String weight;
    private String color;
    private String barcode;
    private Boolean isActive;
    private Long categorySortOrder;
    private Long nutritionInfoId;
    private String rating;
    private String storageInstructions;
    private productVariantMktStatus productVariantMktStatus;
    private Long productVariantMktStatusSortOrder;
    private Long sortOrder;
    private ProductType productType;
    private List<Long> listOfVariantInCombo;
}
