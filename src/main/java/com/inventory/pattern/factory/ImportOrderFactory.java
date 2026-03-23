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

    @Override
    public String getOrderType() { return "IMPORT"; }

    @Override
    public ImportOrder createOrder(OrderRequest request) {
        ImportOrder order = new ImportOrder();

        // Set header fields
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now());
        order.setNote(request.getNote());
        order.setInvoiceNumber(request.getInvoiceNumber());
        order.setExpectedDate(request.getExpectedDate());
        order.setCode(generateCode());

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

        return order;
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

    private String generateCode() {
        long seq = importOrderRepository.count() + 1;
        return String.format("PN%06d", seq);
    }
}
