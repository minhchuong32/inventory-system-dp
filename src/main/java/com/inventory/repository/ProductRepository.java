package com.inventory.repository;
import com.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatusTrueAndDeletedFalse();
    boolean existsByCodeAndDeletedFalse(String code);
    boolean existsByBarcodeAndDeletedFalse(String barcode);
    long countByStatusTrueAndDeletedFalse();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = true AND " +
           "p.quantity <= p.minQuantity ORDER BY p.quantity ASC")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.status = true AND p.quantity = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.unit " +
           "WHERE p.deleted = false AND " +
           "(:kw IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(p.code) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<Product> searchProducts(@Param("kw") String keyword, Pageable pageable);

    @Query("SELECT COALESCE(SUM(p.quantity * p.costPrice), 0) FROM Product p WHERE p.deleted = false AND p.status = true")
    java.math.BigDecimal getTotalStockValue();
}
