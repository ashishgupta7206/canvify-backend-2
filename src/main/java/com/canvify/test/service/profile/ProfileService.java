package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.request.profile.ProfileRequest;
import com.canvify.test.security.CustomUserDetails;

public interface ProfileService {

    ProfileDTO getProfile();

    ProfileDTO updateProfile(ProfileRequest request);
}
