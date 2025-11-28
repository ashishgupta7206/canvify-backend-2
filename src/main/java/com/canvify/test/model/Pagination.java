package com.canvify.test.model;

import lombok.Data;
import org.springframework.data.domain.Page;

@Data
public class Pagination {

    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public Pagination(Page<?> pageData) {
        this.page = pageData.getNumber();
        this.size = pageData.getSize();
        this.totalElements = pageData.getTotalElements();
        this.totalPages = pageData.getTotalPages();
    }
}

