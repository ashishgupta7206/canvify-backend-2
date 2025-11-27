package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.AddressRequest;
import com.canvify.test.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AddressService {
    List<AddressDTO> getAddresses(CustomUserDetails currentUser);
    ResponseEntity<ApiResponse<?>> addAddress(CustomUserDetails currentUser, AddressRequest addressRequest);
    ResponseEntity<ApiResponse<?>> updateAddress(Long addressId, AddressRequest addressRequest);
    ResponseEntity<ApiResponse<?>> deleteAddress(Long addressId);
}
