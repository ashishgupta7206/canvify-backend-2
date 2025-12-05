package com.canvify.test.service.profile;

import com.canvify.test.dto.profile.AddressDTO;
import com.canvify.test.entity.Address;
import com.canvify.test.entity.User;
import com.canvify.test.repository.AddressRepository;
import com.canvify.test.repository.UserRepository;
import com.canvify.test.request.profile.AddressRequest;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserContext userContext;

    @Override
    public List<AddressDTO> getAddresses() {
        Long userId = userContext.getUserId();
        return addressRepository.findByUserIdAndBitDeletedFlagFalse(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AddressDTO addAddress( AddressRequest request) {
        Long userId = userContext.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = convertToEntity(request);
        address.setUser(user);

        return convertToDTO(addressRepository.save(address));
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressRequest request) {

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        CustomUserDetails currentUser = userContext.getCurrentUser();
        if (!(address.getUser().getId().equals(currentUser.getId()) || currentUser.getUser().getRole().equals("ROLE_ADMIN"))) {
            throw new RuntimeException("Unauthorized access");
        }

        address.setFullName(request.getFullName());
        address.setMobile(request.getMobile());
        address.setPincode(request.getPincode());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setAddressType(request.getAddressType());

        return convertToDTO(addressRepository.save(address));
    }

    @Override
    public void deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        CustomUserDetails currentUser = userContext.getCurrentUser();
        if (!address.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        address.setBitDeletedFlag(true);
        addressRepository.save(address);
    }

    private AddressDTO convertToDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setMobile(address.getMobile());
        dto.setPincode(address.getPincode());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setLandmark(address.getLandmark());
        dto.setAddressType(address.getAddressType());
        return dto;
    }

    private Address convertToEntity(AddressRequest request) {
        Address address = new Address();
        address.setFullName(request.getFullName());
        address.setMobile(request.getMobile());
        address.setPincode(request.getPincode());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setAddressType(request.getAddressType());
        return address;
    }
}

