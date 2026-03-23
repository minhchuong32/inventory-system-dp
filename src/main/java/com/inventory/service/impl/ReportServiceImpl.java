package com.inventory.service.impl;

import com.inventory.dto.DashboardStatsDto;
import com.inventory.repository.*;
import com.inventory.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CustomerRepository customerRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportOrderRepository exportOrderRepository;

    @Override
    public DashboardStatsDto getDashboardStats() {
        LocalDate now = LocalDate.now();
        int year = now.getYear(), month = now.getMonthValue();
        List<com.inventory.entity.Product> lowStock = productRepository.findLowStockProducts();
        List<com.inventory.entity.Product> outStock = productRepository.findOutOfStockProducts();
        BigDecimal stockValue = productRepository.getTotalStockValue();
        BigDecimal importAmt = importOrderRepository.sumAmountByYearAndMonth(year, month);
        BigDecimal exportAmt = exportOrderRepository.sumAmountByYearAndMonth(year, month);
        return DashboardStatsDto.builder()
            .totalProducts(productRepository.countByStatusTrueAndDeletedFalse())
            .totalSuppliers(supplierRepository.countByStatusTrueAndDeletedFalse())
            .totalCustomers(customerRepository.countByStatusTrueAndDeletedFalse())
            .importCountThisMonth(importOrderRepository.countByYearAndMonth(year, month))
            .exportCountThisMonth(exportOrderRepository.countByYearAndMonth(year, month))
            .importAmountThisMonth(importAmt != null ? importAmt : BigDecimal.ZERO)
            .exportAmountThisMonth(exportAmt != null ? exportAmt : BigDecimal.ZERO)
            .lowStockCount(lowStock.size())
            .outOfStockCount(outStock.size())
            .totalStockValue(stockValue != null ? stockValue : BigDecimal.ZERO)
            .build();
    }

    @Override
    public List<Long> getMonthlyImportCounts(int year) {
        List<Long> counts = new ArrayList<>();
        for (int m = 1; m <= 12; m++) counts.add(importOrderRepository.countByYearAndMonth(year, m));
        return counts;
    }

    @Override
    public List<Long> getMonthlyExportCounts(int year) {
        List<Long> counts = new ArrayList<>();
        for (int m = 1; m <= 12; m++) counts.add(exportOrderRepository.countByYearAndMonth(year, m));
        return counts;
    }

    @Override
    public List<BigDecimal> getMonthlyImportAmounts(int year) {
        List<BigDecimal> amounts = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            BigDecimal v = importOrderRepository.sumAmountByYearAndMonth(year, m);
            amounts.add(v != null ? v : BigDecimal.ZERO);
        }
        return amounts;
    }

    @Override
    public List<BigDecimal> getMonthlyExportAmounts(int year) {
        List<BigDecimal> amounts = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            BigDecimal v = exportOrderRepository.sumAmountByYearAndMonth(year, m);
            amounts.add(v != null ? v : BigDecimal.ZERO);
        }
        return amounts;
    }
}
