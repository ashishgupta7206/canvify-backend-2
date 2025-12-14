package com.canvify.test.response.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductAndProductVariantResponse {

    // Product Details
    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String longDescription;
    private String mainImage;
    private String status;

    private Long categoryId;
    private String categoryName;

    // Single Variant Details
    private ProductVariantResponse variant;

    // Product Images
    private List<ProductImageResponse> images;
}
