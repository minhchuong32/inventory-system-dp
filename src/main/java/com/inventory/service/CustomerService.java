package com.inventory.service;
import com.inventory.entity.Customer;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;
public interface CustomerService {
    Page<Customer> findAll(String keyword, int page, int size);
    Optional<Customer> findById(Long id);
    List<Customer> findAllActive();
    Customer save(Customer customer);
    Customer update(Long id, Customer customer);
    void deleteById(Long id);
    long countActiveCustomers();
}
