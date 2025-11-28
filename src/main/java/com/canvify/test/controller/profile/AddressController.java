package com.canvify.test.controller.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.profile.AddressRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.service.profile.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponse<List<AddressDTO>>> getAddresses(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(ApiResponse.success(addressService.getAddresses(currentUser)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressDTO>> addAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                        @Valid @RequestBody AddressRequest request) {

        return new ResponseEntity<>(ApiResponse.success(addressService.addAddress(currentUser, request), "Address added successfully"), HttpStatus.CREATED);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressDTO>> updateAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @PathVariable Long addressId,
                                           @Valid @RequestBody AddressRequest request) {

        return ResponseEntity.ok(ApiResponse.success(addressService.updateAddress(addressId, currentUser, request), "Address updated successfully"));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponse<Object>> deleteAddress(@AuthenticationPrincipal CustomUserDetails currentUser,
                                           @PathVariable Long addressId) {

        addressService.deleteAddress(addressId, currentUser);

        return ResponseEntity.ok(ApiResponse.success(null, "Address deleted successfully"));
    }
}
