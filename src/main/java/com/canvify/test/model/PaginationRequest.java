package com.canvify.test.model;

import lombok.Data;

@Data
public class PaginationRequest {
    private Integer page;  // page number (0-based)
    private Integer size;  // page size
}

