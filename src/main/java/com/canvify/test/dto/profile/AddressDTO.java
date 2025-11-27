package com.canvify.test.dto.profile;

import lombok.Data;

@Data
public class AddressDTO {
    private Long id;
    private String fullName;
    private String mobile;
    private String pincode;
    private String city;
    private String state;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String addressType;
}
