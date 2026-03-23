package com.inventory.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

/**
 * OBSERVER PATTERN – Concrete Observer.
 * Lắng nghe sự kiện tồn kho và ghi cảnh báo
 * khi sản phẩm sắp hết hoặc hết hàng.
 */
@Component
@Slf4j
public class LowStockAlertObserver implements StockEventObserver {

    private final StockEventPublisher publisher;

    public LowStockAlertObserver(StockEventPublisher publisher) {
        this.publisher = publisher;
    }

    @PostConstruct
    public void register() {
        publisher.subscribe(this);
    }

    @Override
    public void onStockChanged(StockEvent event) {
        if (event.isOutOfStockTriggered()) {
            log.warn("[OUT-OF-STOCK ALERT] Sản phẩm '{}' ({}) đã HẾT HÀNG! Ref: {}",
                event.getProduct().getName(),
                event.getProduct().getCode(),
                event.getReferenceCode());
        } else if (event.isLowStockTriggered()) {
            log.warn("[LOW-STOCK ALERT] Sản phẩm '{}' ({}) sắp hết: {} (min: {}). Ref: {}",
                event.getProduct().getName(),
                event.getProduct().getCode(),
                event.getAfterQuantity(),
                event.getProduct().getMinQuantity(),
                event.getReferenceCode());
        }
    }
}
