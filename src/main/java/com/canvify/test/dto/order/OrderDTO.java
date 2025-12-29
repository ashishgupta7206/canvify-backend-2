package com.canvify.test.dto.order;

import com.canvify.test.enums.OrderStatus;
import com.canvify.test.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDTO {

    private Long id;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private PaymentStatus paymentStatus;

    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;

    private String addressSummary;
    private String accessToken;
    private List<OrderItemDTO> items;
}