package com.inventory.service;
import com.inventory.entity.ExportOrder;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.util.Optional;
public interface ExportService {
    Page<ExportOrder> findAll(String keyword, int page, int size);
    Optional<ExportOrder> findById(Long id);
    ExportOrder save(ExportOrder order);
    ExportOrder complete(Long id);
    void cancel(Long id);
    long countThisMonth();
    BigDecimal getTotalAmountThisMonth();
}
