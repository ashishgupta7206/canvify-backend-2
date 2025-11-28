package com.canvify.test.model;

import lombok.Data;
import org.springframework.data.domain.*;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class BaseIndexRequest {

    @Valid
    private PaginationRequest pagination;

    @Valid
    private List<SortingRequest> sorting;

    public Sort buildSort() {
        List<SortingRequest> sorting = Optional.ofNullable(getSorting()).orElse(List.of());
        List<Sort.Order> orders = new ArrayList<>();

        for (SortingRequest req : sorting) {
            if ("asc".equalsIgnoreCase(req.getOrder())) {
                orders.add(Sort.Order.asc(req.getOrderBy()));
            } else {
                orders.add(Sort.Order.desc(req.getOrderBy()));
            }
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    public Pageable buildPageable() {

        Integer page = pagination != null ? pagination.getPage() : null;
        Integer size = pagination != null ? pagination.getSize() : null;

        Sort sort = buildSort();

        if (page == null || size == null) {
            return sort.isSorted() ? Pageable.unpaged(sort) : Pageable.unpaged();
        }

        return PageRequest.of(page, size, sort);
    }
}

