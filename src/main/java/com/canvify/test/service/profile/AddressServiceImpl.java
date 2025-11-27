package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.entity.Address;
import com.canvify.test.entity.User;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.repository.AddressRepository;
import com.canvify.test.repository.UserRepository;
import com.canvify.test.request.AddressRequest;
import com.canvify.test.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<AddressDTO> getAddresses(CustomUserDetails currentUser) {
        List<Address> addresses = addressRepository.findByUserId(currentUser.getId());
        return addresses.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<ApiResponse<?>> addAddress(CustomUserDetails currentUser, AddressRequest addressRequest) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = convertToEntity(addressRequest);
        address.setUser(user);
        addressRepository.save(address);

        ApiResponse<AddressDTO> response = ApiResponse.<AddressDTO>builder()
                .statusCode(HttpStatus.CREATED.value())
                .message("Address added successfully")
                .data(convertToDTO(address))
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponse<?>> updateAddress(Long addressId, AddressRequest addressRequest) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Manual mapping from request to entity
        address.setFullName(addressRequest.getFullName());
        address.setMobile(addressRequest.getMobile());
        address.setPincode(addressRequest.getPincode());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setAddressLine1(addressRequest.getAddressLine1());
        address.setAddressLine2(addressRequest.getAddressLine2());
        address.setLandmark(addressRequest.getLandmark());
        address.setAddressType(addressRequest.getAddressType());

        Address updatedAddress = addressRepository.save(address);

        ApiResponse<AddressDTO> response = ApiResponse.<AddressDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address updated successfully")
                .data(convertToDTO(updatedAddress))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponse<?>> deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        address.setBitDeletedFlag(true);
        addressRepository.save(address);

        ApiResponse<?> response = ApiResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Address deleted successfully")
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private AddressDTO convertToDTO(Address address) {
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setId(address.getId());
        addressDTO.setFullName(address.getFullName());
        addressDTO.setMobile(address.getMobile());
        addressDTO.setPincode(address.getPincode());
        addressDTO.setCity(address.getCity());
        addressDTO.setState(address.getState());
        addressDTO.setAddressLine1(address.getAddressLine1());
        addressDTO.setAddressLine2(address.getAddressLine2());
        addressDTO.setLandmark(address.getLandmark());
        addressDTO.setAddressType(address.getAddressType());
        return addressDTO;
    }

    private Address convertToEntity(AddressRequest addressRequest) {
        Address address = new Address();
        address.setFullName(addressRequest.getFullName());
        address.setMobile(addressRequest.getMobile());
        address.setPincode(addressRequest.getPincode());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setAddressLine1(addressRequest.getAddressLine1());
        address.setAddressLine2(addressRequest.getAddressLine2());
        address.setLandmark(addressRequest.getLandmark());
        address.setAddressType(addressRequest.getAddressType());
        return address;
    }
}
