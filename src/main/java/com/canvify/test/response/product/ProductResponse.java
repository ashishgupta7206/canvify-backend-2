package com.canvify.test.response.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String slug;

    private String shortDescription;
    private String longDescription;

    private String mainImage;
    private String status;

    private Long categoryId;
    private String categoryName;

    private List<ProductVariantResponse> variants;
    private List<ProductImageResponse> images;
}
