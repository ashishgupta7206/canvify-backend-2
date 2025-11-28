package com.canvify.test.request.product;

import com.canvify.test.enums.ProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateProductRequest {

    @NotBlank
    private String name;

    private String shortDescription;
    private String longDescription;

    private String mainImage;

    @NotNull
    private Long categoryId;

    private ProductStatus status = ProductStatus.DRAFT;

    private List<ProductVariantRequest> variants;
    private List<ProductImageRequest> images;
}
