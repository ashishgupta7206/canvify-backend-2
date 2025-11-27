package com.canvify.test.response.auth;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    private String token;
    private Long id;
    private String name;
    private String username;
    private String email;
    private String mobileNumber;
    private String role;   // <-- only role name
}
