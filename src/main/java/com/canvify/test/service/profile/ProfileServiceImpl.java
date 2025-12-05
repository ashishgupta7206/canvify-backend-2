package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.ProfileDTO;
import com.canvify.test.entity.User;
import com.canvify.test.repository.UserRepository;
import com.canvify.test.request.profile.ProfileRequest;
import com.canvify.test.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserContext userContext; // 🔥 Add this

    @Override
    public ProfileDTO getProfile() {
        Long userId = userContext.getUserId();  // 🔥 Fetch logged-in user automatically

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDTO(user);
    }

    @Override
    public ProfileDTO updateProfile(ProfileRequest request) {

        Long userId = userContext.getUserId();  // 🔥 No need to pass currentUser

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // -------------------------------
        // EMAIL unique validation
        // -------------------------------
        if (userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new RuntimeException("Email is already used by another account");
        }

        // -------------------------------
        // MOBILE unique validation
        // -------------------------------
        if (userRepository.existsByMobileNumberAndIdNot(request.getMobileNumber(), userId)) {
            throw new RuntimeException("Mobile number already used by another account");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());

        return convertToDTO(userRepository.save(user));
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


