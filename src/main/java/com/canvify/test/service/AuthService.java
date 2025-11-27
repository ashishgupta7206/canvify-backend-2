package com.canvify.test.service;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.auth.LoginRequest;
import com.canvify.test.request.auth.OtpLoginRequest;
import com.canvify.test.request.auth.OtpRequest;
import com.canvify.test.request.auth.RegistrationRequest;
import com.canvify.test.response.auth.JwtResponse;

public interface AuthService {

    // PASSWORD LOGIN
    JwtResponse login(LoginRequest request);

    // REGISTRATION
    ApiResponse<?> register(RegistrationRequest request);

    // SEND OTP (email or mobile)
    ApiResponse<?> sendOtp(OtpRequest request);

    // OTP LOGIN
    JwtResponse otpLogin(OtpLoginRequest request);
}
