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
@Table(name = "customers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Customer extends BaseEntity {

    @NotBlank(message = "Mã khách hàng không được để trống")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank(message = "Tên khách hàng không được để trống")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "tax_code", length = 20)
    private String taxCode;

    @Column(name = "customer_type", length = 20)
    @Builder.Default
    private String customerType = "RETAIL";  // RETAIL | WHOLESALE | VIP

    @Column(name = "total_purchase", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalPurchase = BigDecimal.ZERO;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ExportOrder> exportOrders = new ArrayList<>();

    // ---- Business logic ----

    public String getLoyaltyLevel() {
        if (totalPurchase == null) return "Thường";
        double value = totalPurchase.doubleValue();
        if (value >= 100_000_000) return "VIP";
        if (value >= 20_000_000)  return "Vàng";
        if (value >= 5_000_000)   return "Bạc";
        return "Thường";
    }

    public void addPurchase(BigDecimal amount) {
        if (this.totalPurchase == null) this.totalPurchase = BigDecimal.ZERO;
        this.totalPurchase = this.totalPurchase.add(amount);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && code != null && !code.isBlank()
                && name != null && !name.isBlank();
    }
}
