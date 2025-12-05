package com.canvify.test.request.product;

import com.canvify.test.enums.ProductStatus;
import lombok.Data;

import java.util.List;

@Data
public class UpdateProductRequest {

    private String name;
    private String shortDescription;
    private String longDescription;
    private String mainImage;

    private Long categoryId;
    private ProductStatus status;

    private List<ProductVariantUpdateRequest> variants;
    private List<ProductImageRequest> images; // product-level images
}
