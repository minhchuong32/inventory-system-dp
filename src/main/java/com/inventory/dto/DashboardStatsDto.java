package com.inventory.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsDto {
    private long totalProducts;
    private long totalSuppliers;
    private long totalCustomers;
    private long importCountThisMonth;
    private long exportCountThisMonth;
    private BigDecimal importAmountThisMonth;
    private BigDecimal exportAmountThisMonth;
    private long lowStockCount;
    private long outOfStockCount;
    private BigDecimal totalStockValue;
}
