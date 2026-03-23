package com.inventory.controller;

import com.inventory.entity.AppUser;
import com.inventory.enums.UserRole;
import com.inventory.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")   // toàn bộ controller chỉ ADMIN
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        Page<AppUser> users = userService.findAll(page, size);
        model.addAttribute("users", users);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("activePage", "users");
        return "user/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new AppUser());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("isNew", true);
        model.addAttribute("activePage", "users");
        return "user/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return userService.findById(id).map(u -> {
            model.addAttribute("user", u);
            model.addAttribute("roles", UserRole.values());
            model.addAttribute("isNew", false);
            model.addAttribute("activePage", "users");
            return "user/form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/users";
        });
    }

    @PostMapping("/save")
    public String save(@ModelAttribute AppUser user,
                       @RequestParam(defaultValue = "false") boolean changePassword,
                       @RequestParam(required = false) String confirmPassword,
                       RedirectAttributes ra) {
        try {
            // Validate password match khi tạo mới hoặc đổi mật khẩu
            if (user.getId() == null || changePassword) {
                if (user.getPassword() == null || user.getPassword().isBlank())
                    throw new RuntimeException("Mật khẩu không được để trống");
                if (!user.getPassword().equals(confirmPassword))
                    throw new RuntimeException("Mật khẩu xác nhận không khớp");
                if (user.getPassword().length() < 6)
                    throw new RuntimeException("Mật khẩu tối thiểu 6 ký tự");
            }

            if (user.getId() == null) userService.save(user);
            else userService.update(user.getId(), user, changePassword);

            ra.addFlashAttribute("success",
                user.getId() == null ? "Tạo tài khoản thành công!" : "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            if (user.getId() == null) return "redirect:/users/new";
            return "redirect:/users/" + user.getId() + "/edit";
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.toggleStatus(id);
            ra.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteById(id);
            ra.addFlashAttribute("success", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/users";
    }
}
