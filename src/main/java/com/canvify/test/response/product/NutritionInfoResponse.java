package com.canvify.test.response.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NutritionInfoResponse {

    private Long id;

    // FSSAI mandatory fields
    private BigDecimal energyKcal;
    private BigDecimal proteinG;
    private BigDecimal carbohydrateG;
    private BigDecimal totalSugarG;
    private BigDecimal addedSugarG;
    private BigDecimal totalFatG;
    private BigDecimal saturatedFatG;
    private BigDecimal transFatG;
    private BigDecimal cholesterolMg;
    private BigDecimal sodiumMg;

    // Optional
    private String ingredients;
    private String servingSize;
}

