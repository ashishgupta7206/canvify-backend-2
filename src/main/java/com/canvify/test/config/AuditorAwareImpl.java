package com.canvify.test.config;

import com.canvify.test.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser") ||
                !(authentication.getPrincipal() instanceof User)) {

            return Optional.of("system");
        }

        User user = (User) authentication.getPrincipal();

        return Optional.ofNullable(user.getUsername());
    }
}
