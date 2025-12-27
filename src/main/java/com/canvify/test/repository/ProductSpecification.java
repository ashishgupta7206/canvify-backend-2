package com.canvify.test.repository;

import com.canvify.test.entity.Product;
import com.canvify.test.entity.ProductVariant;
import com.canvify.test.enums.ProductType;
import com.canvify.test.enums.productVariantMktStatus;
import com.canvify.test.response.product.GetAllProductsRequest;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

    public static Specification<Product> filter(GetAllProductsRequest request) {

        return (root, query, cb) -> {

            query.distinct(true);

            Join<Product, ProductVariant> variantJoin =
                    root.join("variants", JoinType.LEFT);

            Predicate predicate = cb.conjunction();

            // -----------------------
            // Product-level filters
            // -----------------------
            if (request.getCategoryId() != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("category").get("id"),
                                request.getCategoryId())
                );
            }

            if (request.getProductName() != null &&
                    !request.getProductName().isBlank()) {

                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + request.getProductName().toLowerCase() + "%"
                        )
                );
            }

            if (request.getStatus() != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(root.get("status"),
                                request.getStatus())
                );
            }

            // -----------------------
            // Variant-level filters
            // -----------------------
            if (request.getProductVariantName() != null &&
                    !request.getProductVariantName().isBlank()) {

                predicate = cb.and(
                        predicate,
                        cb.like(
                                cb.lower(variantJoin.get("name")),
                                "%" + request.getProductVariantName().toLowerCase() + "%"
                        )
                );
            }

            if (request.getProductVariantMktStatus() != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(
                                variantJoin.get("productVariantMktStatus"),
                                request.getProductVariantMktStatus()
                        )
                );
            }

            if (request.getProductType() != null) {
                predicate = cb.and(
                        predicate,
                        cb.equal(
                                variantJoin.get("productType"),
                                request.getProductType()
                        )
                );
            }

            return predicate;
        };
    }
}
