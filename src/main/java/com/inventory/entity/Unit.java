package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity @Table(name = "units")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Unit extends BaseEntity {
    @NotBlank(message = "Tên đơn vị không được để trống")
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "abbreviation", length = 20)
    private String abbreviation;

    @Column(name = "description", length = 255)
    private String description;

    @Override
    public boolean isValid() {
        return super.isValid() && name != null && !name.isBlank();
    }
}
