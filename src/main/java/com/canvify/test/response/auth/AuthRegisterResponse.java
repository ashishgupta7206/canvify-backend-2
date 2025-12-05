package com.canvify.test.response.auth;

import com.canvify.test.entity.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthRegisterResponse {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String mobileNumber;
    private Role role;
}
