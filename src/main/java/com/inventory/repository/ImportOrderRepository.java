package com.inventory.repository;
import com.inventory.entity.ImportOrder;
import com.inventory.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;
@Repository
public interface ImportOrderRepository extends JpaRepository<ImportOrder, Long> {
    @Query("SELECT COUNT(i) FROM ImportOrder i WHERE i.deleted=false AND YEAR(i.orderDate)=:y AND MONTH(i.orderDate)=:m")
    long countByYearAndMonth(@Param("y") int year, @Param("m") int month);

    @Query("SELECT COALESCE(SUM(i.finalAmount),0) FROM ImportOrder i WHERE i.deleted=false AND i.status='COMPLETED' AND YEAR(i.orderDate)=:y AND MONTH(i.orderDate)=:m")
    BigDecimal sumAmountByYearAndMonth(@Param("y") int year, @Param("m") int month);

    @Query(value = "SELECT DISTINCT i FROM ImportOrder i LEFT JOIN FETCH i.supplier WHERE i.deleted=false AND " +
                   "(:kw IS NULL OR LOWER(i.code) LIKE LOWER(CONCAT('%',:kw,'%'))) ORDER BY i.orderDate DESC, i.id DESC",
           countQuery = "SELECT COUNT(i) FROM ImportOrder i WHERE i.deleted=false AND " +
                        "(:kw IS NULL OR LOWER(i.code) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<ImportOrder> searchOrders(@Param("kw") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT i FROM ImportOrder i LEFT JOIN FETCH i.supplier WHERE i.deleted=false ORDER BY i.orderDate DESC, i.id DESC",
           countQuery = "SELECT COUNT(i) FROM ImportOrder i WHERE i.deleted=false")
    Page<ImportOrder> findAllOrdered(Pageable pageable);

    @Query("SELECT i FROM ImportOrder i LEFT JOIN FETCH i.supplier LEFT JOIN FETCH i.details d LEFT JOIN FETCH d.product WHERE i.id=:id AND i.deleted=false")
    java.util.Optional<ImportOrder> findByIdWithDetails(@Param("id") Long id);

    List<ImportOrder> findByStatusAndDeletedFalse(OrderStatus status);
}
