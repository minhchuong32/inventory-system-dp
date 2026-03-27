package com.inventory.pattern.strategy;

import java.math.BigDecimal;

/**
 * STRATEGY PATTERN – Strategy interface.
 * Định nghĩa thuật toán tính giá bán có thể hoán đổi.
 */
public enum PricingType {
    RETAIL,
    WHOLESALE,
    VIP
}
