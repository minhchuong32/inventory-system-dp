package com.inventory.pattern.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * STRATEGY PATTERN – Concrete Strategy: Khách VIP.
 * Giảm giá cố định 20% + thêm theo loyalty:
 *   totalPurchase >= 100M → +5% (tổng 25%)
 *   totalPurchase >= 200M → +10% (tổng 30%)
 */
@Component("vipPricing")
public class VipPricingStrategy implements PricingStrategy {

    private static final BigDecimal BASE_VIP_DISCOUNT = BigDecimal.valueOf(20);

    @Override
    public String getStrategyName() { return PricingType.VIP.name(); }

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity, BigDecimal totalPurchase) {
        BigDecimal discount = getDiscountPercent(quantity, totalPurchase);
        BigDecimal multiplier = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return basePrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getDiscountPercent(int quantity, BigDecimal totalPurchase) {
        BigDecimal extra = BigDecimal.ZERO;
        if (totalPurchase != null) {
            if (totalPurchase.compareTo(BigDecimal.valueOf(200_000_000)) >= 0) extra = BigDecimal.valueOf(10);
            else if (totalPurchase.compareTo(BigDecimal.valueOf(100_000_000)) >= 0) extra = BigDecimal.valueOf(5);
        }
        return BASE_VIP_DISCOUNT.add(extra);
    }
    
    @Override
public boolean supports(String customerType) {
    return customerType == null || PricingType.VIP.name().equalsIgnoreCase(customerType);
}
}
