package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.hcmuaf.fit.websubject.entity.OrderDetail;
import vn.edu.hcmuaf.fit.websubject.entity.Product;

import java.util.List;


public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @Query("SELECT od.product FROM OrderDetail od GROUP BY od.product.id ORDER BY SUM(od.quantity) DESC")
    List<Product> findBestSellingProducts(int limit);

    List<OrderDetail> findByOrderId(Integer orderId);
}
