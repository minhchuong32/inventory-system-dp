package com.inventory.service.impl;

import com.inventory.entity.*;
import com.inventory.enums.MovementType;
import com.inventory.pattern.observer.*;
import com.inventory.repository.*;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * StockServiceImpl – uses Observer Pattern.
 * Thay vì lưu StockMovement trực tiếp,
 * publish StockEvent và để Observer tự xử lý.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockMovementRepository movementRepository;
    private final ProductRepository       productRepository;
    private final WarehouseRepository     warehouseRepository;
    private final StockEventPublisher     stockEventPublisher;   // Observer

    @Override
    public void recordMovement(Long productId, Long warehouseId, MovementType type,
                               int qty, int before, int after,
                               String refCode, String refType, String note) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // Publish to Observer — StockMovementAuditObserver will save to DB
        stockEventPublisher.publish(StockEvent.builder()
            .product(product)
            .movementType(type)
            .quantityChanged(qty)
            .beforeQuantity(before)
            .afterQuantity(after)
            .referenceCode(refCode)
            .occurredAt(LocalDateTime.now())
            .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockMovement> getMovementHistory(Long productId, int page, int size) {
        return movementRepository.findByProductIdAndDeletedFalseOrderByCreatedAtDesc(
            productId, PageRequest.of(page, size));
    }
}
