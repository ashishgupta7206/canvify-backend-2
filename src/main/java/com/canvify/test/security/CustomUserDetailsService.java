package com.canvify.test.security;

import com.canvify.test.entity.User;
import com.canvify.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        User user = userRepository
                .findByUsernameOrEmailOrMobileNumber(identifier, identifier, identifier)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + identifier)
                );

        return CustomUserDetails.create(user);
    }


    // JWT loads using userId
    @Transactional
    public UserDetails loadUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with id: " + id
                ));

        return CustomUserDetails.create(user);
    }
}
