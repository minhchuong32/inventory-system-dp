package com.inventory.pattern.facade;

import com.inventory.dto.DashboardStatsDto;
import com.inventory.entity.*;
import com.inventory.enums.MovementType;
import com.inventory.exception.InsufficientStockException;
import com.inventory.pattern.factory.*;
import com.inventory.pattern.observer.*;
import com.inventory.repository.*;
import com.inventory.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FACADE PATTERN – Facade.
 * Cung cấp interface đơn giản cho subsystem phức tạp:
 * Factory + Observer + Strategy + các Service/Repository.
 *
 * Controller chỉ cần gọi InventoryFacade thay vì
 * tương tác trực tiếp với nhiều service riêng lẻ.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InventoryFacade {

    // Factories (Factory Method Pattern)
    private final ImportOrderFactory    importOrderFactory;
    private final ExportOrderFactory    exportOrderFactory;

    // Observer publisher
    private final StockEventPublisher   stockEventPublisher;

    // Repositories & Services
    private final ImportOrderRepository importOrderRepository;
    private final ExportOrderRepository exportOrderRepository;
    private final ProductRepository     productRepository;
    private final ReportService         reportService;

    // ── Import workflow ──────────────────────────────────────────────────────

    /**
     * Tạo phiếu nhập kho mới (dùng ImportOrderFactory).
     */
    public ImportOrder createImportOrder(OrderRequest request) {
        ImportOrder order = importOrderFactory.createOrder(request);
        log.info("FACADE: ImportOrder created → {}", order.getCode());
        return order;
    }
    /**
     * Tạo phiếu xuất kho mới (dùng ExportOrderFactory + PricingStrategy).
     */
    public ExportOrder createExportOrder(OrderRequest request) {
        ExportOrder order = exportOrderFactory.createOrder(request);
        log.info("FACADE: ExportOrder created → {}", order.getCode());
        return order;
    }
    /**
     * Xác nhận nhập kho:
     * 1. Cộng tồn kho cho từng sản phẩm
     * 2. Publish StockEvent → Observer tự ghi StockMovement + alert
     */
    public ImportOrder confirmImport(Long orderId) {
        ImportOrder order = importOrderRepository.findByIdWithDetails(orderId)
            .orElseThrow(() -> new com.inventory.exception.ResourceNotFoundException("ImportOrder", orderId));
        order.complete();
        List<Long> productIds = order.getDetails().stream()
            .map(detail -> detail.getProduct().getId())
            .distinct()
            .toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        for (ImportDetail detail : order.getDetails()) {
            Product product = productMap.get(detail.getProduct().getId());
            if (product == null) {
                throw new com.inventory.exception.ResourceNotFoundException("Product", detail.getProduct().getId());
            }
            int before = product.getQuantity();
            product.increaseStock(detail.getQuantity());
            productRepository.save(product);
            // Publish event → StockMovementAuditObserver + LowStockAlertObserver
            stockEventPublisher.publish(StockEvent.builder()
                .product(product)
                .movementType(MovementType.IN)
                .quantityChanged(detail.getQuantity())
                .beforeQuantity(before)
                .afterQuantity(product.getQuantity())
                .referenceCode(order.getCode())
                .occurredAt(LocalDateTime.now())
                .build());
        }

        ImportOrder confirmed = importOrderRepository.save(order);
        log.info("FACADE: ImportOrder confirmed → {}", confirmed.getCode());
        return confirmed;
    }

    // ── Export workflow ──────────────────────────────────────────────────────

    /**
     * Tạo phiếu xuất kho mới (dùng ExportOrderFactory + PricingStrategy).
     */
   

    /**
     * Xác nhận xuất kho:
     * 1. Validate tồn kho
     * 2. Trừ tồn kho
     * 3. Publish StockEvent → Observer xử lý audit + alert
     */
    public ExportOrder confirmExport(Long orderId) {
        ExportOrder order = exportOrderRepository.findByIdWithDetails(orderId)
            .orElseThrow(() -> new com.inventory.exception.ResourceNotFoundException("ExportOrder", orderId));

        // Validate first
        for (ExportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId())
                .orElseThrow(() -> new com.inventory.exception.ResourceNotFoundException("Product", detail.getProduct().getId()));
            if (!product.canExport(detail.getQuantity())) {
                throw new InsufficientStockException(
                    product.getName(), detail.getQuantity(),
                    product.getQuantity() != null ? product.getQuantity() : 0);
            }
        }

        order.complete();
        List<Long> productIds = order.getDetails().stream()
            .map(detail -> detail.getProduct().getId())
            .distinct()
            .toList();
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, p -> p));
        for (ExportDetail detail : order.getDetails()) {
            Product product = productMap.get(detail.getProduct().getId());
            int before = product.getQuantity();
            product.decreaseStock(detail.getQuantity());
            productRepository.save(product);

            stockEventPublisher.publish(StockEvent.builder()
                .product(product)
                .movementType(MovementType.OUT)
                .quantityChanged(detail.getQuantity())
                .beforeQuantity(before)
                .afterQuantity(product.getQuantity())
                .referenceCode(order.getCode())
                .occurredAt(LocalDateTime.now())
                .build());
        }

        ExportOrder confirmed = exportOrderRepository.save(order);
        log.info("FACADE: ExportOrder confirmed → {}", confirmed.getCode());
        return confirmed;
    }

    // ── Dashboard ────────────────────────────────────────────────────────────

    /**
     * Lấy toàn bộ thống kê dashboard qua 1 lời gọi duy nhất.
     */
    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        return reportService.getDashboardStats();
    }
}
