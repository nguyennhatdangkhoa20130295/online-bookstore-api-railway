package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.Promotion;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Integer>, JpaSpecificationExecutor {
    Optional<Promotion> findByProductId(Integer productId);

    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.isCode = true")
    List<Promotion> findByIsCode ();

}
