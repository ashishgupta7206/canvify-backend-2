package com.canvify.test.utility;

import com.canvify.test.model.Pagination;
import org.springframework.data.domain.Page;

public class PaginationUtils {

    public static Pagination from(Page<?> page) {
        return Pagination.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}

