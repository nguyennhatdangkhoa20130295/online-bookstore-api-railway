package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.Inventory;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer>, JpaSpecificationExecutor<Inventory> {
    Optional<Inventory> findByProductId(int productId);
    Optional<Inventory> findByProductIdAndActiveTrue(int productId);
}
