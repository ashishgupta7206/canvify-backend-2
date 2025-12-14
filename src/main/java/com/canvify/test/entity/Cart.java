package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "t_cart", indexes = {
        @Index(name = "idx_cart_user", columnList = "user_id"),
        @Index(name = "idx_cart_token", columnList = "cart_token")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Cart extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // If logged-in user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // If guest user, cart_token is used
    @Column(name = "cart_token", unique = true, length = 200)
    private String cartToken;

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "total_amount")
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
