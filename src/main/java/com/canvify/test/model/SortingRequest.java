package com.canvify.test.model;

import lombok.Data;

@Data
public class SortingRequest {
    private String orderBy; // field name
    private String order;   // "asc" or "desc"
}

