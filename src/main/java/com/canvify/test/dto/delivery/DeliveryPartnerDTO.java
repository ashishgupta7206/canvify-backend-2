package com.canvify.test.dto.delivery;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DeliveryPartnerDTO {
    private Long id;
    private String name;
    private String mobile;
    private String serviceArea;
    private BigDecimal commissionPercent;
}