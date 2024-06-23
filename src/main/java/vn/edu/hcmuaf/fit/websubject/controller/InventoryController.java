package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.Inventory;
import vn.edu.hcmuaf.fit.websubject.payload.request.InventoryRequest;
import vn.edu.hcmuaf.fit.websubject.service.InventoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> getAllInventories(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "10") int perPage,
                                               @RequestParam(defaultValue = "id") String sort,
                                               @RequestParam(defaultValue = "{}") String filter,
                                               @RequestParam(defaultValue = "ASC") String order) {
        Page<Inventory> inventories = inventoryService.getAllInventories(page, perPage, sort, filter, order);
        return ResponseEntity.ok(inventories);
    }

    @PostMapping("/add")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<?> createInventories(@RequestBody List<InventoryRequest> inventoryRequests) {
        List<Inventory> createdInventories = inventoryService.createInventories(inventoryRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInventories);
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<?> getByProductId(@PathVariable int productId) {
        Optional<Inventory> inventoryOptional = inventoryService.getByProductId(productId);
        if (inventoryOptional.isEmpty()) {
            throw new RuntimeException("Inventory not found");
        }
        Inventory inventory = inventoryOptional.get();
        return ResponseEntity.ok(inventory);
    }
}
