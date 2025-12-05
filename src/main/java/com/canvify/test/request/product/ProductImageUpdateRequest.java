package com.canvify.test.request.product;

import lombok.Data;

@Data
public class ProductImageUpdateRequest {

    private Long id; // null = new image
    private String imageUrl;
    private Integer sortOrder;

    private Boolean deleteFlag = false; // mark image deleted
}
