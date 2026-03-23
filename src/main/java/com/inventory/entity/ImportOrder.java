package com.inventory.entity;

import com.inventory.entity.base.AbstractOrder;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "import_orders")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @SuperBuilder
public class ImportOrder extends AbstractOrder {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(name = "expected_date")
    private LocalDate expectedDate;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @OneToMany(mappedBy = "importOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<ImportDetail> details = new ArrayList<>();

    // ---- Business logic ----

    @Override
    protected String generateCode(long sequence) {
        return String.format("PN%06d", sequence);
    }

    /**
     * Recalculates totalAmount from detail lines.
     */
    public void calculateTotal() {
        BigDecimal total = details.stream()
            .map(d -> d.getTotalPrice() != null ? d.getTotalPrice() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        setTotalAmount(total);
        recalculateFinalAmount();
    }

    public int getDetailCount() { return details != null ? details.size() : 0; }

    public int getTotalItems() {
        return details != null
            ? details.stream().mapToInt(d -> d.getQuantity() != null ? d.getQuantity() : 0).sum()
            : 0;
    }

    public void addDetail(ImportDetail detail) {
        detail.setImportOrder(this);
        this.details.add(detail);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && details != null && !details.isEmpty();
    }
}
