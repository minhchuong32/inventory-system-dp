package com.inventory.repository;
import com.inventory.entity.ExportOrder;
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
public interface ExportOrderRepository extends JpaRepository<ExportOrder, Long> {
    @Query("SELECT COUNT(e) FROM ExportOrder e WHERE e.deleted=false AND YEAR(e.orderDate)=:y AND MONTH(e.orderDate)=:m")
    long countByYearAndMonth(@Param("y") int year, @Param("m") int month);

    @Query("SELECT COALESCE(SUM(e.finalAmount),0) FROM ExportOrder e WHERE e.deleted=false AND e.status='COMPLETED' AND YEAR(e.orderDate)=:y AND MONTH(e.orderDate)=:m")
    BigDecimal sumAmountByYearAndMonth(@Param("y") int year, @Param("m") int month);

    @Query(value = "SELECT DISTINCT e FROM ExportOrder e LEFT JOIN FETCH e.customer WHERE e.deleted=false AND " +
                   "(:kw IS NULL OR LOWER(e.code) LIKE LOWER(CONCAT('%',:kw,'%'))) ORDER BY e.orderDate DESC, e.id DESC",
           countQuery = "SELECT COUNT(e) FROM ExportOrder e WHERE e.deleted=false AND " +
                        "(:kw IS NULL OR LOWER(e.code) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<ExportOrder> searchOrders(@Param("kw") String keyword, Pageable pageable);

    @Query(value = "SELECT DISTINCT e FROM ExportOrder e LEFT JOIN FETCH e.customer WHERE e.deleted=false ORDER BY e.orderDate DESC, e.id DESC",
           countQuery = "SELECT COUNT(e) FROM ExportOrder e WHERE e.deleted=false")
    Page<ExportOrder> findAllOrdered(Pageable pageable);

    @Query("SELECT e FROM ExportOrder e LEFT JOIN FETCH e.customer LEFT JOIN FETCH e.details d LEFT JOIN FETCH d.product WHERE e.id=:id AND e.deleted=false")
    java.util.Optional<ExportOrder> findByIdWithDetails(@Param("id") Long id);

    List<ExportOrder> findByStatusAndDeletedFalse(OrderStatus status);
}
