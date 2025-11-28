package com.canvify.test.service.impl;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.entity.Role;
import com.canvify.test.entity.User;
import com.canvify.test.repository.RoleRepository;
import com.canvify.test.repository.UserRepository;
import com.canvify.test.request.auth.LoginRequest;
import com.canvify.test.request.auth.OtpLoginRequest;
import com.canvify.test.request.auth.OtpRequest;
import com.canvify.test.request.auth.RegistrationRequest;
import com.canvify.test.response.auth.JwtResponse;
import com.canvify.test.security.JwtTokenProvider;
import com.canvify.test.service.AuthService;
import com.canvify.test.utility.OtpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    // ------------------------
    // PASSWORD LOGIN
    // ------------------------
    @Override
    public JwtResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        return JwtResponse.builder()
                .accessToken(token)
                .build();
    }

    // ------------------------
    // SEND OTP (email or mobile)
    // ------------------------
    @Override
    public ApiResponse<?> sendOtp(OtpRequest request) {

        User user = userRepository
                .findByUsernameOrEmailOrMobileNumber(
                        request.getIdentifier(),
                        request.getIdentifier(),
                        request.getIdentifier()
                )
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = OtpUtil.generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        // TODO: Integrate SMS/Email sending
        System.out.println("OTP for " + user.getUsername() + " is " + otp);

        return ApiResponse.success(null, "OTP sent successfully");
    }

    // ------------------------
    // OTP LOGIN
    // ------------------------
    @Override
    public JwtResponse otpLogin(OtpLoginRequest request) {

        User user = userRepository
                .findByUsernameOrEmailOrMobileNumber(
                        request.getIdentifier(),
                        request.getIdentifier(),
                        request.getIdentifier()
                )
                .orElseThrow(() -> new RuntimeException("Invalid identifier"));

        if (user.getOtp() == null || !user.getOtp().equals(request.getOtp()))
            throw new RuntimeException("Invalid OTP");

        if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired");

        // Clear OTP after use
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = tokenProvider.generateToken(authentication);

        return JwtResponse.builder()
                .accessToken(token)
                .build();
    }

    // ------------------------
    // REGISTRATION
    // ------------------------
    @Override
    public ApiResponse<?> register(RegistrationRequest req) {

        if (userRepository.existsByEmail(req.getEmail()))
            return ApiResponse.error("Email already in use");

        if (userRepository.existsByMobileNumber(req.getMobileNumber()))
            return ApiResponse.error("Mobile already in use");

        // AUTO-GENERATE USERNAME: AURA001
        String last = userRepository.findTopByOrderByIdDesc()
                .map(User::getUsername)
                .orElse("AURA000");

        int num = Integer.parseInt(last.substring(4));
        String newUsername = String.format("AURA%03d", num + 1);

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER missing"));

        User user = new User();
        user.setName(req.getName());
        user.setUsername(newUsername);
        user.setEmail(req.getEmail());
        user.setMobileNumber(req.getMobileNumber());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);

        userRepository.save(user);

        return ApiResponse.success(null, "User registered successfully");
    }
}