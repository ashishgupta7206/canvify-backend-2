package com.canvify.test.dto.wishlist;

import lombok.Data;

@Data
public class WishlistItemDTO {
    private Long id;

    // Product minimal
    private Long productId;
    private String productName;
    private String productSlug;
    private String productMainImage;

    // Variant minimal (nullable)
    private Long variantId;
    private String sku;
    private String variantLabel; // size/weight description
    private Integer stockQty;
    private String price; // string to avoid null formatting; or BigDecimal if you prefer
}