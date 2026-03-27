package com.inventory.service.impl;

import com.inventory.entity.Product;
import com.inventory.enums.MovementType;
import com.inventory.exception.*;
import com.inventory.pattern.proxy.CachingProductSearchDecorator;
import com.inventory.repository.ProductRepository;
import com.inventory.service.ProductService;
import com.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor @Transactional @Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository             productRepository;
    private final StockService                  stockService;
    private final CachingProductSearchDecorator searchService;  // Decorator

    @Override @Transactional(readOnly = true)
    public Page<Product> findAll(String keyword, int page, int size) {
        return searchService.search(keyword, page, size);  // Uses Decorator chain
    }

    @Override @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id).filter(p -> !p.isDeleted());
    }

    @Override @Transactional(readOnly = true)
    public List<Product> findAllActive() {
        return productRepository.findByStatusTrueAndDeletedFalse();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            if (product.getCode() == null || product.getCode().isBlank())
                product.setCode(generateCode());
            if (productRepository.existsByCodeAndDeletedFalse(product.getCode()))
                throw new DuplicateCodeException("Mã sản phẩm", product.getCode());
        }
        Product saved = productRepository.save(product);
        searchService.invalidate();  // Invalidate cache
        return saved;
    }

    @Override
    public Product update(Long id, Product incoming) {
        Product existing = findById(id).orElseThrow(() -> new ResourceNotFoundException("sản phẩm", id));
        existing.setName(incoming.getName());
        existing.setDescription(incoming.getDescription());
        existing.setCategory(incoming.getCategory());
        existing.setSupplier(incoming.getSupplier());
        existing.setUnit(incoming.getUnit());
        existing.setWarehouse(incoming.getWarehouse());
        existing.setCostPrice(incoming.getCostPrice());
        existing.setSellPrice(incoming.getSellPrice());
        existing.setMinQuantity(incoming.getMinQuantity());
        existing.setMaxQuantity(incoming.getMaxQuantity());
        existing.setWeight(incoming.getWeight());
        existing.setStatus(incoming.getStatus());
        existing.setBarcode(incoming.getBarcode());
        Product saved = productRepository.save(existing);
        searchService.invalidate();
        return saved;
    }

    @Override
    public void deleteById(Long id) {
        Product product = findById(id).orElseThrow(() -> new ResourceNotFoundException("sản phẩm", id));
        product.softDelete();
        productRepository.save(product);
        searchService.invalidate();
    }

    @Override @Transactional(readOnly = true)
    public List<Product> findLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    @Override @Transactional(readOnly = true)
    public long countActiveProducts() {
        return searchService.countActive();
    }

    @Override
    public void adjustStock(Long productId, int delta, String reason, String refCode) {
        Product product = findById(productId).orElseThrow(() -> new ResourceNotFoundException("sản phẩm", productId));
        int before = product.getQuantity() != null ? product.getQuantity() : 0;
        if (delta > 0) product.increaseStock(delta);
        else if (delta < 0) product.decreaseStock(Math.abs(delta));
        productRepository.save(product);
        stockService.recordMovement(productId,
            product.getWarehouse() != null ? product.getWarehouse().getId() : null,
            MovementType.ADJUST, Math.abs(delta), before, product.getQuantity(), refCode, "ADJUST", reason);
    }

    private String generateCode() {
        return String.format("SP%05d", productRepository.count() + 1);
    }
}
