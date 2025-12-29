package com.canvify.test.request.order;

import com.canvify.test.request.profile.AddressRequest;
import lombok.Data;
import java.util.List;

@Data
public class CreateOrderRequest {

    private Long addressId;    // selected address ID
    private String emailId;
    private AddressRequest address; //optional
    private String couponCode; // optional
    private List<OrderItemRequest> items;
}