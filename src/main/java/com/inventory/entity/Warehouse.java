package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity @Table(name = "warehouses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Warehouse extends BaseEntity {
    @NotBlank(message = "Mã kho không được để trống")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank(message = "Tên kho không được để trống")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address", length = 300)
    private String address;

    @Column(name = "capacity")
    @Builder.Default
    private Integer capacity = 0;

    @Column(name = "manager_name", length = 100)
    private String managerName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @Override
    public boolean isValid() {
        return super.isValid() && code != null && name != null;
    }
}
