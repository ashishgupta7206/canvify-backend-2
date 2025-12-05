package com.canvify.test.response.product;

import lombok.Data;

@Data
public class ProductImageResponse {

    private Long id;
    private String imageUrl;
    private Integer sortOrder;
}
