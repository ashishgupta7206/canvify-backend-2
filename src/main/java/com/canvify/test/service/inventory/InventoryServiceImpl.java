package com.canvify.test.service.inventory;

import com.canvify.test.entity.ProductVariant;
import com.canvify.test.entity.StockLedger;
import com.canvify.test.entity.User;
import com.canvify.test.enums.StockChangeType;
import com.canvify.test.enums.StockReferenceType;
import com.canvify.test.model.ApiResponse;
import com.canvify.test.repository.ProductVariantRepository;
import com.canvify.test.repository.StockLedgerRepository;
import com.canvify.test.security.CustomUserDetails;
import com.canvify.test.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final ProductVariantRepository variantRepo;
    private final StockLedgerRepository ledgerRepo;
    private final UserContext userContext;

    /* =========================================================
       ADD STOCK (ADMIN / STAFF)
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> addStock(Long variantId, Integer qty, String remarks) {

        CustomUserDetails currentUser = requireUser();

        ProductVariant variant = getVariant(variantId);

        StockLedger ledger = baseLedger(variant, currentUser);
        ledger.setQuantityChange(qty);
        ledger.setRemarks(remarks);
        ledger.setChangeType(StockChangeType.INBOUND);
        ledger.setReferenceType(StockReferenceType.PURCHASE);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success(null,"Stock added successfully");
    }

    /* =========================================================
       REDUCE STOCK (SALE / ADJUSTMENT)
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> reduceStock(Long variantId, Integer qty, String remarks, Long referenceId) {

        CustomUserDetails currentUser = requireUser();

        ProductVariant variant = getVariant(variantId);

        if (variant.getStockQty() < qty) {
            return ApiResponse.error("Insufficient stock");
        }

        StockLedger ledger = baseLedger(variant, currentUser);
        ledger.setQuantityChange(-qty);
        ledger.setRemarks(remarks);
        ledger.setChangeType(StockChangeType.SALE);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(StockReferenceType.ORDER);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success(null,"Stock reduced successfully");
    }

    /* =========================================================
       RESERVE STOCK (CHECKOUT)
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> reserveStock(Long variantId, Integer qty, Long referenceId) {

//        CustomUserDetails currentUser = requireUser();

        ProductVariant variant = getVariant(variantId);

        if (variant.getStockQty() < qty) {
            return ApiResponse.error("Insufficient stock");
        }

        StockLedger ledger = baseLedger(variant);
        ledger.setQuantityChange(-qty);
        ledger.setChangeType(StockChangeType.RESERVATION);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(StockReferenceType.ORDER);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success(null,"Stock reserved");
    }

    /* =========================================================
       RELEASE RESERVED STOCK (CANCEL ORDER)
       ========================================================= */

    @Override
    @Transactional
    public ApiResponse<?> releaseReservedStock(Long variantId, Integer qty, Long referenceId) {
//
//        CustomUserDetails currentUser = requireUser();

        ProductVariant variant = getVariant(variantId);

        StockLedger ledger = baseLedger(variant);
        ledger.setQuantityChange(qty);
        ledger.setChangeType(StockChangeType.CANCEL_RESERVATION);
        ledger.setReferenceId(referenceId);
        ledger.setReferenceType(StockReferenceType.ORDER);

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);

        return ApiResponse.success(null,"Reserved stock released");
    }

    /* =========================================================
       GET STOCK (READ ONLY)
       ========================================================= */

    @Override
    public ApiResponse<?> getStock(Long variantId) {

        ProductVariant variant = getVariant(variantId);
        return ApiResponse.success(variant.getStockQty());
    }

    /* =========================================================
       LEDGER → VARIANT SYNC
       ========================================================= */

    @Override
    public void adjustStockFromLedger(Long variantId) {

        List<StockLedger> ledgerList =
                ledgerRepo.findByProductVariantIdOrderByCreatedDateDesc(variantId);

        int total = ledgerList.stream()
                .mapToInt(StockLedger::getQuantityChange)
                .sum();

        ProductVariant variant = getVariant(variantId);
        variant.setStockQty(total);

        variantRepo.save(variant);
    }

    @Override
    @Transactional
    public void systemReleaseReservedStock(
            Long variantId,
            int qty,
            Long orderId
    ) {
        ProductVariant variant = getVariant(variantId);

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);

        ledger.setPerformedBy(null);

        ledger.setQuantityChange(qty);
        ledger.setChangeType(StockChangeType.CANCEL_RESERVATION);
        ledger.setReferenceId(orderId);
        ledger.setReferenceType(StockReferenceType.ORDER);
        ledger.setRemarks("System release due to order expiry");

        ledgerRepo.save(ledger);

        adjustStockFromLedger(variantId);
    }



    /* =========================================================
       HELPERS
       ========================================================= */

    private ProductVariant getVariant(Long id) {
        return variantRepo.findByIdAndBitDeletedFlagFalse(id)
                .orElseThrow(() -> new RuntimeException("Variant not found"));
    }

    private CustomUserDetails requireUser() {
        CustomUserDetails user = userContext.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("Unauthorized inventory operation");
        }
        return user;
    }

    private StockLedger baseLedger(ProductVariant variant, CustomUserDetails userDetails) {

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);

        User u = new User();
        u.setId(userDetails.getId());
        ledger.setPerformedBy(u);

        return ledger;
    }

    private StockLedger baseLedger(ProductVariant variant) {

        StockLedger ledger = new StockLedger();
        ledger.setProductVariant(variant);

        return ledger;
    }
}
