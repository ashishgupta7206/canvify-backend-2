package com.canvify.test.response.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariantResponse {

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

    private List<ProductImageResponse> images; // variant-level images
}
