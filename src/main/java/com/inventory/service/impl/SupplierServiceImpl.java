package com.inventory.service.impl;

import com.inventory.entity.Supplier;
import com.inventory.exception.DuplicateCodeException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.SupplierRepository;
import com.inventory.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;

    @Override @Transactional(readOnly = true)
    public Page<Supplier> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return supplierRepository.searchSuppliers(kw, pageable);
    }

    @Override @Transactional(readOnly = true)
    public Optional<Supplier> findById(Long id) {
        return supplierRepository.findById(id).filter(s -> !s.isDeleted());
    }

    @Override @Transactional(readOnly = true)
    public List<Supplier> findAllActive() { return supplierRepository.findByStatusTrueAndDeletedFalse(); }

    @Override
    public Supplier save(Supplier supplier) {
        if (supplier.getId() == null) {
            if (supplier.getCode() == null || supplier.getCode().isBlank())
                supplier.setCode(generateCode());
            if (supplierRepository.existsByCodeAndDeletedFalse(supplier.getCode()))
                throw new DuplicateCodeException("Mã nhà cung cấp", supplier.getCode());
        }
        return supplierRepository.save(supplier);
    }

    @Override
    public Supplier update(Long id, Supplier incoming) {
        Supplier existing = findById(id).orElseThrow(() -> new ResourceNotFoundException("nhà cung cấp", id));
        existing.setName(incoming.getName());
        existing.setContactPerson(incoming.getContactPerson());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setAddress(incoming.getAddress());
        existing.setTaxCode(incoming.getTaxCode());
        existing.setBankAccount(incoming.getBankAccount());
        existing.setBankName(incoming.getBankName());
        existing.setCreditLimit(incoming.getCreditLimit());
        existing.setStatus(incoming.getStatus());
        return supplierRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Supplier s = findById(id).orElseThrow(() -> new ResourceNotFoundException("nhà cung cấp", id));
        s.softDelete();
        supplierRepository.save(s);
    }

    @Override @Transactional(readOnly = true)
    public long countActiveSuppliers() { return supplierRepository.countByStatusTrueAndDeletedFalse(); }

    private String generateCode() {
        return String.format("NCC%04d", supplierRepository.count() + 1);
    }
}
