package com.inventory.controller;

import com.inventory.entity.Supplier;
import com.inventory.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/suppliers") @RequiredArgsConstructor
public class SupplierController {
    private final SupplierService supplierService;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue="") String search,
                       @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        Page<Supplier> suppliers = supplierService.findAll(search, page, size);
        model.addAttribute("suppliers", suppliers);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "suppliers");
        return "supplier/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        model.addAttribute("activePage", "suppliers");
        return "supplier/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return supplierService.findById(id).map(s -> {
            model.addAttribute("supplier", s);
            model.addAttribute("activePage", "suppliers");
            return "supplier/form";
        }).orElseGet(() -> { ra.addFlashAttribute("error","Không tìm thấy nhà cung cấp"); return "redirect:/suppliers"; });
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Supplier supplier, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) { model.addAttribute("activePage","suppliers"); return "supplier/form"; }
        try {
            if (supplier.getId() != null) supplierService.update(supplier.getId(), supplier);
            else supplierService.save(supplier);
            ra.addFlashAttribute("success","Lưu nhà cung cấp thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/suppliers";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try { supplierService.deleteById(id); ra.addFlashAttribute("success","Xóa thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/suppliers";
    }
}
