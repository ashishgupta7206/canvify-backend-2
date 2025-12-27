package com.canvify.test.request.category;

import com.canvify.test.enums.CategoryStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateRequest {

    @NotBlank
    private String name;

    private String description;
    private String imageUrl;
    private Long parentId;
    private CategoryStatus status;
}
