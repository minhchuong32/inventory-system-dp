package com.inventory.repository;
import com.inventory.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByCodeAndDeletedFalse(String code);
    List<Supplier> findByStatusTrueAndDeletedFalse();
    boolean existsByCodeAndDeletedFalse(String code);
    long countByStatusTrueAndDeletedFalse();

    @Query("SELECT s FROM Supplier s WHERE s.deleted = false AND " +
           "(:kw IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%',:kw,'%')) " +
           "OR LOWER(s.code) LIKE LOWER(CONCAT('%',:kw,'%')))")
    Page<Supplier> searchSuppliers(@Param("kw") String keyword, Pageable pageable);
}
