package com.canvify.test.service.delivery;

import com.canvify.test.dto.delivery.DeliveryAssignmentDTO;
import com.canvify.test.entity.*;
import com.canvify.test.enums.DeliveryAssignmentStatus;
import com.canvify.test.enums.DeliveryStatus;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.Pagination;
import com.canvify.test.repository.*;
import com.canvify.test.request.delivery.AssignDeliveryRequest;
import com.canvify.test.request.delivery.UpdateAssignmentStatusRequest;
import com.canvify.test.integration.PartnerNotificationClient;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentServiceImpl implements DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final PartnerNotificationClient partnerNotificationClient;
    private final ShipmentRepository shipmentRepo;

    @Override
    @Transactional
    public ApiResponse<?> assign(AssignDeliveryRequest req) {

        Orders order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            return ApiResponse.error("Order cannot be assigned at this stage");
        }

        var partner = partnerRepository.findById(req.getPartnerId())
                .orElseThrow(() -> new RuntimeException("Delivery partner not found"));

        // Prevent duplicate assignment for same partner+order
        Optional<DeliveryAssignment> existing = assignmentRepository.findByOrderIdAndPartnerIdAndBitDeletedFlagFalse(order.getId(), partner.getId());
        if (existing.isPresent()) {
            return ApiResponse.error("Assignment already exists for this partner and order");
        }

        // create Shipment if not exists (create minimal shipment record here)
        Shipment shipment = null;
        List<Shipment> shipments = shipmentRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());
        if (shipments.isEmpty()) {
            shipment = new Shipment();
            shipment.setOrder(order);
            shipment.setCourierName(req.getCourierName());
            shipment.setTrackingNumber(req.getTrackingNumber() != null ? req.getTrackingNumber() : UUID.randomUUID().toString());
            shipment.setShippedOn(null);
            shipment.setDeliveryStatus(DeliveryStatus.PENDING);
            shipment = shipmentRepository.save(shipment);
        } else {
            shipment = shipments.get(0);
            if (req.getTrackingNumber() != null) {
                shipment.setTrackingNumber(req.getTrackingNumber());
                shipmentRepository.save(shipment);
            }
        }

        // create assignment
        DeliveryAssignment a = new DeliveryAssignment();
        a.setOrder(order);
        a.setPartner(partner);
        a.setAssignedOn(LocalDateTime.now());
        a.setStatus(DeliveryAssignmentStatus.ASSIGNED);
        assignmentRepository.save(a);

        // update order status to SHIPPED (or OUT_FOR_DELIVERY depending on your flow)
        OrderStatus old = order.getStatus();
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);
        // add history
        addHistory(order, old, order.getStatus(), "system", "Assigned to partner " + partner.getName());

        // notify partner (async in future — here sync call to stub)
        Map<String,Object> payload = new HashMap<>();
        payload.put("assignmentId", a.getId());
        payload.put("orderId", order.getId());
        payload.put("trackingNumber", shipment.getTrackingNumber());
        payload.put("courier", shipment.getCourierName());
        payload.put("pickupAddress", order.getAddress().getAddressLine1());
        payload.put("value", order.getPayableAmount());
        partnerNotificationClient.notifyAssignment(partner.getId(), payload);

        return ApiResponse.success(convert(a), "Assigned to delivery partner");
    }

    @Override
    @Transactional
    public ApiResponse<?> updateStatus(Long assignmentId, UpdateAssignmentStatusRequest req) {
        DeliveryAssignment a = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        DeliveryAssignmentStatus old = a.getStatus();
        a.setStatus(req.getStatus());

        if (req.getStatus() == DeliveryAssignmentStatus.DELIVERED) {
            a.setDeliveredOn(LocalDateTime.now());
        }
        assignmentRepository.save(a);

        // Update shipment & order accordingly
        Optional<Shipment> sOpt = shipmentRepo.findByOrderIdAndBitDeletedFlagFalse(a.getOrder().getId()).stream().findFirst();
        if (sOpt.isPresent()) {
            Shipment s = sOpt.get();
            s.setDeliveryStatus(mapAssignmentToDeliveryStatus(req.getStatus()));
            if (req.getStatus() == DeliveryAssignmentStatus.DELIVERED) {
                s.setDeliveredOn(LocalDateTime.now());
            }
            shipmentRepo.save(s);
        }

        Orders order = a.getOrder();
        OrderStatus oldOrder = order.getStatus();
        order.setStatus(mapAssignmentToOrderStatus(req.getStatus()));
        orderRepository.save(order);

        addHistory(order, oldOrder, order.getStatus(), "system", req.getRemark());

        // notify partner that status update recorded
        Map<String,Object> p = new HashMap<>();
        p.put("assignmentId", a.getId());
        p.put("newStatus", req.getStatus());
        partnerNotificationClient.notifyUpdate(a.getPartner().getId(), p);

        return ApiResponse.success(convert(a), "Assignment status updated");
    }

    @Override
    public ApiResponse<?> get(Long id) {
        DeliveryAssignment a = assignmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Assignment not found"));
        return ApiResponse.success(convert(a));
    }

    @Override
    public ApiResponse<?> listByOrder(Long orderId) {
        List<DeliveryAssignment> list = assignmentRepository.findByOrderIdAndBitDeletedFlagFalse(orderId);
        var dtoList = list.stream().map(this::convert).toList();
        return ApiResponse.success(dtoList);
    }

    @Override
    public ApiResponse<?> listByPartner(Long partnerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<DeliveryAssignment> pageRes = assignmentRepository.findByPartnerIdAndBitDeletedFlagFalse(partnerId, pageable);
        Pagination p = new Pagination(pageRes);
        var dtos = pageRes.getContent().stream().map(this::convert).toList();
        return ApiResponse.success(dtos, "Assignments fetched", p);
    }

    // helpers

    private DeliveryAssignmentDTO convert(DeliveryAssignment a) {
        DeliveryAssignmentDTO d = new DeliveryAssignmentDTO();
        d.setId(a.getId());
        d.setOrderId(a.getOrder().getId());
        d.setPartnerId(a.getPartner().getId());
        d.setPartnerName(a.getPartner().getName());
        d.setStatus(a.getStatus());
        d.setAssignedOn(a.getAssignedOn());
        d.setDeliveredOn(a.getDeliveredOn());
        return d;
    }

    private void addHistory(Orders order, OrderStatus oldStatus, OrderStatus newStatus, String updatedBy, String remark) {
        var h = new com.canvify.test.entity.OrderStatusHistory();
        h.setOrder(order);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setRemark(remark);
        historyRepository.save(h);
    }

    private DeliveryStatus mapAssignmentToDeliveryStatus(DeliveryAssignmentStatus s) {
        return switch (s) {
            case PICKED_UP, IN_TRANSIT -> DeliveryStatus.IN_TRANSIT;
//            case OUT_FOR_DELIVERY -> DeliveryStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> DeliveryStatus.DELIVERED;
            case FAILED, CANCELLED -> DeliveryStatus.CANCELLED;
            default -> DeliveryStatus.IN_TRANSIT;
        };
    }

    private OrderStatus mapAssignmentToOrderStatus(DeliveryAssignmentStatus s) {
        return switch (s) {
            case ASSIGNED, PICKED_UP, IN_TRANSIT -> OrderStatus.SHIPPED;
//            case OUT_FOR_DELIVERY -> OrderStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> OrderStatus.DELIVERED;
            case FAILED, CANCELLED -> OrderStatus.CANCELLED;
            default -> OrderStatus.SHIPPED;
        };
    }
}