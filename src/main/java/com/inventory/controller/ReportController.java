package com.inventory.controller;

import com.inventory.repository.ProductRepository;
import com.inventory.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller @RequestMapping("/reports") @RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;
    private final ProductRepository productRepository;

    @GetMapping
    public String report(Model model, @RequestParam(defaultValue="0") int year) {
        if (year == 0) year = LocalDate.now().getYear();
        model.addAttribute("importCounts",  reportService.getMonthlyImportCounts(year));
        model.addAttribute("exportCounts",  reportService.getMonthlyExportCounts(year));
        model.addAttribute("importAmounts", reportService.getMonthlyImportAmounts(year));
        model.addAttribute("exportAmounts", reportService.getMonthlyExportAmounts(year));
        model.addAttribute("year", year);
        model.addAttribute("allProducts", productRepository.findByStatusTrueAndDeletedFalse());
        model.addAttribute("lowStockProducts", productRepository.findLowStockProducts());
        model.addAttribute("stats", reportService.getDashboardStats());
        model.addAttribute("activePage", "reports");
        return "report/index";
    }
}
