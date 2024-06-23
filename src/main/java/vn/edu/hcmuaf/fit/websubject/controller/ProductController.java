package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.Product;
import vn.edu.hcmuaf.fit.websubject.service.ProductService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "24") int perPage,
                                                        @RequestParam(defaultValue = "id") String sort,
                                                        @RequestParam(defaultValue = "{}") String filter,
                                                        @RequestParam(defaultValue = "DESC") String order) {
        Page<Product> products = productService.getAllProducts(page, perPage, sort, filter, order);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Integer id) {
        Optional<Product> productOptional = productService.getProductById(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            return ResponseEntity.ok().body(product);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/promotion")
    public List<Product> getPromotionProducts() {
        return productService.getAllProductWithPromotion();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Integer categoryId,
                                                   @RequestParam(required = false) Integer page,
                                                   @RequestParam(required = false) Integer perPage,
                                                   @RequestParam(defaultValue = "id") String sort,
                                                   @RequestParam(defaultValue = "{}") String filter,
                                                   @RequestParam(defaultValue = "ASC") String order) {
        if (page == null || perPage == null) {
            List<Product> products = productService.getProductsByCategoryId(categoryId);
            return ResponseEntity.ok(products);
        } else {
            Page<Product> products = productService.getProductsByCategory(categoryId, page, perPage, sort, filter, order);
            return ResponseEntity.ok(products);
        }
    }

    @PutMapping("/set_discount/{id}/price/{newPrice}")
    public ResponseEntity<?> setDiscount(@PathVariable Integer id, @PathVariable Integer newPrice) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        } else {
            productService.setDiscountPrice(id, newPrice);
            return ResponseEntity.ok().build();
        }
    }

    @GetMapping("/latest")
    public List<Product> getThreeLatestProducts() {
        return productService.getThreeLatestProduct();
    }

    @GetMapping("/feature")
    public List<Product> getFeatureProducts() {
        return productService.getFeatureProducts();
    }

    @GetMapping("/top_review")
    public List<Product> getTopReviewProducts() {
        return productService.getTopReviewProducts();
    }

    @GetMapping("/top_selling")
    public List<Product> getTopSellingProducts(@RequestParam int limit) {
        return productService.getTopSellingProducts(limit);
    }

    @PostMapping("/add")
    @Transactional
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<?> saveProduct(@RequestBody Product product) {
        try {
            System.out.println(product);
            Product savedProduct = productService.createProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Transactional
    @PutMapping("/edit/{productId}")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Integer productId, @RequestBody Product product) {
        System.out.println(product);
        Product updatedProduct = productService.updateProduct(productId, product);
        return ResponseEntity.status(HttpStatus.OK).body(updatedProduct);
    }

}
