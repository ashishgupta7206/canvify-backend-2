package com.canvify.test.config;

import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.UserContext;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    private final UserContext userContext;

    public AuditorAwareImpl(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public Optional<String> getCurrentAuditor() {

        CustomUserDetails user = userContext.getCurrentUser();

        if (user != null) {
            return Optional.of(user.getUsername());
        }

        return Optional.of("SYSTEM");
    }
}
