package com.canvify.test.controller.profile;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.request.ProfileRequest;
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
    public ProfileDTO getProfile(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return profileService.getProfile(currentUser);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal CustomUserDetails currentUser, @Valid @RequestBody ProfileRequest profileRequest) {
        return profileService.updateProfile(currentUser, profileRequest);
    }
}
