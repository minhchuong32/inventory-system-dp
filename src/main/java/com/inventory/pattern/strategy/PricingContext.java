package com.inventory.pattern.strategy;

import com.inventory.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * STRATEGY PATTERN – Context.
 * Chọn PricingStrategy phù hợp dựa trên loại khách hàng,
 * rồi ủy quyền tính toán cho strategy đó.
 */

@Component
@Slf4j
public class PricingContext {
    private final List<PricingStrategy> allStrategies;

    public PricingContext(List<PricingStrategy> allStrategies){
        this.allStrategies = allStrategies;
    }

    /**
     * Chọn strategy theo customerType của khách hàng.
     */
    public PricingStrategy resolveStrategy(Customer customer) {
        if (customer == null) return getDefaultStrategy();

        return allStrategies.stream()
                .filter(s -> s.supports(customer.getCustomerType()))
                .findFirst()
                .orElse(getDefaultStrategy());
    }

    private PricingStrategy getDefaultStrategy() {
        return allStrategies.stream()
                .filter(s -> s.supports(PricingType.RETAIL.name()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Retail strategy not found"));
    }

    /**
     * Tính giá áp dụng chiến lược phù hợp.
     */
    public BigDecimal applyPricing(Customer customer, BigDecimal basePrice, int quantity) {
        PricingStrategy strategy = resolveStrategy(customer);
        BigDecimal total = customer != null ? customer.getTotalPurchase() : BigDecimal.ZERO;
        BigDecimal result = strategy.calculatePrice(basePrice, quantity, total);
        log.debug("Pricing [{}]: base={} qty={} → final={}",
            strategy.getStrategyName(), basePrice, quantity, result);
        return result;
    }

    /**
     * Lấy % chiết khấu để điền vào ExportDetail.discountPercent.
     */
    public BigDecimal getDiscountPercent(Customer customer, int quantity) {
        PricingStrategy strategy = resolveStrategy(customer);
        BigDecimal total = customer != null ? customer.getTotalPurchase() : BigDecimal.ZERO;
        return strategy.getDiscountPercent(quantity, total);
    }
}
