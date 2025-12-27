package com.canvify.test.entity;

import com.canvify.test.entity.audit.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "m_nutrition_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NutritionInfo extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // ---- FSSAI Nutrition Fields ----
    @Column(name = "energy_kcal", precision = 6, scale = 2)
    private BigDecimal energyKcal;

    @Column(name = "protein_g", precision = 6, scale = 2)
    private BigDecimal proteinG;

    @Column(name = "carbohydrate_g", precision = 6, scale = 2)
    private BigDecimal carbohydrateG;

    @Column(name = "total_sugars_g", precision = 6, scale = 2)
    private BigDecimal totalSugarsG;

    @Column(name = "added_sugars_g", precision = 6, scale = 2)
    private BigDecimal addedSugarsG;

    @Column(name = "total_fat_g", precision = 6, scale = 2)
    private BigDecimal totalFatG;

    @Column(name = "saturated_fat_g", precision = 6, scale = 2)
    private BigDecimal saturatedFatG;

    @Column(name = "trans_fat_g", precision = 6, scale = 2)
    private BigDecimal transFatG;

    @Column(name = "cholesterol_mg", precision = 6, scale = 2)
    private BigDecimal cholesterolMg;

    @Column(name = "sodium_mg", precision = 6, scale = 2)
    private BigDecimal sodiumMg;

    @Column(name = "dietary_fiber_g", precision = 6, scale = 2)
    private BigDecimal dietaryFiberG;

    @Column(name = "ingredients", columnDefinition = "TEXT", nullable = false)
    private String ingredients;

    @Column(name = "serving_size", length = 50)
    private String servingSize;

    @Column(name = "nutrition_basis", length = 20)
    private String nutritionBasis; // PER_100_G / PER_SERVING
}
