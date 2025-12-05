package com.canvify.test.request.delivery;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeliveryPartnerCreateRequest {

    @NotBlank
    private String name;

    @Size(max = 50)
    private String mobile;

    private String serviceArea;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    private BigDecimal commissionPercent;
}