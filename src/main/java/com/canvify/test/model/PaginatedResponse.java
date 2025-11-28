package com.canvify.test.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PaginatedResponse<T> {

    private List<T> items;      // list of DTOs
    private Pagination pagination;
}
