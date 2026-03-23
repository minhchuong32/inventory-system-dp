package com.inventory.service.impl;

import com.inventory.entity.*;
import com.inventory.enums.MovementType;
import com.inventory.exception.*;
import com.inventory.pattern.factory.*;
import com.inventory.pattern.observer.*;
import com.inventory.repository.*;
import com.inventory.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class ExportServiceImpl implements ExportService {

    private final ExportOrderRepository exportOrderRepository;
    private final ProductRepository     productRepository;
    private final StockEventPublisher   stockEventPublisher;  // Observer
    private final ExportOrderFactory    exportOrderFactory;   // Factory

    @Override @Transactional(readOnly = true)
    public Page<ExportOrder> findAll(String keyword, int page, int size) {
        Pageable p = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return kw != null ? exportOrderRepository.searchOrders(kw, p)
                          : exportOrderRepository.findAllOrdered(p);
    }

    @Override @Transactional(readOnly = true)
    public Optional<ExportOrder> findById(Long id) {
        return exportOrderRepository.findByIdWithDetails(id);
    }

    @Override
    public ExportOrder save(ExportOrder order) {
        if (order.getId() == null) {
            order.setCode(generateCode());
            order.setOrderDate(LocalDate.now());
        }
        if (order.getDetails() != null) {
            order.getDetails().forEach(d -> { d.setExportOrder(order); d.calculateTotal(); });
            order.calculateTotal();
        }
        return exportOrderRepository.save(order);
    }

    @Override
    public ExportOrder complete(Long id) {
        ExportOrder order = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("phiếu xuất", id));

        for (ExportDetail detail : order.getDetails()) {
            Product p = productRepository.findById(detail.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", detail.getProduct().getId()));
            if (!p.canExport(detail.getQuantity()))
                throw new InsufficientStockException(p.getName(), detail.getQuantity(),
                    p.getQuantity() != null ? p.getQuantity() : 0);
        }

        order.complete();

        for (ExportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId()).get();
            int before = product.getQuantity();
            product.decreaseStock(detail.getQuantity());
            productRepository.save(product);

            // Observer Pattern
            stockEventPublisher.publish(StockEvent.builder()
                .product(product).movementType(MovementType.OUT)
                .quantityChanged(detail.getQuantity())
                .beforeQuantity(before).afterQuantity(product.getQuantity())
                .referenceCode(order.getCode()).occurredAt(LocalDateTime.now())
                .build());
        }
        return exportOrderRepository.save(order);
    }

    @Override
    public void cancel(Long id) {
        ExportOrder order = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("phiếu xuất", id));
        order.cancel();
        exportOrderRepository.save(order);
    }

    @Override @Transactional(readOnly = true)
    public long countThisMonth() {
        LocalDate now = LocalDate.now();
        return exportOrderRepository.countByYearAndMonth(now.getYear(), now.getMonthValue());
    }

    @Override @Transactional(readOnly = true)
    public BigDecimal getTotalAmountThisMonth() {
        LocalDate now = LocalDate.now();
        BigDecimal r = exportOrderRepository.sumAmountByYearAndMonth(now.getYear(), now.getMonthValue());
        return r != null ? r : BigDecimal.ZERO;
    }

    private String generateCode() {
        return String.format("PX%06d", exportOrderRepository.count() + 1);
    }
}
