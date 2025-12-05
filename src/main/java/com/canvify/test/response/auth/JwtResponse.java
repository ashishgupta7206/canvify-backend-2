package com.canvify.test.response.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {

    private String accessToken;
    private String tokenType;      // "Bearer"
    private String username;
    private String name;
    private String email;
    private String mobileNumber;
    private String role;
}
