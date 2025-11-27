package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.entity.User;
import com.canvify.test.repository.UserRepository;
import com.canvify.test.request.profile.ProfileRequest;
import com.canvify.test.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ProfileDTO updateProfile(CustomUserDetails currentUser, ProfileRequest request) {

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // -------------------------------
        // EMAIL unique validation
        // -------------------------------
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), currentUser.getId())) {
            throw new RuntimeException("Email is already used by another account");
        }

        // -------------------------------
        // MOBILE unique validation
        // -------------------------------
        if (userRepository.existsByMobileNumberAndIdNot(request.getMobileNumber(), currentUser.getId())) {
            throw new RuntimeException("Mobile number already used by another account");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());

        User updatedUser = userRepository.save(user);

        return convertToDTO(updatedUser);
    }

    private ProfileDTO convertToDTO(User user) {
        ProfileDTO dto = new ProfileDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());
        return dto;
    }
}

