package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Category extends BaseEntity {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Category> subCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<Product> products = new ArrayList<>();

    public boolean isRootCategory() { return parentCategory == null; }

    public int getProductCount() {
        return products != null ? (int) products.stream().filter(p -> !p.isDeleted()).count() : 0;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && name != null && !name.isBlank();
    }
}
