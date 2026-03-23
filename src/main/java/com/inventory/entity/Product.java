package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import com.inventory.enums.StockStatus;
import com.inventory.exception.InsufficientStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class Product extends BaseEntity {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @DecimalMin(value = "0", message = "Giá nhập phải >= 0")
    @Column(name = "cost_price", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal costPrice = BigDecimal.ZERO;

    @DecimalMin(value = "0", message = "Giá bán phải >= 0")
    @Column(name = "sell_price", precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal sellPrice = BigDecimal.ZERO;

    @Min(value = 0, message = "Tồn kho không được âm")
    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 0;

    @Column(name = "min_quantity")
    @Builder.Default
    private Integer minQuantity = 5;

    @Column(name = "max_quantity")
    @Builder.Default
    private Integer maxQuantity = 999999;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "status")
    @Builder.Default
    private Boolean status = true;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ImportDetail> importDetails = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ExportDetail> exportDetails = new ArrayList<>();

    // ---- Business logic ----

    @Transient
    public StockStatus getStockStatus() {
        if (quantity == null || quantity <= 0) return StockStatus.OUT_OF_STOCK;
        if (minQuantity != null && quantity <= minQuantity) return StockStatus.LOW_STOCK;
        return StockStatus.IN_STOCK;
    }

    @Transient
    public boolean isLowStock() {
        return getStockStatus() == StockStatus.LOW_STOCK;
    }

    @Transient
    public boolean isOutOfStock() {
        return getStockStatus() == StockStatus.OUT_OF_STOCK;
    }

    @Transient
    public BigDecimal getStockValue() {
        if (quantity == null || costPrice == null) return BigDecimal.ZERO;
        return costPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public boolean canExport(Integer qty) {
        return qty != null && quantity != null && quantity >= qty;
    }

    /**
     * Increases stock quantity (e.g., import confirmed).
     */
    public void increaseStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng nhập phải > 0");
        this.quantity = (this.quantity != null ? this.quantity : 0) + qty;
    }

    /**
     * Decreases stock quantity (e.g., export confirmed).
     * Throws InsufficientStockException if not enough stock.
     */
    public void decreaseStock(int qty) {
        if (qty <= 0) throw new IllegalArgumentException("Số lượng xuất phải > 0");
        int current = this.quantity != null ? this.quantity : 0;
        if (current < qty) {
            throw new InsufficientStockException(this.name, qty, current);
        }
        this.quantity = current - qty;
    }

    @Transient
    public String getUnitName() {
        return unit != null ? unit.getName() : "";
    }

    @Transient
    public String getCategoryName() {
        return category != null ? category.getName() : "";
    }

    @Transient
    public String getSupplierName() {
        return supplier != null ? supplier.getName() : "";
    }

    @Override
    public boolean isValid() {
        return super.isValid() && code != null && !code.isBlank()
                && name != null && !name.isBlank();
    }
}
