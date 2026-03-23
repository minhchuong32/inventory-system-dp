package com.inventory.pattern.observer;

import com.inventory.entity.StockMovement;
import com.inventory.entity.Warehouse;
import com.inventory.repository.StockMovementRepository;
import com.inventory.repository.WarehouseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

/**
 * OBSERVER PATTERN – Concrete Observer.
 * Tự động lưu StockMovement vào DB mỗi khi tồn kho thay đổi.
 * Tách biệt trách nhiệm audit khỏi business logic.
 */
@Component
@Slf4j
public class StockMovementAuditObserver implements StockEventObserver {

    private final StockEventPublisher      publisher;
    private final StockMovementRepository  movementRepository;
    private final WarehouseRepository      warehouseRepository;

    public StockMovementAuditObserver(StockEventPublisher publisher,
                                      StockMovementRepository movementRepository,
                                      WarehouseRepository warehouseRepository) {
        this.publisher          = publisher;
        this.movementRepository = movementRepository;
        this.warehouseRepository = warehouseRepository;
    }

    @PostConstruct
    public void register() {
        publisher.subscribe(this);
    }

    @Override
    public void onStockChanged(StockEvent event) {
        Warehouse warehouse = event.getProduct().getWarehouse();

        StockMovement movement = StockMovement.builder()
            .product(event.getProduct())
            .warehouse(warehouse)
            .movementType(event.getMovementType())
            .quantity(event.getQuantityChanged())
            .beforeQuantity(event.getBeforeQuantity())
            .afterQuantity(event.getAfterQuantity())
            .referenceCode(event.getReferenceCode())
            .referenceType(event.getMovementType().name())
            .note("Auto-recorded by Observer at " + event.getOccurredAt())
            .build();

        movementRepository.save(movement);
        log.debug("StockMovement saved for product {}", event.getProduct().getCode());
    }
}
