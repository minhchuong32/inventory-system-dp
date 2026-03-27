package com.inventory.pattern.proxy;

import com.inventory.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

@Slf4j
public class LoggingProductSearchProxy implements ProductSearchService {

    private final ProductSearchService target;

    public LoggingProductSearchProxy(ProductSearchService target) {
        this.target = target;
    }

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        long start = System.currentTimeMillis();

        Page<Product> result = target.search(keyword, page, size);

        long elapsed = System.currentTimeMillis() - start;

        log.info("[Proxy-Logging] keyword='{}' → {} results in {}ms",
                keyword, result.getTotalElements(), elapsed);

        if (elapsed > 1000) {
            log.warn("[Proxy-Logging] SLOW QUERY: {}ms", elapsed);
        }

        return result;
    }

    @Override
    public long countActive() {
        return target.countActive();
    }
}