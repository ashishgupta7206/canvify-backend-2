package com.canvify.test.security;

import com.canvify.test.entity.Role;
import com.canvify.test.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    // 🔥 prevent serialization issues (VERY IMPORTANT)
    private final transient User user;

    public CustomUserDetails(Long id,
                             String username,
                             String password,
                             Collection<? extends GrantedAuthority> authorities,
                             User user) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.user = user;
    }
    public CustomUserDetails(User user) {
        this(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities(), // or build authorities from role
                user
        );
    }

    public static CustomUserDetails create(User user) {
        Role role = user.getRole();

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(role.getName())
        );

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities,
                user
        );
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // 🔥 prevents logging password accidentally
    @Override
    public String toString() {
        return "CustomUserDetails{id=" + id + ", username='" + username + "'}";
    }
}
