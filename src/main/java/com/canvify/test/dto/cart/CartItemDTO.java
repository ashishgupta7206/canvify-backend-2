package com.canvify.test.dto.cart;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemDTO {

    private Long id;

    private Long productId;
    private String productName;
    private String productSlug;        // ✅ NEW
    private String productImage;

    private Long variantId;
    private String variantName;         // ✅ NEW
    private String variantLabel;

    private String sku;
    private Integer quantity;

    private BigDecimal priceAtTime;
    private BigDecimal lineTotal;
}
