package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmuaf.fit.websubject.entity.ProductDetail;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Integer> {
}
