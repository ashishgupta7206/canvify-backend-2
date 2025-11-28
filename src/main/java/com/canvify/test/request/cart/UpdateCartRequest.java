package com.canvify.test.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCartRequest {

    @NotNull
    private Long cartItemId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
