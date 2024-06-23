package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByParentCategoryIdAndActiveTrue(Integer parentId);

    List<Category> findByParentCategoryIsNullAndActiveTrue();

    Page<Category> findAll(Specification<Category> specification, Pageable pageable);

    boolean existsByNameAndParentCategory(String name, Category parentCategory);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN TRUE ELSE FALSE END FROM Product p WHERE p.category.id = :categoryId")
    boolean existsProductWithCategory(@Param("categoryId") Integer categoryId);

}
