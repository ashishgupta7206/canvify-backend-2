package com.canvify.test.response.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtResponse {

    private String accessToken;
    private final String tokenType = "Bearer";

    public JwtResponse(String accessToken) {
        this.accessToken = accessToken;
    }
}
