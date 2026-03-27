package com.inventory.pattern.strategy;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * STRATEGY PATTERN – Concrete Strategy: Bán lẻ.
 * Giá niêm yết, không chiết khấu.
 */
@Component("retailPricing")
public class RetailPricingStrategy implements PricingStrategy {

    @Override
    public String getStrategyName() { return PricingType.RETAIL.name(); }

    @Override
    public BigDecimal calculatePrice(BigDecimal basePrice, int quantity, BigDecimal totalPurchase) {
        return basePrice; // no discount
    }

    @Override
    public BigDecimal getDiscountPercent(int quantity, BigDecimal totalPurchase) {
        return BigDecimal.ZERO;
    }

    @Override
public boolean supports(String customerType) {
    return customerType == null || PricingType.RETAIL.name().equalsIgnoreCase(customerType);
}
}
