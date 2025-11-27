package com.canvify.test.controller.profile;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.profile.ProfileRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.service.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .statusCode(200)
                        .message("Profile fetched successfully")
                        .data(profileService.getProfile(currentUser))
                        .build()
        );
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @Valid @RequestBody ProfileRequest request) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .statusCode(200)
                        .message("Profile updated successfully")
                        .data(profileService.updateProfile(currentUser, request))
                        .build()
        );
    }
}

