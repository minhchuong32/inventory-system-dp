package com.inventory.pattern.observer;

import com.inventory.entity.Product;

/**
 * OBSERVER PATTERN – Observer interface.
 * Các class muốn nhận thông báo biến động tồn kho
 * phải implement interface này.
 */
public interface StockEventObserver {
    void onStockChanged(StockEvent event);
}
