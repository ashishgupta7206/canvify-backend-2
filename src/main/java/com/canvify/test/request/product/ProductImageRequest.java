package com.canvify.test.request.product;

import lombok.Data;

@Data
public class ProductImageRequest {

    private Long id; // null for new image
    private String imageUrl;
    private Integer sortOrder;
}
