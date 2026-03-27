package com.inventory.pattern.strategy;

import java.math.BigDecimal;

/**
 * STRATEGY PATTERN – Strategy interface.
 * Định nghĩa thuật toán tính giá bán có thể hoán đổi.
 */
public interface PricingStrategy {
    /** Tên chiến lược (dùng cho log/display) */
    String getStrategyName();

    /**
     * Tính đơn giá cuối sau khi áp dụng chiến lược.
     * @param basePrice    giá gốc
     * @param quantity     số lượng mua
     * @param totalPurchase tổng đã mua của khách (cho loyalty)
     */
    BigDecimal calculatePrice(BigDecimal basePrice, int quantity, BigDecimal totalPurchase);

    /** Tính % chiết khấu để hiển thị trên phiếu */
    BigDecimal getDiscountPercent(int quantity, BigDecimal totalPurchase);

    boolean supports(String customerType);
}
