package com.canvify.test.service.delivery;

import com.canvify.test.request.delivery.PartnerWebhookRequest;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.entity.DeliveryAssignment;
import com.canvify.test.enums.DeliveryAssignmentStatus;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.repository.DeliveryAssignmentRepository;
import com.canvify.test.repository.OrderRepository;
import com.canvify.test.repository.ShipmentRepository;
import com.canvify.test.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeliveryWorkflowServiceImpl implements DeliveryWorkflowService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Override
    @Transactional
    public ApiResponse<?> handlePartnerWebhook(PartnerWebhookRequest req) {

        DeliveryAssignment asg = assignmentRepository.findById(req.getAssignmentId())
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Idempotency: if same status already recorded, ignore
        if (asg.getStatus() == req.getStatus()) {
            return ApiResponse.success("No-op (same status)");
        }

        // Update assignment
        DeliveryAssignmentStatus old = asg.getStatus();
        asg.setStatus(req.getStatus());
        if (req.getStatus() == DeliveryAssignmentStatus.DELIVERED) {
            asg.setDeliveredOn(LocalDateTime.now());
        }
        assignmentRepository.save(asg);

        // Update shipment (if exists)
        shipmentRepository.findByOrderIdAndBitDeletedFlagFalse(asg.getOrder().getId()).stream().findFirst()
                .ifPresent(shipment -> {
                    shipment.setTrackingNumber(req.getTrackingNumber() != null ? req.getTrackingNumber() : shipment.getTrackingNumber());
                    shipment.setDeliveryStatus(mapAssignmentToDeliveryStatus(req.getStatus()));
                    if (req.getStatus() == DeliveryAssignmentStatus.DELIVERED) {
                        shipment.setDeliveredOn(LocalDateTime.now());
                    }
                    shipmentRepository.save(shipment);
                });

        // Update order + history
        var order = asg.getOrder();
        OrderStatus oldOrder = order.getStatus();
        order.setStatus(mapAssignmentToOrderStatus(req.getStatus()));
        orderRepository.save(order);

        var h = new com.canvify.test.entity.OrderStatusHistory();
        h.setOrder(order);
        h.setOldStatus(oldOrder);
        h.setNewStatus(order.getStatus());
        h.setRemark(req.getRemark());
        historyRepository.save(h);

        return ApiResponse.success("Webhook processed");
    }

    // helpers (similar to previous)
    private com.canvify.test.enums.DeliveryStatus mapAssignmentToDeliveryStatus(DeliveryAssignmentStatus s) {
        return switch (s) {
            case PICKED_UP, IN_TRANSIT -> com.canvify.test.enums.DeliveryStatus.IN_TRANSIT;
//            case OUT_FOR_DELIVERY -> com.canvify.test.enums.DeliveryStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> com.canvify.test.enums.DeliveryStatus.DELIVERED;
            case FAILED, CANCELLED -> com.canvify.test.enums.DeliveryStatus.CANCELLED;
            default -> com.canvify.test.enums.DeliveryStatus.IN_TRANSIT;
        };
    }

    private com.canvify.test.enums.OrderStatus mapAssignmentToOrderStatus(DeliveryAssignmentStatus s) {
        return switch (s) {
            case ASSIGNED, PICKED_UP, IN_TRANSIT -> com.canvify.test.enums.OrderStatus.SHIPPED;
//            case OUT_FOR_DELIVERY -> com.canvify.test.enums.OrderStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> com.canvify.test.enums.OrderStatus.DELIVERED;
            case FAILED, CANCELLED -> com.canvify.test.enums.OrderStatus.CANCELLED;
            default -> com.canvify.test.enums.OrderStatus.SHIPPED;
        };
    }
}