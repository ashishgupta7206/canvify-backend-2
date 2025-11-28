package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "t_cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_item_variant", columnList = "product_variant_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CartItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // the cart (either guest cart with token or user cart)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    @JsonIgnore
    private Cart cart;

    // product variant chosen
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    @JsonIgnore
    private ProductVariant productVariant;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // snapshot of selling price at time of adding to cart (or last update)
    @Column(name = "price_at_time", precision = 10, scale = 2)
    private BigDecimal priceAtTime;
}
