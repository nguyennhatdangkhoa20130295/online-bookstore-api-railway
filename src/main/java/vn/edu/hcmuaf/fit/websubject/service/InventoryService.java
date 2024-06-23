package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.websubject.entity.Inventory;
import vn.edu.hcmuaf.fit.websubject.entity.Product;
import vn.edu.hcmuaf.fit.websubject.payload.request.InventoryRequest;

import java.util.List;
import java.util.Optional;

public interface InventoryService {
    Optional<Inventory> getByProductId(int productId);

    Page<Inventory> getAllInventories(int page, int perPage, String sort, String filter, String order);

    Inventory createInventory(Inventory inventory);

    List<Inventory> createInventories(List<InventoryRequest> inventoryRequests);
}
