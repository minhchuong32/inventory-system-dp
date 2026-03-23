package com.inventory.pattern.decorator;

import com.inventory.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DECORATOR PATTERN – Concrete Decorator: In-memory Cache.
 * Bọc LoggingProductSearchDecorator và cache kết quả
 * tìm kiếm trong 60 giây để giảm DB round-trip.
 */
@Component("cachingProductSearch")
@Slf4j
public class CachingProductSearchDecorator extends ProductSearchDecorator {

    private static final int    MAX_CACHE_SIZE   = 50;
    private static final long   CACHE_TTL_MS     = 60_000L; // 60s

    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public CachingProductSearchDecorator(
            @Qualifier("loggingProductSearch") ProductSearchService wrapped) {
        super(wrapped);
    }

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        String key = buildKey(keyword, page, size);
        CacheEntry entry = cache.get(key);

        if (entry != null && !entry.isExpired()) {
            log.debug("[ProductSearch Cache] HIT for key='{}'", key);
            return entry.data;
        }

        log.debug("[ProductSearch Cache] MISS for key='{}'", key);
        Page<Product> result = wrapped.search(keyword, page, size);
        cache.put(key, new CacheEntry(result));
        return result;
    }

    public void invalidate() {
        cache.clear();
        log.info("[ProductSearch Cache] Invalidated all entries");
    }

    private String buildKey(String kw, int page, int size) {
        return (kw == null ? "" : kw.toLowerCase()) + "|" + page + "|" + size;
    }

    private static class CacheEntry {
        final Page<Product> data;
        final long          createdAt;

        CacheEntry(Page<Product> data) {
            this.data      = data;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CACHE_TTL_MS;
        }
    }
}
