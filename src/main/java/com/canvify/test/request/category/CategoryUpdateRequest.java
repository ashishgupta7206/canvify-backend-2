package com.canvify.test.request.category;

import lombok.Data;

@Data
public class CategoryUpdateRequest {

    private String name;
    private String description;
    private String imageUrl;
    private Long parentId;
}
