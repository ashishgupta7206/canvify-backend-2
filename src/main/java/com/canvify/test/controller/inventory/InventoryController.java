package com.canvify.test.controller.inventory;

import com.canvify.test.response.ApiResponse;
import com.canvify.test.service.inventory.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/{variantId}/add")
    public ResponseEntity<ApiResponse<?>> addStock(
            @PathVariable Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) String remarks) {

        return ResponseEntity.ok(inventoryService.addStock(variantId, qty, remarks));
    }

    @PostMapping("/{variantId}/reduce")
    public ResponseEntity<ApiResponse<?>> reduceStock(
            @PathVariable Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) String remarks,
            @RequestParam(required = false) Long referenceId) {

        return ResponseEntity.ok(inventoryService.reduceStock(variantId, qty, remarks, referenceId));
    }

    @PostMapping("/{variantId}/reserve")
    public ResponseEntity<ApiResponse<?>> reserveStock(
            @PathVariable Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) Long referenceId) {

        return ResponseEntity.ok(inventoryService.reserveStock(variantId, qty, referenceId));
    }

    @PostMapping("/{variantId}/release")
    public ResponseEntity<ApiResponse<?>> releaseReservedStock(
            @PathVariable Long variantId,
            @RequestParam Integer qty,
            @RequestParam(required = false) Long referenceId) {

        return ResponseEntity.ok(inventoryService.releaseReservedStock(variantId, qty, referenceId));
    }

    @GetMapping("/{variantId}")
    public ResponseEntity<ApiResponse<?>> getStock(@PathVariable Long variantId) {
        return ResponseEntity.ok(inventoryService.getStock(variantId));
    }
}
