package com.inventory.pattern.factory;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * FACTORY METHOD PATTERN – Product data transfer object.
 * Đóng gói tất cả thông tin cần thiết để tạo một order.
 */
@Getter
@Builder
public class OrderRequest {
    // Common
    private final String        note;
    private final LocalDate     orderDate;

    // Import specific
    private final Long          supplierId;
    private final String        invoiceNumber;
    private final LocalDate     expectedDate;

    // Export specific
    private final Long          customerId;
    private final String        deliveryAddress;
    private final LocalDate     expectedDelivery;

    // Detail lines
    private final List<Long>        productIds;
    private final List<Integer>     quantities;
    private final List<BigDecimal>  unitPrices;
    private final List<BigDecimal>  discountPercents;
}
