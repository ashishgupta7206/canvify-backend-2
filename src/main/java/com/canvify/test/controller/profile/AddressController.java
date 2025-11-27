package com.canvify.test.controller.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.request.AddressRequest;
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
    public ResponseEntity<?> addAddress(@AuthenticationPrincipal CustomUserDetails currentUser, @Valid @RequestBody AddressRequest addressRequest) {
        return addressService.addAddress(currentUser, addressRequest);
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<?> updateAddress(@PathVariable Long addressId, @Valid @RequestBody AddressRequest addressRequest) {
        return addressService.updateAddress(addressId, addressRequest);
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long addressId) {
        return addressService.deleteAddress(addressId);
    }
}
