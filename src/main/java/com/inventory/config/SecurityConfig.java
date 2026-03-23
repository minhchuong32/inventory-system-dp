package com.inventory.config;

import com.inventory.entity.AppUser;
import com.inventory.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import java.util.List;

/**
 * Role hierarchy:
 *   ADMIN   – toàn quyền (xem + tạo + xác nhận + xóa + quản lý user)
 *   MANAGER – quản lý nghiệp vụ (xem + tạo + xác nhận, KHÔNG xóa sản phẩm/NCC/KH, KHÔNG quản lý user)
 *   STAFF   – nhân viên nhập liệu (chỉ xem danh sách + tạo phiếu, KHÔNG xác nhận/hủy/xóa)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // bật @PreAuthorize trên controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppUserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // ── Static resources ──────────────────────────────
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/error", "/access-denied").permitAll()

                // ── ADMIN only ────────────────────────────────────
                .requestMatchers("/users/**").hasRole("ADMIN")

                // ── ADMIN + MANAGER: xóa/điều chỉnh master data ──
                .requestMatchers("/products/*/delete").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/suppliers/*/delete").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/customers/*/delete").hasAnyRole("ADMIN", "MANAGER")

                // ── ADMIN + MANAGER: xác nhận / hủy phiếu ────────
                .requestMatchers("/imports/*/complete", "/imports/*/cancel").hasAnyRole("ADMIN", "MANAGER")
                .requestMatchers("/exports/*/complete", "/exports/*/cancel").hasAnyRole("ADMIN", "MANAGER")

                // ── Tất cả đã xác thực: xem + tạo phiếu ─────────
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            AppUser user = userRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user: " + username));
            if (!Boolean.TRUE.equals(user.getStatus())) {
                throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa");
            }
            return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
