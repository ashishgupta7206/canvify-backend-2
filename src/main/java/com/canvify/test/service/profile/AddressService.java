package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.request.profile.AddressRequest;
import com.canvify.test.security.CustomUserDetails;

import java.util.List;

public interface AddressService {

    // GET all addresses for logged-in user
    List<AddressDTO> getAddresses(CustomUserDetails currentUser);

    // ADD new address
    AddressDTO addAddress(CustomUserDetails currentUser, AddressRequest request);

    // UPDATE existing address
    AddressDTO updateAddress(Long addressId, CustomUserDetails currentUser, AddressRequest request);

    // DELETE address
    void deleteAddress(Long addressId, CustomUserDetails currentUser);
}
