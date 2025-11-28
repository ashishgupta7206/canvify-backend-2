package com.canvify.test.dto.product;

import com.canvify.test.enums.ProductStatus;
import lombok.Data;
import java.util.List;

@Data
public class ProductDTO {

    private Long id;
    private String name;
    private String slug;
    private String shortDescription;
    private String longDescription;
    private String mainImage;
    private ProductStatus status;
    private Long categoryId;

    private List<ProductVariantDTO> variants;
    private List<ProductImageDTO> images;
}
