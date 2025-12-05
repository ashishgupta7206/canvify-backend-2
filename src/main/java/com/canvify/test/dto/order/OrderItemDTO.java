package com.canvify.test.dto.order;

import lombok.Data;

@Data
public class OrderItemDTO {

    private Long id;
    private Long variantId;
    private String productName;
    private String variantLabel;
    private Integer quantity;
    private Double priceAtTime;
    private Double totalPrice;
}