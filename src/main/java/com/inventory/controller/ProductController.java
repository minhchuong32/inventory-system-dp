package com.inventory.controller;

import com.inventory.entity.Product;
import com.inventory.repository.*;
import com.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.HashMap;
import java.util.Map;

@Controller @RequestMapping("/products") @RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final UnitRepository unitRepository;
    private final WarehouseRepository warehouseRepository;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue="") String search,
                       @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        Page<Product> products = productService.findAll(search, page, size);
        model.addAttribute("products", products);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "products");
        return "product/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        populateFormData(model);
        model.addAttribute("activePage", "products");
        return "product/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productService.findById(id).map(p -> {
            model.addAttribute("product", p);
            populateFormData(model);
            model.addAttribute("activePage", "products");
            return "product/form";
        }).orElseGet(() -> { ra.addFlashAttribute("error","Không tìm thấy sản phẩm"); return "redirect:/products"; });
    }

    @GetMapping("/{id}/stock")
    public String stockHistory(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return productService.findById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("activePage", "products");
            return "product/stock-history";
        }).orElseGet(() -> { ra.addFlashAttribute("error","Không tìm thấy sản phẩm"); return "redirect:/products"; });
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Product product, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) { populateFormData(model); model.addAttribute("activePage","products"); return "product/form"; }
        try {
            if (product.getId() != null) productService.update(product.getId(), product);
            else productService.save(product);
            ra.addFlashAttribute("success","Lưu sản phẩm thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/products";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try { productService.deleteById(id); ra.addFlashAttribute("success","Xóa sản phẩm thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/products";
    }

    @GetMapping("/api/{id}") @ResponseBody
    public ResponseEntity<?> getProductApi(@PathVariable Long id) {
        return productService.findById(id).map(p -> {
            Map<String,Object> d = new HashMap<>();
            d.put("id",p.getId()); d.put("name",p.getName()); d.put("code",p.getCode());
            d.put("unit",p.getUnitName()); d.put("costPrice",p.getCostPrice());
            d.put("sellPrice",p.getSellPrice()); d.put("quantity",p.getQuantity());
            d.put("stockStatus",p.getStockStatus().name());
            return ResponseEntity.ok(d);
        }).orElse(ResponseEntity.notFound().build());
    }

    private void populateFormData(Model model) {
        model.addAttribute("categories", categoryRepository.findAllActive());
        model.addAttribute("suppliers", supplierRepository.findByStatusTrueAndDeletedFalse());
        model.addAttribute("units", unitRepository.findByDeletedFalseOrderByNameAsc());
        model.addAttribute("warehouses", warehouseRepository.findByStatusTrueAndDeletedFalse());
    }
}
