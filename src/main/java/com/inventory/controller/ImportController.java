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

@Controller @RequestMapping("/imports") @RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;
    private final SupplierService supplierService;
    private final ProductService productService;

    /** Tất cả xem được danh sách */
    @GetMapping
    public String list(Model model, @RequestParam(defaultValue="") String search,
                       @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        Page<ImportOrder> orders = importService.findAll(search, page, size);
        model.addAttribute("orders", orders);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "imports");
        return "import/list";
    }

    /** Tất cả xem chi tiết */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return importService.findById(id).map(order -> {
            model.addAttribute("order", order);
            model.addAttribute("activePage", "imports");
            return "import/detail";
        }).orElseGet(() -> { ra.addFlashAttribute("error","Không tìm thấy phiếu nhập"); return "redirect:/imports"; });
    }

    /** STAFF + MANAGER + ADMIN: tạo phiếu mới */
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("order", new ImportOrder());
        model.addAttribute("suppliers", supplierService.findAllActive());
        model.addAttribute("products", productService.findAllActive());
        model.addAttribute("activePage", "imports");
        return "import/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam Long supplierId,
                       @RequestParam(required=false) String note,
                       @RequestParam(required=false) String invoiceNumber,
                       @RequestParam List<Long> productIds,
                       @RequestParam List<Integer> quantities,
                       @RequestParam List<BigDecimal> unitPrices,
                       RedirectAttributes ra) {
        try {
            ImportOrder order = new ImportOrder();
            Supplier supplier = new Supplier(); supplier.setId(supplierId);
            order.setSupplier(supplier);
            order.setNote(note); order.setInvoiceNumber(invoiceNumber);
            List<ImportDetail> details = new ArrayList<>();
            for (int i = 0; i < productIds.size(); i++) {
                ImportDetail d = new ImportDetail();
                Product product = new Product(); product.setId(productIds.get(i));
                d.setProduct(product); d.setQuantity(quantities.get(i));
                d.setUnitPrice(unitPrices.get(i)); d.setImportOrder(order);
                details.add(d);
            }
            order.setDetails(details);
            importService.save(order);
            ra.addFlashAttribute("success","Tạo phiếu nhập thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/imports";
    }

    /** Chỉ ADMIN + MANAGER: xác nhận nhập kho */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, RedirectAttributes ra) {
        try { importService.complete(id); ra.addFlashAttribute("success","Xác nhận nhập kho thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/imports/" + id;
    }

    /** Chỉ ADMIN + MANAGER: hủy phiếu */
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try { importService.cancel(id); ra.addFlashAttribute("success","Hủy phiếu nhập thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/imports";
    }

    @GetMapping("/api/product/{id}") @ResponseBody
    public ResponseEntity<?> getProductInfo(@PathVariable Long id) {
        return productService.findById(id).map(p -> {
            Map<String,Object> d = new HashMap<>();
            d.put("id",p.getId()); d.put("name",p.getName());
            d.put("unit",p.getUnitName()); d.put("costPrice",p.getCostPrice());
            d.put("quantity",p.getQuantity());
            return ResponseEntity.ok(d);
        }).orElse(ResponseEntity.notFound().build());
    }
}
