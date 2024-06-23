package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.CartItem;
import vn.edu.hcmuaf.fit.websubject.service.CartItemsService;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartItemsService cartItemsService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem cartItem) {
        try {
            return cartItemsService.addToCart(cartItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<String> removeFromCart(@PathVariable int cartItemId) {
        try {
            cartItemsService.removeFromCart(cartItemId);
            return ResponseEntity.ok("Removed from cart successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/items")
    public ResponseEntity<List<CartItem>> getCartItems() {
        try {
            List<CartItem> cartItems = cartItemsService.getCartItems();
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/increase/{cartItemId}")
    public ResponseEntity<String> increaseCartItemQuantity(@PathVariable int cartItemId) {
        try {
            cartItemsService.increaseCartItemQuantity(cartItemId);
            return ResponseEntity.ok("Increased quantity successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to increase quantity");
        }
    }

    @PutMapping("/decrease/{cartItemId}")
    public ResponseEntity<String> decreaseCartItemQuantity(@PathVariable int cartItemId) {
        try {
            cartItemsService.decreaseCartItemQuantity(cartItemId);
            return ResponseEntity.ok("Decreased quantity successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to decrease quantity");
        }
    }
}
