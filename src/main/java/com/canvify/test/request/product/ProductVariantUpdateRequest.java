package com.canvify.test.request.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantUpdateRequest {

    private Long id; // null = new variant

    private String sku;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPercent;

    private Integer stockQty;
    private String size;
    private String weight;
    private String color;
    private String barcode;

    private Boolean deleteFlag = false; // mark variant deleted

    private List<ProductImageUpdateRequest> images;
}
