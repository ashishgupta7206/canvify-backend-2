package com.canvify.test.request.wishlist;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddWishlistRequest {

    @NotNull
    private Long productId;

    // optional
    private Long productVariantId;
}