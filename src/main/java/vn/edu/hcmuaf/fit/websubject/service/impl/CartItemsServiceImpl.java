package vn.edu.hcmuaf.fit.websubject.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.CartItem;
import vn.edu.hcmuaf.fit.websubject.entity.Inventory;
import vn.edu.hcmuaf.fit.websubject.entity.Product;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.repository.CartItemsRepository;
import vn.edu.hcmuaf.fit.websubject.repository.InventoryRepository;
import vn.edu.hcmuaf.fit.websubject.repository.ProductRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.CartItemsService;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemsServiceImpl implements CartItemsService {
    private static final Logger Log = Logger.getLogger(CartItemsServiceImpl.class);
    @Autowired
    CartItemsRepository cartItemsRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    public ResponseEntity<?> addToCart(CartItem cartItems) {
        try {
            Optional<Product> productOptional = productRepository.findById(cartItems.getProduct().getId());
            if (productOptional.isPresent()) {
                Product product = productOptional.get();

                Optional<Inventory> inventoryOptional = inventoryRepository.findByProductId(product.getId());

                if (inventoryOptional.isEmpty()) {
                    throw new RuntimeException("Inventory not found");
                }
                Inventory inventory = inventoryOptional.get();
                int availableQuantity = inventory.getRemainingQuantity();

                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
                Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());

                if (user.isPresent()) {
                    User currentUser = user.get();

                    CartItem existingCartItem = cartItemsRepository.findByProductIdAndUserId(cartItems.getProduct().getId(), currentUser.getId());
                    int requestedQuantity = cartItems.getQuantity();
                    if (existingCartItem != null) {
                        requestedQuantity += existingCartItem.getQuantity();
                    }
                    if (requestedQuantity > availableQuantity) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không đủ số lượng sản phẩm trong kho!");
                    }
                    if (existingCartItem != null) {
                        existingCartItem.setQuantity(requestedQuantity);
                        cartItemsRepository.save(existingCartItem);
                    } else {
                        CartItem newCartItem = new CartItem();
                        newCartItem.setProduct(product);
                        newCartItem.setQuantity(cartItems.getQuantity());
                        newCartItem.setUser(currentUser);
                        cartItemsRepository.save(newCartItem);
                    }
                    return ResponseEntity.ok("Sản phẩm đã được thêm vào giỏ hàng!");
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Người dùng không được xác thực");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm");
            }
        } catch (Exception e) {
            Log.error("Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm sản phẩm vào giỏ hàng");
        }
    }

    @Override
    public void removeFromCart(int cartItemId) {
        try {
            cartItemsRepository.deleteById(cartItemId);
            Log.info("Đã xóa sản phẩm với id #" + cartItemId + " khỏi giỏ hàng");
        } catch (Exception e) {
            Log.error("Lỗi khi xóa sản phẩm với id #" + cartItemId + " khỏi giỏ hàng");
            System.out.println("Lỗi khi xóa sản phẩm khỏi giỏ hàng");
        }
    }

    @Override
    public List<CartItem> getCartItems() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
        return cartItemsRepository.findAllByUserId(user.get().getId());
    }

    @Override
    public void increaseCartItemQuantity(int cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemsRepository.findById(cartItemId);
        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            int newQuantity = cartItem.getQuantity() + 1;
            cartItem.setQuantity(newQuantity);
            cartItemsRepository.save(cartItem);
        }
    }

    public void decreaseCartItemQuantity(int cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemsRepository.findById(cartItemId);
        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            int newQuantity = cartItem.getQuantity() - 1;
            if (newQuantity <= 0) {
                System.out.println("không thể giảm thêm số lượng");
            } else {
                cartItem.setQuantity(newQuantity);
                cartItemsRepository.save(cartItem);
            }
        }
    }
}
