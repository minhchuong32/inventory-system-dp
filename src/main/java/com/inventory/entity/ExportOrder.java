package com.inventory.entity;

import com.inventory.entity.base.AbstractOrder;
import com.inventory.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity @Table(name = "export_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class ExportOrder extends AbstractOrder {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "delivery_address", length = 300)
    private String deliveryAddress;

    @Column(name = "expected_delivery")
    private LocalDate expectedDelivery;

    @Column(name = "actual_delivery")
    private LocalDate actualDelivery;

    @OneToMany(mappedBy = "exportOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ExportDetail> details = new ArrayList<>();

    @Override
    protected String generateCode(long sequence) { return String.format("PX%06d", sequence); }

    public void calculateTotal() {
        BigDecimal total = details.stream()
            .map(d -> d.getTotalPrice() != null ? d.getTotalPrice() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        setTotalAmount(total);
        recalculateFinalAmount();
    }

    public boolean canComplete() {
        return details != null && !details.isEmpty() && details.stream()
            .allMatch(d -> d.getProduct() != null && d.getProduct().canExport(d.getQuantity()));
    }

    public int getDetailCount() { return details != null ? details.size() : 0; }
    public int getTotalItems() {
        return details != null
            ? details.stream().mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0).sum() : 0;
    }

    public void addDetail(ExportDetail detail) {
        detail.setExportOrder(this);
        this.details.add(detail);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && details != null && !details.isEmpty();
    }
}
