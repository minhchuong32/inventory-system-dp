package com.inventory.pattern.proxy;

import com.inventory.entity.Product;
import org.springframework.data.domain.Page;

/**
 * DECORATOR PATTERN – Component interface.
 * Định nghĩa hành vi tìm kiếm sản phẩm
 * có thể được wrap bởi nhiều proxy.
 */
public interface ProductSearchService {
    Page<Product> search(String keyword, int page, int size);
    long countActive();
}
