package com.canvify.test.request.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {

    /**
     * Optional - for guest users frontend should pass cartToken.
     * If user is logged in, service will use currentUser and ignore cartToken if a user cart exists.
     */
    private String cartToken;

    @NotNull
    private Long productVariantId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
