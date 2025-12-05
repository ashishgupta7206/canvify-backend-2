package com.canvify.test.service.delivery;

import com.canvify.test.dto.delivery.DeliveryPartnerDTO;
import com.canvify.test.entity.DeliveryPartner;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.Pagination;
import com.canvify.test.repository.DeliveryPartnerRepository;
import com.canvify.test.request.delivery.DeliveryPartnerCreateRequest;
import com.canvify.test.request.delivery.DeliveryPartnerUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    @Override
    public ApiResponse<?> create(DeliveryPartnerCreateRequest req) {

        if (deliveryPartnerRepository.existsByNameAndBitDeletedFlagFalse(req.getName())) {
            return ApiResponse.error("Delivery partner already exists");
        }

        DeliveryPartner partner = new DeliveryPartner();
        partner.setName(req.getName());
        partner.setMobile(req.getMobile());
        partner.setServiceArea(req.getServiceArea());
        partner.setCommissionPercent(req.getCommissionPercent());

        deliveryPartnerRepository.save(partner);

        return ApiResponse.success(convertToDTO(partner), "Delivery partner added successfully");
    }

    @Override
    public ApiResponse<?> update(Long id, DeliveryPartnerUpdateRequest req) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        partner.setName(req.getName());
        partner.setMobile(req.getMobile());
        partner.setServiceArea(req.getServiceArea());
        partner.setCommissionPercent(req.getCommissionPercent());

        deliveryPartnerRepository.save(partner);

        return ApiResponse.success(convertToDTO(partner), "Delivery partner updated successfully");
    }

    @Override
    public ApiResponse<?> delete(Long id) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        partner.setBitDeletedFlag(true);
        deliveryPartnerRepository.save(partner);

        return ApiResponse.success("Delivery partner deleted successfully");
    }

    @Override
    public ApiResponse<?> get(Long id) {
        DeliveryPartner partner = deliveryPartnerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        return ApiResponse.success(convertToDTO(partner));
    }

    @Override
    public ApiResponse<?> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<DeliveryPartner> result = deliveryPartnerRepository.findAll(pageable);

        Pagination pagination = new Pagination(result);

        return ApiResponse.success(
                result.getContent().stream().map(this::convertToDTO).toList(),
                "Delivery partners fetched successfully",
                pagination
        );
    }

    private DeliveryPartnerDTO convertToDTO(DeliveryPartner partner) {
        DeliveryPartnerDTO dto = new DeliveryPartnerDTO();
        dto.setId(partner.getId());
        dto.setName(partner.getName());
        dto.setMobile(partner.getMobile());
        dto.setServiceArea(partner.getServiceArea());
        dto.setCommissionPercent(partner.getCommissionPercent());
        return dto;
    }
}