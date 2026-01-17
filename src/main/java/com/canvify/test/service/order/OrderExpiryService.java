package com.canvify.test.service.order;

import com.canvify.test.entity.OrderItem;
import com.canvify.test.entity.Orders;
import com.canvify.test.enums.OrderStatus;
import com.canvify.test.enums.PaymentStatus;
import com.canvify.test.repository.CouponUsageRepository;
import com.canvify.test.repository.OrderItemRepository;
import com.canvify.test.repository.OrderRepository;
import com.canvify.test.repository.OrderStatusHistoryRepository;
import com.canvify.test.service.inventory.InventoryService;
import com.canvify.test.service.orderstatus.OrderStatusHistoryService;
import com.canvify.test.service.orderstatus.OrderStatusManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final InventoryService inventoryService;
    private final CouponUsageRepository couponUsageRepository;
    private final OrderStatusManager orderStatusManager;

    private static final int EXPIRY_MINUTES = 100;

    @Transactional
    @Scheduled(fixedDelay = 60_00_000) // every 1 minute
    public void expireUnpaidOrders() {

        LocalDateTime expiryTime =
                LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);

        List<Orders> expiredOrders =
                orderRepository.findExpiredOrders(
                        PaymentStatus.PENDING,
                        OrderStatus.PLACED,
                        expiryTime
                );

        for (Orders order : expiredOrders) {
            try {
                expireOrder(order);
            } catch (Exception ex) {
                log.error("Failed to expire order {}", order.getId(), ex);
            }
        }
    }

    private void expireOrder(Orders order) {

        // -----------------------
        // RELEASE STOCK
        // -----------------------
        List<OrderItem> items =
                orderItemRepository.findByOrderIdAndBitDeletedFlagFalse(order.getId());

        for (OrderItem item : items) {
            inventoryService.systemReleaseReservedStock(
                    item.getProductVariant().getId(),
                    item.getQuantity(),
                    order.getId()
            );
        }

        // -----------------------
        // ROLLBACK COUPON
        // -----------------------
        couponUsageRepository
                .findByOrderIdAndBitDeletedFlagFalse(order.getId())
                .ifPresent(usage -> {
                    usage.setBitDeletedFlag(true);
                    couponUsageRepository.save(usage);
                });

        // -----------------------
        // UPDATE ORDER
        // -----------------------
        order.setPaymentStatus(PaymentStatus.FAILED);

        orderStatusManager.changeStatus(order, OrderStatus.CANCELLED, "Order expired");
    }
}


