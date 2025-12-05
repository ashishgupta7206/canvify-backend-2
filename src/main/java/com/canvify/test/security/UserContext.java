package com.canvify.test.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    public CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) auth.getPrincipal();
    }

    public Long getUserId() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getUser().getId() : null;
    }

    public String getUsername() {
        CustomUserDetails user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public String getRole() {
        CustomUserDetails user = getCurrentUser();
        return (user != null && user.getUser() != null)
                ? user.getUser().getRole().getName() : null;
    }

    public boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
}

