package com.inventory.controller;

import com.inventory.entity.Customer;
import com.inventory.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller @RequestMapping("/customers") @RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    public String list(Model model, @RequestParam(defaultValue="") String search,
                       @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="10") int size) {
        Page<Customer> customers = customerService.findAll(search, page, size);
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "customers");
        return "customer/list";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("activePage", "customers");
        return "customer/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return customerService.findById(id).map(c -> {
            model.addAttribute("customer", c);
            model.addAttribute("activePage", "customers");
            return "customer/form";
        }).orElseGet(() -> { ra.addFlashAttribute("error","Không tìm thấy khách hàng"); return "redirect:/customers"; });
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Customer customer, BindingResult result,
                       Model model, RedirectAttributes ra) {
        if (result.hasErrors()) { model.addAttribute("activePage","customers"); return "customer/form"; }
        try {
            if (customer.getId() != null) customerService.update(customer.getId(), customer);
            else customerService.save(customer);
            ra.addFlashAttribute("success","Lưu khách hàng thành công!");
        } catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/customers";
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try { customerService.deleteById(id); ra.addFlashAttribute("success","Xóa thành công!"); }
        catch (Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/customers";
    }
}
