package com.inventory.controller;

import com.inventory.entity.*;
import com.inventory.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.*;

@Controller @RequestMapping("/exports") @RequiredArgsConstructor
public class ExportController {
    private final ExportService exportService;
    private final ProductService productService;
    private final CustomerService customerService;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue="") String search,
                       @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        Page<ExportOrder> orders = exportService.findAll(search, page, size);
        model.addAttribute("orders", orders);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "exports");
        return "export/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return exportService.findById(id).map(order -> {
            model.addAttribute("order", order);
            model.addAttribute("activePage", "exports");
            return "export/detail";
        }).orElseGet(() -> { ra.addFlashAttribute("error","Không tìm thấy phiếu xuất"); return "redirect:/exports"; });
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("order", new ExportOrder());
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("customers", customerService.findAllActive());
        model.addAttribute("activePage", "exports");
        return "export/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required=false) Long customerId,
                       @RequestParam(required=false) String note,
                       @RequestParam(required=false) String deliveryAddress,
                       @RequestParam List<Long> productIds,
                       @RequestParam List<Integer> quantities,
                       @RequestParam List<BigDecimal> unitPrices,
                       @RequestParam(required=false) List<BigDecimal> discountPercents,
                       RedirectAttributes ra) {
        try {
            ExportOrder order = new ExportOrder();
            if (customerId != null) {
                Customer customer = new Customer(); customer.setId(customerId);
                order.setCustomer(customer);
            }
            order.setNote(note); order.setDeliveryAddress(deliveryAddress);
            List<ExportDetail> details = new ArrayList<>();
            for (int i = 0; i < productIds.size(); i++) {
                ExportDetail d = new ExportDetail();
                Product product = new Product(); product.setId(productIds.get(i));
                d.setProduct(product); d.setQuantity(quantities.get(i));
                d.setUnitPrice(unitPrices.get(i));
                if (discountPercents != null && i < discountPercents.size())
                    d.setDiscountPercent(discountPercents.get(i));
                d.setExportOrder(order); details.add(d);
            }
            order.setDetails(details);
            exportService.save(order);
            ra.addFlashAttribute("success","Tạo phiếu xuất thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/exports";
    }

    /** Chỉ ADMIN + MANAGER */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        try { exportService.complete(id); ra.addFlashAttribute("success","Xác nhận xuất kho thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/exports/" + id;
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try { exportService.cancel(id); ra.addFlashAttribute("success","Hủy phiếu xuất thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/exports";
    }

    @GetMapping("/api/product/{id}") @ResponseBody
    public ResponseEntity<?> getProductInfo(@PathVariable Long id) {
        return productService.findById(id).map(p -> {
            Map<String,Object> d = new HashMap<>();
            d.put("id",p.getId()); d.put("name",p.getName());
            d.put("unit",p.getUnitName()); d.put("sellPrice",p.getSellPrice());
            d.put("quantity",p.getQuantity()); d.put("stockStatus",p.getStockStatus().name());
            return ResponseEntity.ok(d);
        }).orElse(ResponseEntity.notFound().build());
    }
}
