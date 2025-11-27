package com.canvify.test.controller.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.profile.AddressRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.service.profile.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public List<AddressDTO> getAddresses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return addressService.getAddresses(currentUser);
    }

    @PostMapping
    public ResponseEntity<?> addAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                        @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .statusCode(201)
                        .message("Address added successfully")
                        .data(addressService.addAddress(currentUser, request))
                        .build()
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @PathVariable Long addressId,
                                           @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .statusCode(200)
                        .message("Address updated successfully")
                        .data(addressService.updateAddress(addressId, currentUser, request))
                        .build()
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @PathVariable Long addressId) {

        addressService.deleteAddress(addressId, currentUser);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .statusCode(200)
                        .message("Address deleted successfully")
                        .build()
        );
    }
}

