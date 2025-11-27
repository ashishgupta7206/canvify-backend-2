package com.canvify.test.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
