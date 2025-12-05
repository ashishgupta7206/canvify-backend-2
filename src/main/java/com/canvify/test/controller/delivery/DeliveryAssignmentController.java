package com.canvify.test.controller.delivery;

import com.canvify.test.request.delivery.AssignDeliveryRequest;
import com.canvify.test.request.delivery.UpdateAssignmentStatusRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.service.delivery.DeliveryAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class DeliveryAssignmentController {

    private final DeliveryAssignmentService assignmentService;

    @PostMapping
    public ApiResponse<?> assign(@Valid @RequestBody AssignDeliveryRequest req) {
        return assignmentService.assign(req);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateAssignmentStatusRequest req) {
        return assignmentService.updateStatus(id, req);
    }

    @GetMapping("/{id}")
    public ApiResponse<?> get(@PathVariable Long id) {
        return assignmentService.get(id);
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<?> listByOrder(@PathVariable Long orderId) {
        return assignmentService.listByOrder(orderId);
    }

    @GetMapping("/partner/{partnerId}")
    public ApiResponse<?> listByPartner(@PathVariable Long partnerId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return assignmentService.listByPartner(partnerId, page, size);
    }
}