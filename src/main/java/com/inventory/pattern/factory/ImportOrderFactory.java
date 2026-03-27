package com.inventory.pattern.factory;

import com.inventory.entity.*;
import com.inventory.repository.ImportOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * FACTORY METHOD PATTERN – Concrete Creator for ImportOrder.
 * Đóng gói toàn bộ logic khởi tạo ImportOrder và các detail.
 */
@Component
@RequiredArgsConstructor
public class ImportOrderFactory implements OrderFactory<ImportOrder> {

    private final ImportOrderRepository importOrderRepository;
    private final CodeGenerator codeGenerator; 
    @Override
    public String getOrderType() { return OrderType.IMPORT.name(); }

    @Override
    public ImportOrder createOrder(OrderRequest request) {
        LocalDate orderDate = request.getOrderDate() != null
            ? request.getOrderDate()
            : LocalDate.now();

        ImportOrder order = ImportOrder.builder()
                .orderDate(orderDate)
                .note(request.getNote())
                .invoiceNumber(request.getInvoiceNumber())
                .expectedDate(request.getExpectedDate())
                .code(codeGenerator.generateImportCode())
                .build();

        // Set supplier reference
        if (request.getSupplierId() != null) {
            Supplier supplier = new Supplier();
            supplier.setId(request.getSupplierId());
            order.setSupplier(supplier);
        }

        // Build detail lines
        List<ImportDetail> details = buildDetails(order, request);
        order.setDetails(details);
        order.calculateTotal();
        return this.importOrderRepository.save(order);
    }

    private List<ImportDetail> buildDetails(ImportOrder order, OrderRequest request) {
        List<ImportDetail> details = new ArrayList<>();
        List<Long> ids    = request.getProductIds();
        List<Integer> qtys = request.getQuantities();
        List<BigDecimal> prices = request.getUnitPrices();

        for (int i = 0; i < ids.size(); i++) {
            ImportDetail d = new ImportDetail();
            Product p = new Product();
            p.setId(ids.get(i));
            d.setProduct(p);
            d.setQuantity(qtys.get(i));
            d.setUnitPrice(prices.get(i));
            d.setImportOrder(order);
            d.calculateTotal();
            details.add(d);
        }
        return details;
    }

}
