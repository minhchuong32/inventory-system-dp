package com.inventory.service.impl;

import com.inventory.entity.*;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.pattern.factory.*;
import com.inventory.pattern.observer.*;
import com.inventory.repository.*;
import com.inventory.service.ImportService;
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
public class ImportServiceImpl implements ImportService {

    private final ImportOrderRepository importOrderRepository;
    private final ProductRepository     productRepository;
    private final StockEventPublisher   stockEventPublisher;   // Observer
    private final ImportOrderFactory    importOrderFactory;    // Factory

    @Override @Transactional(readOnly = true)
    public Page<ImportOrder> findAll(String keyword, int page, int size) {
        Pageable p = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return kw != null ? importOrderRepository.searchOrders(kw, p)
                          : importOrderRepository.findAllOrdered(p);
    }

    @Override @Transactional(readOnly = true)
    public Optional<ImportOrder> findById(Long id) {
        return importOrderRepository.findByIdWithDetails(id);
    }

    @Override
    public ImportOrder save(ImportOrder order) {
        if (order.getId() == null) {
            order.setCode(generateCode());
            order.setOrderDate(LocalDate.now());
        }
        if (order.getDetails() != null) {
            order.getDetails().forEach(d -> { d.setImportOrder(order); d.calculateTotal(); });
            order.calculateTotal();
        }
        return importOrderRepository.save(order);
    }

    @Override
    public ImportOrder complete(Long id) {
        ImportOrder order = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("phiếu nhập", id));
        order.complete();

        for (ImportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", detail.getProduct().getId()));
            int before = product.getQuantity() != null ? product.getQuantity() : 0;
            product.increaseStock(detail.getQuantity());
            productRepository.save(product);

            // Observer Pattern: publish event instead of direct DB save
            stockEventPublisher.publish(StockEvent.builder()
                .product(product)
                .movementType(com.inventory.enums.MovementType.IN)
                .quantityChanged(detail.getQuantity())
                .beforeQuantity(before)
                .afterQuantity(product.getQuantity())
                .referenceCode(order.getCode())
                .occurredAt(LocalDateTime.now())
                .build());
        }
        return importOrderRepository.save(order);
    }

    @Override
    public void cancel(Long id) {
        ImportOrder order = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("phiếu nhập", id));
        order.cancel();
        importOrderRepository.save(order);
    }

    @Override @Transactional(readOnly = true)
    public long countThisMonth() {
        LocalDate now = LocalDate.now();
        return importOrderRepository.countByYearAndMonth(now.getYear(), now.getMonthValue());
    }

    @Override @Transactional(readOnly = true)
    public BigDecimal getTotalAmountThisMonth() {
        LocalDate now = LocalDate.now();
        BigDecimal r = importOrderRepository.sumAmountByYearAndMonth(now.getYear(), now.getMonthValue());
        return r != null ? r : BigDecimal.ZERO;
    }

    private String generateCode() {
        return String.format("PN%06d", importOrderRepository.count() + 1);
    }
}
