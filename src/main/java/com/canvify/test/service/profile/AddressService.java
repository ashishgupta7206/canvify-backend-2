package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.request.profile.AddressRequest;

import java.util.List;

public interface AddressService {

    // GET all addresses for logged-in user
    List<AddressDTO> getAddresses();

    // ADD new address
    AddressDTO addAddress( AddressRequest request);

    // UPDATE existing address
    AddressDTO updateAddress(Long addressId,  AddressRequest request);

    // DELETE address
    void deleteAddress(Long addressId);
}
