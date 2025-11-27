package com.canvify.test.controller;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.auth.LoginRequest;
import com.canvify.test.request.auth.OtpLoginRequest;
import com.canvify.test.request.auth.OtpRequest;
import com.canvify.test.request.auth.RegistrationRequest;
import com.canvify.test.response.auth.JwtResponse;
import com.canvify.test.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // REGISTRATION
    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegistrationRequest request) {
        return authService.register(request);
    }

    // PASSWORD LOGIN
    @PostMapping("/login")
    public JwtResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // SEND OTP
    @PostMapping("/otp/send")
    public ApiResponse<?> sendOtp(@Valid @RequestBody OtpRequest request) {
        return authService.sendOtp(request);
    }

    // OTP LOGIN
    @PostMapping("/otp/login")
    public JwtResponse otpLogin(@Valid @RequestBody OtpLoginRequest request) {
        return authService.otpLogin(request);
    }
}

