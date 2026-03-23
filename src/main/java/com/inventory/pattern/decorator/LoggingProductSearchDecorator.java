package com.inventory.pattern.decorator;

import com.inventory.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * DECORATOR PATTERN – Concrete Decorator: Logging.
 * Bọc ProductSearchService và ghi log mỗi lần search.
 * Không thay đổi logic tìm kiếm gốc.
 */
@Component("loggingProductSearch")
@Slf4j
public class LoggingProductSearchDecorator extends ProductSearchDecorator {

    public LoggingProductSearchDecorator(
            @Qualifier("baseProductSearch") ProductSearchService wrapped) {
        super(wrapped);
    }

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        long start = System.currentTimeMillis();
        Page<Product> result = wrapped.search(keyword, page, size);
        long elapsed = System.currentTimeMillis() - start;

        log.info("[ProductSearch] keyword='{}' page={} size={} → {} results in {}ms",
            keyword, page, size, result.getTotalElements(), elapsed);

        if (elapsed > 1000) {
            log.warn("[ProductSearch] SLOW QUERY: {}ms for keyword='{}'", elapsed, keyword);
        }
        return result;
    }
}
