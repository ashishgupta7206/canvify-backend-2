package com.canvify.test.dto.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDTO {
    private Long cartId;
    private String cartToken; // for guest
    private Long userId;      // for logged-in user
    private Integer totalItems;
    private BigDecimal totalAmount;
    private List<CartItemDTO> items;
}
