package com.canvify.test.request.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {

    private Long id; // null for new variant

    private String sku;
    private BigDecimal price;
    private BigDecimal mrp;
    private BigDecimal discountPercent;

    private Integer stockQty;

    private String size;
    private String weight;
    private String color;

    private String barcode;
}
