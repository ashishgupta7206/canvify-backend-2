package com.canvify.test.controller;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.shipment.CreateShipmentRequest;
import com.canvify.test.request.shipment.UpdateShipmentStatusRequest;
import com.canvify.test.service.shipment.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CreateShipmentRequest req) {
        return shipmentService.createShipment(req);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateShipmentStatusRequest req
    ) {
        return shipmentService.updateShipmentStatus(id, req);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        return shipmentService.getShipment(id);
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<?> listByOrder(@PathVariable Long orderId) {
        return shipmentService.getShipmentsForOrder(orderId);
    }

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return shipmentService.list(page, size);
    }
}