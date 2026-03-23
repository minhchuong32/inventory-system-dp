package com.inventory.service;

import com.inventory.entity.Product;
import com.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    Page<Product> findAll(String keyword, int page, int size);
    Optional<Product> findById(Long id);
    List<Product> findAllActive();
    Product save(Product product);
    Product update(Long id, Product product);
    void deleteById(Long id);
    List<Product> findLowStockProducts();
    long countActiveProducts();
    void adjustStock(Long productId, int delta, String reason, String refCode);
}
