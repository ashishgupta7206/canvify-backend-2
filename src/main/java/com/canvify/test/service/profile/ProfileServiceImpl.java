package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.entity.User;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.repository.UserRepository;
import com.canvify.test.request.ProfileRequest;
import com.canvify.test.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public ProfileDTO getProfile(CustomUserDetails currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }

    @Override
    public ResponseEntity<ApiResponse<?>> updateProfile(CustomUserDetails currentUser, ProfileRequest profileRequest) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(profileRequest.getName());
        user.setEmail(profileRequest.getEmail());
        user.setMobileNumber(profileRequest.getMobileNumber());

        User updatedUser = userRepository.save(user);

        ApiResponse<ProfileDTO> response = ApiResponse.<ProfileDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Profile updated successfully")
                .data(convertToDTO(updatedUser))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ProfileDTO convertToDTO(User user) {
        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setId(user.getId());
        profileDTO.setName(user.getName());
        profileDTO.setUsername(user.getUsername());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setMobileNumber(user.getMobileNumber());
        return profileDTO;
    }
}
