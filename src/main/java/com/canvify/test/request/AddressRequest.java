package com.canvify.test.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String mobile;

    @NotBlank
    private String pincode;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String addressLine1;

    private String addressLine2;

    private String landmark;

    @NotBlank
    private String addressType;
}
