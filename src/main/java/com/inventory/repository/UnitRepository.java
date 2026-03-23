package com.inventory.repository;
import com.inventory.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    List<Unit> findByDeletedFalseOrderByNameAsc();
    boolean existsByNameAndDeletedFalse(String name);
}
