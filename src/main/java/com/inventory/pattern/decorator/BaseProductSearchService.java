package com.inventory.pattern.decorator;

import com.inventory.entity.Product;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

/**
 * DECORATOR PATTERN – Concrete Component.
 * Triển khai cơ bản: gọi thẳng repository.
 */
@Component("baseProductSearch")
@RequiredArgsConstructor
public class BaseProductSearchService implements ProductSearchService {

    private final ProductRepository productRepository;

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return productRepository.searchProducts(kw, pageable);
    }

    @Override
    public long countActive() {
        return productRepository.countByStatusTrueAndDeletedFalse();
    }
}
