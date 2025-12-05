package com.canvify.test.service.inventory;

import com.canvify.test.entity.ProductVariant;
import com.canvify.test.entity.StockLedger;
import com.canvify.test.enums.StockChangeType;
import com.canvify.test.enums.StockReferenceType;
import com.canvify.test.repository.ProductVariantRepository;
import com.canvify.test.repository.StockLedgerRepository;
import com.canvify.test.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductVariantRepository variantRepo;
    private final StockLedgerRepository ledgerRepo;

    @Override
    public ApiResponse<?> addStock(Long variantId, Integer qty, String remarks) {

        ProductVariant variant = getVariant(variantId);

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);
        ledger.setQuantityChange(qty);
        ledger.setRemarks(remarks);
        ledger.setChangeType(StockChangeType.INBOUND);
        ledger.setReferenceType(StockReferenceType.PURCHASE);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success("Stock added successfully");
    }

    @Override
    public ApiResponse<?> reduceStock(Long variantId, Integer qty, String remarks, Long referenceId) {

        ProductVariant variant = getVariant(variantId);

        if (variant.getStockQty() < qty) {
            return ApiResponse.error("Insufficient stock");
        }

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);
        ledger.setQuantityChange(-qty);
        ledger.setRemarks(remarks);
        ledger.setChangeType(StockChangeType.SALE);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(StockReferenceType.ORDER);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success("Stock reduced successfully");
    }

    @Override
    public ApiResponse<?> reserveStock(Long variantId, Integer qty, Long referenceId) {

        ProductVariant variant = getVariant(variantId);

        if (variant.getStockQty() < qty) {
            return ApiResponse.error("Insufficient stock");
        }

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);
        ledger.setQuantityChange(-qty);
        ledger.setChangeType(StockChangeType.RESERVATION);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(StockReferenceType.ORDER);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success("Stock reserved");
    }

    @Override
    public ApiResponse<?> releaseReservedStock(Long variantId, Integer qty, Long referenceId) {

        ProductVariant variant = getVariant(variantId);

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);
        ledger.setQuantityChange(qty);
        ledger.setChangeType(StockChangeType.CANCEL_RESERVATION);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(StockReferenceType.ORDER);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success("Reserved stock released");
    }

    @Override
    public ApiResponse<?> getStock(Long variantId) {

        ProductVariant variant = getVariant(variantId);

        return ApiResponse.success(variant.getStockQty());
    }

    @Override
    public void adjustStockFromLedger(Long variantId) {

        List<StockLedger> ledgerList = ledgerRepo.findByProductVariantIdOrderByCreatedDateDesc(variantId);

        int total = ledgerList.stream()
                .mapToInt(StockLedger::getQuantityChange)
                .sum();

        ProductVariant variant = getVariant(variantId);
        variant.setStockQty(total);

        variantRepo.save(variant);
    }

    private ProductVariant getVariant(Long id) {
        return variantRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
    }
}
