package com.canvify.test.service.delivery;

import com.canvify.test.dto.delivery.DeliveryPartnerDTO;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.delivery.DeliveryPartnerCreateRequest;
import com.canvify.test.request.delivery.DeliveryPartnerUpdateRequest;

public interface DeliveryPartnerService {

    ApiResponse<?> create(DeliveryPartnerCreateRequest req);

    ApiResponse<?> update(Long id, DeliveryPartnerUpdateRequest req);

    ApiResponse<?> delete(Long id);

    ApiResponse<?> get(Long id);

    ApiResponse<?> list(int page, int size);
}