package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.ProfileRequest;
import com.canvify.test.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;

public interface ProfileService {
    ProfileDTO getProfile(CustomUserDetails currentUser);
    ResponseEntity<ApiResponse<?>> updateProfile(CustomUserDetails currentUser, ProfileRequest profileRequest);
}
