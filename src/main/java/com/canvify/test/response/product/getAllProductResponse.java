package com.canvify.test.response.product;

import com.canvify.test.enums.ProductType;
import com.canvify.test.enums.productVariantMktStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class getAllProductResponse {
    private Long productId;
    private String productName;
    private String productSlug;

    private String shortDescription;
    private String longDescription;

    private String mainImage;
    private String status;

    private Long categoryId;
    private String categoryName;

    private Long productVariantId;

    private String productVariantName;
    private String productVariantSku;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPercent;

    private Integer stockQty;

    private String size;
    private String weight;
    private String color;

    private String barcode;

    private Boolean productVariantIsActive;
    private Long categorySortOrder;
    private String productVariantRating;
    private productVariantMktStatus productVariantMktStatus;
    private Long productVariantMktStatusSortOrder;
    private Long sortOrder;

    private ProductType productType;
    private List<Long> listOfVariantInCombo;

    private List<ProductImageResponse> images;
}
