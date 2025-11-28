package com.canvify.test.dto.product;

import lombok.Data;
import java.math.BigDecimal;

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
}
