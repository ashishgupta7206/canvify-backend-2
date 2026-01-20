package com.canvify.test.dto.order;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDTO {

    private Long id;
    private Long variantId;
    private String productName;
    private String productId;
    private String ProductVariantName;
    private String variantLabel;
    private Integer quantity;
    private BigDecimal priceAtTime;
    private BigDecimal mrp;
    private BigDecimal DiscountPercent;
    private String variantImage;

    private String personalizationName;

}