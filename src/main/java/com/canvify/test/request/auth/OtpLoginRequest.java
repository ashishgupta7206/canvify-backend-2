package com.canvify.test.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpLoginRequest {

    @NotBlank(message = "Identifier (email or mobile) is required")
    private String identifier;

    @NotBlank(message = "OTP is required")
    private String otp;
}
