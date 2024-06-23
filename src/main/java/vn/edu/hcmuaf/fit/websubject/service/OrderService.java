package vn.edu.hcmuaf.fit.websubject.service;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.websubject.entity.Order;
import vn.edu.hcmuaf.fit.websubject.entity.OrderDetail;
import vn.edu.hcmuaf.fit.websubject.entity.OrderStatus;
import vn.edu.hcmuaf.fit.websubject.entity.Product;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getUserOrders(Integer userId);

    Order getLatestOrder(Integer userId);

    Order createOrder(Order order);

    Order getOrderByPromoCode(String code, Integer userId);

    void createOrderDetail(OrderDetail orderDetail);

    String generateOrderCode();

    Optional<Order> getOrder(Integer orderId);

    List<Order> getOrderByProductIdAndUserId(Integer productId, Integer userId);

    void cancelOrder(Integer orderId);

    Page<Order> getAllOrders(int page, int perPage, String sort, String filter, String order);

    List<OrderStatus> getOrderStatus();

    void updateOrderStatus(Integer orderId, Order order);
}
