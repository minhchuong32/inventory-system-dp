package com.inventory.repository;
import com.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    Page<StockMovement> findByProductIdAndDeletedFalseOrderByCreatedAtDesc(Long productId, Pageable pageable);

    @Query("SELECT sm FROM StockMovement sm WHERE sm.product.id=:pid AND sm.deleted=false " +
           "AND sm.createdAt BETWEEN :from AND :to ORDER BY sm.createdAt DESC")
    List<StockMovement> findByProductAndDateRange(@Param("pid") Long productId,
        @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
