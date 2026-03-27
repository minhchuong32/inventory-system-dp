package com.inventory.pattern.proxy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductSearchProxyConfig {

    @Bean
    public ProductSearchService productSearchService(ProductSearchServiceImpl realService) {
        ProductSearchService logging = new LoggingProductSearchProxy(realService);
        return new CachingProductSearchProxy(logging);
    }
}