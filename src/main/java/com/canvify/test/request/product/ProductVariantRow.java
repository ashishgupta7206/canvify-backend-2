package com.canvify.test.request.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ProductVariantRow {
    private Long productId;
    private Long variantId;
}

