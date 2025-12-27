package com.canvify.test.response.product;

import com.canvify.test.enums.ProductStatus;
import com.canvify.test.enums.ProductType;
import com.canvify.test.enums.productVariantMktStatus;
import com.canvify.test.model.BaseIndexRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GetAllProductsRequest extends BaseIndexRequest {
    private Long categoryId;
    private String productName;
    private String productVariantName;
    private productVariantMktStatus productVariantMktStatus;
    private ProductStatus status;
    private ProductType productType;

}
