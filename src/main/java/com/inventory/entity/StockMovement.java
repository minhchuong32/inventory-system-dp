package com.inventory.entity;

import com.inventory.entity.base.BaseEntity;
import com.inventory.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity @Table(name = "stock_movements")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", length = 20, nullable = false)
    private MovementType movementType;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "before_quantity", nullable = false)
    private Integer beforeQuantity;

    @Column(name = "after_quantity", nullable = false)
    private Integer afterQuantity;

    @Column(name = "reference_code", length = 30)
    private String referenceCode;

    @Column(name = "reference_type", length = 20)
    private String referenceType;

    @Column(name = "note", length = 300)
    private String note;

    public boolean isInbound()  { return movementType == MovementType.IN; }
    public boolean isOutbound() { return movementType == MovementType.OUT; }
}
