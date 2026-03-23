package com.inventory.service;
import com.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;
public interface SupplierService {
    Page<Supplier> findAll(String keyword, int page, int size);
    Optional<Supplier> findById(Long id);
    List<Supplier> findAllActive();
    Supplier save(Supplier supplier);
    Supplier update(Long id, Supplier supplier);
    void deleteById(Long id);
    long countActiveSuppliers();
}
