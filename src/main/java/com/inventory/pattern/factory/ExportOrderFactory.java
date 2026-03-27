package com.inventory.pattern.factory;

import com.inventory.entity.*;
import com.inventory.pattern.strategy.PricingContext;
import com.inventory.repository.CustomerRepository;
import com.inventory.repository.ExportOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * FACTORY METHOD PATTERN – Concrete Creator for ExportOrder.
 * Kết hợp với PricingContext (Strategy) để tính giá tự động
 * dựa trên loại khách hàng khi tạo order.
 */
@Component
@RequiredArgsConstructor
public class ExportOrderFactory implements OrderFactory<ExportOrder> {

    private final ExportOrderRepository exportOrderRepository;
    private final CustomerRepository customerRepository;
    private final PricingContext pricingContext;
     private final CodeGenerator codeGenerator; 
    @Override
    public String getOrderType() { return OrderType.EXPORT.name(); }

    @Override
    public ExportOrder createOrder(OrderRequest request) {

        // Resolve customer for pricing strategy
        Customer customer = customerRepository.findById(request.getCustomerId()).orElse(null);

        ExportOrder order = ExportOrder.builder()
            .orderDate(request.getOrderDate() != null ? request.getOrderDate() : LocalDate.now())
            .note(request.getNote())
            .deliveryAddress(request.getDeliveryAddress())
            .expectedDelivery(request.getExpectedDelivery())
            .code(codeGenerator.generateExportCode())
            .customer(customer)
            .build();

        List<ExportDetail> details = buildDetails(order, request, customer);
        order.setDetails(details);
        order.calculateTotal();
        return this.exportOrderRepository.save(order);
    }

    private List<ExportDetail> buildDetails(ExportOrder order, OrderRequest request, Customer customer) {
        List<ExportDetail> details = new ArrayList<>();
        List<Long>        ids     = request.getProductIds();
        List<Integer>     qtys    = request.getQuantities();
        List<BigDecimal>  prices  = request.getUnitPrices();

        for (int i = 0; i < ids.size(); i++) {
            ExportDetail d = new ExportDetail();
            Product p = new Product(); p.setId(ids.get(i));
            d.setProduct(p);
            d.setQuantity(qtys.get(i));

            // Apply pricing strategy
            BigDecimal basePrice = prices.get(i);
            BigDecimal discPct   = pricingContext.getDiscountPercent(customer, qtys.get(i));
            d.setUnitPrice(basePrice);
            d.setDiscountPercent(discPct);
            d.setExportOrder(order);
            d.calculateTotal();
            details.add(d);
        }
        return details;
    }

}
