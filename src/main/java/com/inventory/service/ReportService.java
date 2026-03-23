package com.inventory.service;
import com.inventory.dto.DashboardStatsDto;
import java.math.BigDecimal;
import java.util.List;
public interface ReportService {
    DashboardStatsDto getDashboardStats();
    List<Long> getMonthlyImportCounts(int year);
    List<Long> getMonthlyExportCounts(int year);
    List<BigDecimal> getMonthlyImportAmounts(int year);
    List<BigDecimal> getMonthlyExportAmounts(int year);
}
