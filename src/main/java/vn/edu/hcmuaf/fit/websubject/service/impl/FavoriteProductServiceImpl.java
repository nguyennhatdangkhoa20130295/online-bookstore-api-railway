package vn.edu.hcmuaf.fit.websubject.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.FavoriteProduct;
import vn.edu.hcmuaf.fit.websubject.entity.Product;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.repository.FavoriteProductRepository;
import vn.edu.hcmuaf.fit.websubject.repository.ProductRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.FavoriteProductService;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

@Service
public class FavoriteProductServiceImpl implements FavoriteProductService {
    private static final Logger Log = Logger.getLogger(FavoriteProductServiceImpl.class);
    @Autowired
    private FavoriteProductRepository favoriteProductRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<FavoriteProduct> getAllFavoriteProducts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
        if (user.isEmpty()) {
            Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
            throw new RuntimeException("User not found");
        }
        return favoriteProductRepository.findAllByUserId(user.get().getId());
    }

    @Override
    public FavoriteProduct addFavorite(Integer productId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isEmpty()) {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                throw new RuntimeException("User not found");
            }
            FavoriteProduct existFavorite = favoriteProductRepository.findByProductId(productId);
            if (existFavorite != null) {
                return null;
            } else {
                Optional<Product> productOptional = productRepository.findById(productId);
                if (productOptional.isEmpty()) {
                    Log.warn("Sản phẩm #" + productId + " không tồn tại");
                    throw new RuntimeException("Product not found");
                }
                Product product = productOptional.get();
                FavoriteProduct favoriteProduct = new FavoriteProduct();
                favoriteProduct.setProduct(product);
                favoriteProduct.setUser(user.get());
                Log.info("Người dùng " + customUserDetails.getUsername() + " đã thêm sản phẩm " + product.getTitle() + " vào danh sách yêu thích");
                return favoriteProductRepository.save(favoriteProduct);
            }
        } catch (Exception e) {
            Log.error("Lỗi khi thêm sản phẩm vào danh sách yêu thích: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteFavorite(Integer id) {
        try {
            Log.info("Người dùng đã xóa sản phẩm #" + id + " khỏi danh sách yêu thích");
            favoriteProductRepository.deleteById(id);
        } catch (Exception e) {
            Log.error("Lỗi khi xóa sản phẩm khỏi danh sách yêu thích: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
