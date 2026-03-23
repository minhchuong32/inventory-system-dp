package com.inventory.controller;

import com.inventory.dto.DashboardStatsDto;
import com.inventory.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProductService productService;
    private final ReportService reportService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        DashboardStatsDto stats = reportService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("lowStockProducts", productService.findLowStockProducts());
        model.addAttribute("activePage", "dashboard");

        int year = LocalDate.now().getYear();
        List<Long> importCounts  = reportService.getMonthlyImportCounts(year);
        List<Long> exportCounts  = reportService.getMonthlyExportCounts(year);
        List<BigDecimal> importAmounts = reportService.getMonthlyImportAmounts(year);
        List<BigDecimal> exportAmounts = reportService.getMonthlyExportAmounts(year);
        model.addAttribute("importCounts",  importCounts);
        model.addAttribute("exportCounts",  exportCounts);
        model.addAttribute("importAmounts", importAmounts);
        model.addAttribute("exportAmounts", exportAmounts);
        model.addAttribute("year", year);
        return "dashboard";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}
