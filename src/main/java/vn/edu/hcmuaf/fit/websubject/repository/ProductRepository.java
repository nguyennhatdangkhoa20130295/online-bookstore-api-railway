package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    List<Product> findTop3ByActiveTrueOrderByIdDesc();

    Optional<Product> findById(Integer id);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId OR p.category.parentCategory.id = :categoryId OR p.category.parentCategory.parentCategory.id = :categoryId")
    List<Product> findByCategoryId(Integer categoryId);

    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY RAND() LIMIT 3")
    List<Product> findRandomProducts();

    @Query("SELECT p FROM Product p LEFT JOIN p.comments c WHERE p.active = true GROUP BY p.id ORDER BY COUNT(c) DESC LIMIT 2")
    List<Product> findTopReviewProducts();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.id NOT IN :ids ORDER BY p.id ASC LiMIT :limit")
    List<Product> findTopNProductsNotInSet(@Param("limit") int limit, @Param("ids") Set<Integer> ids);
}
