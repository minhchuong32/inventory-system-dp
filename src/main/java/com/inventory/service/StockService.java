package com.inventory.service;
import com.inventory.entity.StockMovement;
import com.inventory.enums.MovementType;
import org.springframework.data.domain.Page;
public interface StockService {
    void recordMovement(Long productId, Long warehouseId, MovementType type,
                        int qty, int before, int after, String refCode, String refType, String note);
    Page<StockMovement> getMovementHistory(Long productId, int page, int size);
}
