package com.inventory.pattern.proxy;


@Component
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Page<Product> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.searchProducts(keyword, pageable);
    }

    @Override
    public long countActive() {
        return productRepository.countByStatusTrueAndDeletedFalse();
    }
}