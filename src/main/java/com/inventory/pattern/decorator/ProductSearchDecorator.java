package com.inventory.pattern.decorator;

import com.inventory.entity.Product;
import org.springframework.data.domain.Page;

/**
 * DECORATOR PATTERN – Base Decorator (abstract).
 * Wrap ProductSearchService khác và ủy quyền lại.
 */
public abstract class ProductSearchDecorator implements ProductSearchService {

    protected final ProductSearchService wrapped;

    protected ProductSearchDecorator(ProductSearchService wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        return wrapped.search(keyword, page, size);
    }

    @Override
    public long countActive() {
        return wrapped.countActive();
    }
}
