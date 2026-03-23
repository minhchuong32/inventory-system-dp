package com.inventory.service.impl;

import com.inventory.entity.Customer;
import com.inventory.exception.DuplicateCodeException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.CustomerRepository;
import com.inventory.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;

    @Override @Transactional(readOnly = true)
    public Page<Customer> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return customerRepository.searchCustomers(kw, pageable);
    }

    @Override @Transactional(readOnly = true)
    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id).filter(c -> !c.isDeleted());
    }

    @Override @Transactional(readOnly = true)
    public List<Customer> findAllActive() { return customerRepository.findByStatusTrueAndDeletedFalse(); }

    @Override
    public Customer save(Customer customer) {
        if (customer.getId() == null) {
            if (customer.getCode() == null || customer.getCode().isBlank())
                customer.setCode(generateCode());
            if (customerRepository.existsByCodeAndDeletedFalse(customer.getCode()))
                throw new DuplicateCodeException("Mã khách hàng", customer.getCode());
        }
        return customerRepository.save(customer);
    }

    @Override
    public Customer update(Long id, Customer incoming) {
        Customer existing = findById(id).orElseThrow(() -> new ResourceNotFoundException("khách hàng", id));
        existing.setName(incoming.getName());
        existing.setPhone(incoming.getPhone());
        existing.setEmail(incoming.getEmail());
        existing.setAddress(incoming.getAddress());
        existing.setTaxCode(incoming.getTaxCode());
        existing.setCustomerType(incoming.getCustomerType());
        existing.setStatus(incoming.getStatus());
        return customerRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        Customer c = findById(id).orElseThrow(() -> new ResourceNotFoundException("khách hàng", id));
        c.softDelete();
        customerRepository.save(c);
    }

    @Override @Transactional(readOnly = true)
    public long countActiveCustomers() { return customerRepository.countByStatusTrueAndDeletedFalse(); }

    private String generateCode() {
        return String.format("KH%04d", customerRepository.count() + 1);
    }
}
