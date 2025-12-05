package com.canvify.test.request.order;

import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {

    private Long addressId;    // selected address ID
    private String couponCode; // optional
    private List<OrderItemRequest> items;
}