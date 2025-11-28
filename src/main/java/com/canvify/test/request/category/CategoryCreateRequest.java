package com.canvify.test.request.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {

    @NotBlank
    private String name;

    private String description;
    private String imageUrl;
    private Long parentId;
}
