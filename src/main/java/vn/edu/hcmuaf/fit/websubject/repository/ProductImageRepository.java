package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmuaf.fit.websubject.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
}
