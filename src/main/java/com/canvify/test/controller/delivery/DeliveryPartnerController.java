package com.canvify.test.controller.delivery;

import com.canvify.test.model.ApiResponse;
import com.canvify.test.request.delivery.DeliveryPartnerCreateRequest;
import com.canvify.test.request.delivery.DeliveryPartnerUpdateRequest;
import com.canvify.test.service.delivery.DeliveryPartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delivery-partners")
@RequiredArgsConstructor
public class DeliveryPartnerController {

    private final DeliveryPartnerService deliveryPartnerService;

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody DeliveryPartnerCreateRequest req) {
        return deliveryPartnerService.create(req);
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Long id, @Valid @RequestBody DeliveryPartnerUpdateRequest req) {
        return deliveryPartnerService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return deliveryPartnerService.delete(id);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        return deliveryPartnerService.get(id);
    }

    @GetMapping
    public ApiResponse<?> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return deliveryPartnerService.list(page, size);
    }
}