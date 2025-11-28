package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "t_product_reviews", indexes = {
        @Index(name = "idx_reviews_product", columnList = "product_id")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductReview extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    @JsonIgnore
    private Product product;

    @Column(name = "rating")
    private Short rating; // validate 1-5 in service/controller

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Column(name = "images_url", length = 1000)
    private String imagesUrl; // consider separate table or jsonb
}

