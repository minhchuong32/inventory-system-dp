package com.inventory.pattern.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * STRATEGY PATTERN – Concrete Strategy: Bán sỉ.
 * Giảm giá theo số lượng:
 *   >= 10  → 5%
 *   >= 50  → 10%
 *   >= 100 → 15%
 */
@Component("wholesalePricing")
public class WholesalePricingStrategy implements PricingStrategy {

    @Override
    public String getStrategyName() { return PricingType.WHOLESALE.name(); }

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity, BigDecimal totalPurchase) {
        BigDecimal discount = getDiscountPercent(quantity, totalPurchase);
        BigDecimal multiplier = BigDecimal.ONE.subtract(discount.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return basePrice.multiply(multiplier).setScale(0, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getDiscountPercent(int quantity, BigDecimal totalPurchase) {
        if (quantity >= 100) return BigDecimal.valueOf(15);
        if (quantity >= 50)  return BigDecimal.valueOf(10);
        if (quantity >= 10)  return BigDecimal.valueOf(5);
        return BigDecimal.ZERO;
    }
    
    @Override
public boolean supports(String customerType) {
    return customerType == null || PricingType.WHOLESALE.name().equalsIgnoreCase(customerType);
}
}
