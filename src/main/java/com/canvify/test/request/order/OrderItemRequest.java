package com.canvify.test.request.order;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long productVariantId;
    private Integer quantity;
}