package com.inventory.pattern.proxy;

import com.inventory.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class CachingProductSearchProxy implements ProductSearchService {

    private final ProductSearchService target;

    private static final int MAX_CACHE_SIZE = 50;
    private static final long TTL = 60_000;

    private final Map<String, CacheEntry> cache =
            Collections.synchronizedMap(
                    new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
                        @Override
                        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
                            return size() > MAX_CACHE_SIZE;
                        }
                    });

    public CachingProductSearchProxy(ProductSearchService target) {
        this.target = target;
    }

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        String key = buildKey(keyword, page, size);

        synchronized (cache) {
            CacheEntry entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                log.debug("[Proxy-Cache] HIT: {}", key);
                return entry.data;
            }
        }

        log.debug("[Proxy-Cache] MISS: {}", key);

        Page<Product> result = target.search(keyword, page, size);

        synchronized (cache) {
            cache.put(key, new CacheEntry(result));
        }

        return result;
    }

    @Override
    public long countActive() {
        return target.countActive();
    }

    private String buildKey(String kw, int page, int size) {
        return (kw == null ? "" : kw.toLowerCase()) + "|" + page + "|" + size;
    }

    private static class CacheEntry {
        final Page<Product> data;
        final long createdAt;

        CacheEntry(Page<Product> data) {
            this.data = data;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > TTL;
        }
    }
}