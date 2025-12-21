package com.canvify.test.service.shipment;

import com.canvify.test.dto.shipment.ShipmentDTO;
import com.canvify.test.entity.Orders;
import com.canvify.test.entity.Shipment;
import com.canvify.test.enums.DeliveryStatus;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.model.Pagination;
import com.canvify.test.repository.OrderRepository;
import com.canvify.test.repository.OrderStatusHistoryRepository;
import com.canvify.test.repository.ShipmentRepository;
import com.canvify.test.request.shipment.CreateShipmentRequest;
import com.canvify.test.request.shipment.UpdateShipmentStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository historyRepository;

    @Override
    @Transactional
    public ApiResponse<?> createShipment(CreateShipmentRequest req) {

        if (shipmentRepository.existsByTrackingNumberAndBitDeletedFlagFalse(req.getTrackingNumber())) {
            return ApiResponse.error("Tracking number already exists");
        }

        Orders order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Shipment shipment = new Shipment();
        shipment.setOrder(order);
        shipment.setCourierName(req.getCourierName());
        shipment.setTrackingNumber(req.getTrackingNumber());
        shipment.setDeliveryStatus(req.getStatus());
        shipment.setShippedOn(LocalDateTime.now());

        shipmentRepository.save(shipment);

        // update order status
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        // history
        addHistory(order, oldStatus, OrderStatus.SHIPPED, "system", "Shipment created");

        return ApiResponse.success(convert(shipment), "Shipment created successfully");
    }

    @Override
    @Transactional
    public ApiResponse<?> updateShipmentStatus(Long shipmentId, UpdateShipmentStatusRequest req) {

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));

        Orders order = shipment.getOrder();

        // update shipment status
        DeliveryStatus oldDeliveryStatus = shipment.getDeliveryStatus();
        shipment.setDeliveryStatus(req.getStatus());

        if (req.getStatus() == DeliveryStatus.DELIVERED) {
            shipment.setDeliveredOn(LocalDateTime.now());
        }

        shipmentRepository.save(shipment);

        // update order status accordingly
        OrderStatus newOrderStatus = mapToOrderStatus(req.getStatus());
        OrderStatus oldOrderStatus = order.getStatus();

        order.setStatus(newOrderStatus);
        orderRepository.save(order);

        // history
        addHistory(order, oldOrderStatus, newOrderStatus, "system", req.getRemark());

        return ApiResponse.success(convert(shipment), "Shipment status updated");
    }

    @Override
    public ApiResponse<?> getShipment(Long shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new RuntimeException("Shipment not found"));
        return ApiResponse.success(convert(shipment));
    }

    @Override
    public ApiResponse<?> getShipmentsForOrder(Long orderId) {
        var list = shipmentRepository.findByOrderIdAndBitDeletedFlagFalse(orderId)
                .stream()
                .map(this::convert)
                .toList();

        return ApiResponse.success(list);
    }

    @Override
    public ApiResponse<?> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Shipment> result = shipmentRepository.findAll(pageable);

        Pagination pagination = new Pagination(result);

        return ApiResponse.success(
                result.getContent().stream().map(this::convert).toList(),
                "Shipments fetched successfully",
                pagination
        );
    }

    private ShipmentDTO convert(Shipment s) {
        ShipmentDTO dto = new ShipmentDTO();
        dto.setId(s.getId());
        dto.setOrderId(s.getOrder().getId());
        dto.setCourierName(s.getCourierName());
        dto.setTrackingNumber(s.getTrackingNumber());
        dto.setShippedOn(s.getShippedOn());
        dto.setDeliveredOn(s.getDeliveredOn());
        dto.setDeliveryStatus(s.getDeliveryStatus());
        return dto;
    }

    private OrderStatus mapToOrderStatus(DeliveryStatus status) {
        return switch (status) {
            case SHIPPED, IN_TRANSIT -> OrderStatus.SHIPPED;
//            case OUT_FOR_DELIVERY -> OrderStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> OrderStatus.DELIVERED;
            case CANCELLED -> OrderStatus.CANCELLED;
            default -> OrderStatus.SHIPPED;
        };
    }

    private void addHistory(Orders order, OrderStatus oldStatus, OrderStatus newStatus, String updatedBy, String remark) {
        var h = new com.canvify.test.entity.OrderStatusHistory();
        h.setOrder(order);
        h.setOldStatus(oldStatus);
        h.setNewStatus(newStatus);
        h.setRemark(remark);
        historyRepository.save(h);
    }
}