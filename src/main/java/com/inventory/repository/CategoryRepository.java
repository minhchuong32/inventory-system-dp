package com.inventory.repository;

import com.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByDeletedFalseOrderByNameAsc();
    List<Category> findByParentCategoryIsNullAndDeletedFalse();
    List<Category> findByParentCategoryIdAndDeletedFalse(Long parentId);
    boolean existsByNameAndDeletedFalse(String name);

    @Query("SELECT c FROM Category c WHERE c.deleted = false ORDER BY c.sortOrder, c.name")
    List<Category> findAllActive();
}
