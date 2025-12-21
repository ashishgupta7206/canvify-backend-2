package com.canvify.test.service.inventory;

import com.canvify.test.model.ApiResponse;

public interface InventoryService {

    ApiResponse<?> addStock(Long variantId, Integer qty, String remarks);

    ApiResponse<?> reduceStock(Long variantId, Integer qty, String remarks, Long referenceId);

    ApiResponse<?> reserveStock(Long variantId, Integer qty, Long referenceId);

    ApiResponse<?> releaseReservedStock(Long variantId, Integer qty, Long referenceId);

    ApiResponse<?> getStock(Long variantId);

    void adjustStockFromLedger(Long variantId);

    //system generated release reserved stock
    void systemReleaseReservedStock(Long variantId, int qty, Long orderId);
}