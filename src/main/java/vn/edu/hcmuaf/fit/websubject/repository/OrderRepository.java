package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.Order;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    List<Order> findByUserIdOrderByIdDesc(Integer id);

    @Query("""
        select o from Order o inner join OrderDetail od on o.id = od.order.id inner join Product p on od.product.id = p.id 
        where p.id = :productId and od.order.user.id = :userId and o.status.id = 5
    """)
    List<Order> findByProductIdAndUserId(Integer productId, Integer userId);

    @Query("""
        select o from Order o inner join Promotion p on o.promotion.id = p.id where p.code = :promoCode and o.user.id = :userId
    """)
    List<Order> findByPromoCode(String promoCode, Integer userId);

    Order findTopByUserIdOrderByIdDesc(Integer userId);

}
