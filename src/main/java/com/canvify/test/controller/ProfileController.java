package com.canvify.test.controller;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.profile.ProfileRequest;
import com.canvify.test.service.profile.ProfileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<ProfileDTO>> getProfile() {
        return ResponseEntity.ok(
                ApiResponse.success(profileService.getProfile(), "Profile fetched successfully")
        );
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ProfileDTO>> updateProfile(
            @Valid @RequestBody ProfileRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(profileService.updateProfile(request), "Profile updated successfully")
        );
    }
}
