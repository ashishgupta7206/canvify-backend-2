package com.canvify.test.dto.category;

import com.canvify.test.enums.CategoryStatus;
import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private Long parentId;
    private CategoryStatus status;
}
