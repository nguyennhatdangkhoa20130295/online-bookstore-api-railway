package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.hcmuaf.fit.websubject.entity.CartItem;

import java.util.List;

public interface CartItemsRepository extends JpaRepository<CartItem, Integer> {
    CartItem findByProductIdAndUserId(Integer idProduct, Integer userId);

    List<CartItem> findAllByUserId(Integer idUser);
}
