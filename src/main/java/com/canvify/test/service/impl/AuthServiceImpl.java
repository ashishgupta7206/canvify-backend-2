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
import com.canvify.test.response.auth.AuthRegisterResponse;
import com.canvify.test.response.auth.JwtResponse;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.JwtTokenProvider;
import com.canvify.test.security.UserContext;
import com.canvify.test.service.AuthService;
import com.canvify.test.utility.OtpUtil;
import jakarta.transaction.Transactional;
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
    @Autowired
    private UserContext userContext;

    // ------------------------
    // PASSWORD LOGIN
    // ------------------------
    @Override
    public ApiResponse<?> login(LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        JwtResponse jwt = buildJwtResponse(user, tokenProvider.generateToken(auth));

        return ApiResponse.success(jwt, "Login successful");
    }

    public JwtResponse loginForregister(LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(auth);

        User user = ((CustomUserDetails) auth.getPrincipal()).getUser();
        JwtResponse jwt = buildJwtResponse(user, tokenProvider.generateToken(auth));

        return jwt;
    }

    // ------------------------
    // SEND OTP
    // ------------------------
    @Override
    public ApiResponse<?> sendOtp(OtpRequest req) {

        User user = findUser(req.getIdentifier());

        String otp = OtpUtil.generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        System.out.println("OTP: " + otp);

        return ApiResponse.success(null, "OTP sent successfully");
    }

    // ------------------------
    // OTP LOGIN
    // ------------------------
    @Override
    public JwtResponse otpLogin(OtpLoginRequest req) {

        User user = findUser(req.getIdentifier());

        validateOtp(user, req.getOtp());

        // Clear OTP after successful login
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);

        return buildJwtResponse(user, tokenProvider.generateToken(auth));
    }

//    // ------------------------
//    // REGISTRATION
//    // ------------------------
//    @Override
//    @Transactional
//    public ApiResponse<?> register(RegistrationRequest req) {
//
//        validateRegistration(req);
//
//        String username = generateNextUsername();
//
//        Role role = roleRepository.findByName("ROLE_USER")
//                .orElseThrow(() -> new RuntimeException("Role missing"));
//
//        User user = new User();
//        user.setName(req.getName());
//        user.setUsername(username);
//        user.setEmail(blankToNull(req.getEmail()));
//        user.setMobileNumber(blankToNull(req.getMobileNumber()));
//        user.setPassword(passwordEncoder.encode(req.getPassword()));
//        user.setRole(role);
//
//        User saved = userRepository.save(user);
//
//        return ApiResponse.success(
//                AuthRegisterResponse.builder()
//                        .id(saved.getId())
//                        .username(saved.getUsername())
//                        .name(saved.getName())
//                        .email(saved.getEmail())
//                        .mobileNumber(saved.getMobileNumber())
//                        .role(saved.getRole())
//                        .build(),
//                "User registered successfully"
//        );
//    }

    @Override
    @Transactional
    public ApiResponse<?> register(RegistrationRequest req) {

        validateRegistration(req);

        String username = generateNextUsername();

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role missing"));

        User user = new User();
        user.setName(req.getName());
        user.setUsername(username);
        user.setEmail(blankToNull(req.getEmail()));
        user.setMobileNumber(blankToNull(req.getMobileNumber()));
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(role);

        User saved = userRepository.save(user);

        LoginRequest lr = new LoginRequest();
        lr.setPassword(req.getPassword());
        if (req.getEmail() != null && !req.getEmail().isBlank()) {
            lr.setIdentifier(req.getEmail());
        } else {
            lr.setIdentifier(req.getMobileNumber());
        }

        JwtResponse jwt = loginForregister(lr);

        return ApiResponse.success(jwt, "User registered & logged in successfully");
    }


    // ------------------------------------------------------------
    // PRIVATE METHODS BELOW — CLEAN ARCHITECTURE
    // ------------------------------------------------------------

    private User findUser(String identifier) {
        return userRepository.findByUsernameOrEmailOrMobileNumber(identifier, identifier, identifier)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateOtp(User user, String otp) {

        if (user.getOtp() == null || !user.getOtp().equals(otp))
            throw new RuntimeException("Invalid OTP");

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired");
    }

    private void validateRegistration(RegistrationRequest req) {

        if (isBlank(req.getEmail()) && isBlank(req.getMobileNumber()))
            throw new RuntimeException("Email or Mobile is required");

        if (!isBlank(req.getEmail()) && userRepository.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email already exists");

        if (!isBlank(req.getMobileNumber()) && userRepository.existsByMobileNumber(req.getMobileNumber()))
            throw new RuntimeException("Mobile already exists");
    }

    private String generateNextUsername() {
        String last = userRepository.findTopByOrderByIdDesc()
                .map(User::getUsername)
                .orElse("AURA000");
        int next = Integer.parseInt(last.substring(4)) + 1;
        return String.format("AURA%03d", next);
    }

    private JwtResponse buildJwtResponse(User user, String token) {
        return JwtResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole().getName())
                .build();
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private String blankToNull(String s) {
        return isBlank(s) ? null : s;
    }
}