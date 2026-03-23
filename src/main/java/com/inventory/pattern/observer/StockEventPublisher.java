package com.inventory.pattern.observer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * OBSERVER PATTERN – Subject (Publisher).
 * Quản lý danh sách observer và publish event.
 * Spring bean singleton → inject vào StockServiceImpl.
 */
@Component
@Slf4j
public class StockEventPublisher {

    private final List<StockEventObserver> observers = new ArrayList<>();

    public void subscribe(StockEventObserver observer) {
        observers.add(observer);
        log.debug("Observer registered: {}", observer.getClass().getSimpleName());
    }

    public void unsubscribe(StockEventObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notify all registered observers with the event.
     */
    public void publish(StockEvent event) {
        log.info("Publishing StockEvent: product={}, type={}, qty={}->{} ref={}",
            event.getProduct().getCode(),
            event.getMovementType(),
            event.getBeforeQuantity(),
            event.getAfterQuantity(),
            event.getReferenceCode());

        observers.forEach(observer -> {
            try {
                observer.onStockChanged(event);
            } catch (Exception e) {
                log.error("Observer {} failed: {}", observer.getClass().getSimpleName(), e.getMessage());
            }
        });
    }
}
