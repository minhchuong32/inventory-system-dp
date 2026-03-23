package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import com.inventory.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Entity @Table(name = "app_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class AppUser extends BaseEntity {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Email(message = "Email không hợp lệ")
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    @Builder.Default
    private UserRole role = UserRole.STAFF;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public boolean isAdmin()   { return role == UserRole.ADMIN; }
    public boolean isManager() { return role == UserRole.MANAGER || isAdmin(); }

    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && username != null && !username.isBlank();
    }
}
