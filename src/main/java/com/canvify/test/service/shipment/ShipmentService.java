package com.canvify.test.service.shipment;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.shipment.CreateShipmentRequest;
import com.canvify.test.request.shipment.UpdateShipmentStatusRequest;

public interface ShipmentService {

    ApiResponse<?> createShipment(CreateShipmentRequest req);

    ApiResponse<?> updateShipmentStatus(Long shipmentId, UpdateShipmentStatusRequest req);

    ApiResponse<?> getShipment(Long shipmentId);

    ApiResponse<?> getShipmentsForOrder(Long orderId);

    ApiResponse<?> list(int page, int size);
}