package com.inventory.entity.base;

import com.inventory.enums.OrderStatus;
import com.inventory.enums.PaymentStatus;
import com.inventory.exception.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstract base for all order types (import/export).
 * Encapsulates shared order behavior: status transitions, totals calculation.
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
public abstract class AbstractOrder extends BaseEntity {

    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "final_amount", precision = 18, scale = 2)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "note", length = 500)
    private String note;

    // ---- Business methods ----

    /**
     * Transition to COMPLETED. Subclasses may override for additional logic.
     */
    public void complete() {
        if (this.status.isFinal()) {
            throw new BusinessException("ORDER_ALREADY_FINAL",
                "Phiếu đã ở trạng thái cuối: " + this.status.getDisplayName());
        }
        this.status = OrderStatus.COMPLETED;
    }

    /**
     * Transition to CANCELLED.
     */
    public void cancel() {
        if (this.status.isFinal()) {
            throw new BusinessException("ORDER_ALREADY_FINAL",
                "Không thể hủy phiếu đã hoàn thành");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isPending()   { return this.status == OrderStatus.PENDING; }
    public boolean isCompleted() { return this.status == OrderStatus.COMPLETED; }
    public boolean isCancelled() { return this.status == OrderStatus.CANCELLED; }

    /**
     * Recalculates finalAmount = totalAmount - discountAmount + taxAmount.
     * Subclasses should call this after modifying details.
     */
    protected void recalculateFinalAmount() {
        BigDecimal discount = this.discountAmount != null ? this.discountAmount : BigDecimal.ZERO;
        BigDecimal tax      = this.taxAmount      != null ? this.taxAmount      : BigDecimal.ZERO;
        BigDecimal total    = this.totalAmount     != null ? this.totalAmount    : BigDecimal.ZERO;
        this.finalAmount = total.subtract(discount).add(tax);
    }

    /** Subclasses must implement to generate domain-specific codes (PN000001, PX000001). */
    protected abstract String generateCode(long sequence);

    @Override
    public boolean isValid() {
        return super.isValid() && code != null && !code.isBlank();
    }
}
