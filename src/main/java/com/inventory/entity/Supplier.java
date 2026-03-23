package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "suppliers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Supplier extends BaseEntity {

    @NotBlank(message = "Mã nhà cung cấp không được để trống")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    @Column(name = "phone", length = 20)
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "bank_account", length = 30)
    private String bankAccount;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "credit_limit", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "current_debt", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal currentDebt = BigDecimal.ZERO;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Product> products = new ArrayList<>();

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ImportOrder> importOrders = new ArrayList<>();

    // ---- Business logic ----

    public boolean isOverCreditLimit() {
        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) == 0) return false;
        return currentDebt != null && currentDebt.compareTo(creditLimit) > 0;
    }

    public BigDecimal getRemainingCredit() {
        BigDecimal limit = creditLimit != null ? creditLimit : BigDecimal.ZERO;
        BigDecimal debt  = currentDebt  != null ? currentDebt  : BigDecimal.ZERO;
        return limit.subtract(debt);
    }

    public void updateDebt(BigDecimal amount) {
        if (this.currentDebt == null) this.currentDebt = BigDecimal.ZERO;
        this.currentDebt = this.currentDebt.add(amount);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && code != null && !code.isBlank()
                && name != null && !name.isBlank();
    }
}
