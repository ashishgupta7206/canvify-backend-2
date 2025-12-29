package com.canvify.test.security;

import com.canvify.test.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    public CustomUserDetails getCurrentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();

        // ✅ Normal case (best)
        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        // ✅ VERY IMPORTANT fallback
        if (principal instanceof User) {
            return new CustomUserDetails((User) principal);
        }

        // ❌ Anonymous or unsupported
        return null;
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
