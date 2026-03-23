package com.inventory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AccessDeniedController {

    /**
     * Spring Security dùng RequestDispatcher.forward() khi xử lý AccessDeniedException
     * → giữ nguyên HTTP method gốc (GET hoặc POST).
     * Phải dùng @RequestMapping (không phải @GetMapping) để xử lý cả GET lẫn POST.
     */
    @RequestMapping("/access-denied")
    public String accessDenied(Model model) {
        model.addAttribute("activePage", "");
        return "access-denied";
    }
}