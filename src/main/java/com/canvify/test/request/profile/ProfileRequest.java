package com.canvify.test.request.profile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileRequest {

    @NotBlank
    @Size(min = 3, max = 40)
    private String name;

    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @Size(max = 50)
    private String mobileNumber;
}
