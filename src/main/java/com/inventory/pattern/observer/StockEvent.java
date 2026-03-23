package com.inventory.pattern.observer;

import com.inventory.entity.Product;
import com.inventory.enums.MovementType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * OBSERVER PATTERN – Event object.
 * Đóng gói thông tin về sự kiện biến động tồn kho.
 */
@Getter
@Builder
public class StockEvent {
    private final Product       product;
    private final MovementType  movementType;
    private final int           quantityChanged;
    private final int           beforeQuantity;
    private final int           afterQuantity;
    private final String        referenceCode;
    private final LocalDateTime occurredAt;

    public boolean isLowStockTriggered() {
        return afterQuantity <= product.getMinQuantity() && beforeQuantity > product.getMinQuantity();
    }

    public boolean isOutOfStockTriggered() {
        return afterQuantity <= 0 && beforeQuantity > 0;
    }
}
