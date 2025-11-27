package com.canvify.test.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OtpRequest {

    @NotBlank(message = "Identifier (email or mobile) is required")
    private String identifier; // Can be email or mobile number
}
