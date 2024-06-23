package vn.edu.hcmuaf.fit.websubject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.*;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.repository.*;
import vn.edu.hcmuaf.fit.websubject.service.ProductService;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.log4j.Logger;

import java.util.*;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger Log = Logger.getLogger(ProductServiceImpl.class);
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductDetailRepository productDetailRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    private static final String PREFIX = "978"; // Prefix cố định của SKU
    private static final int TOTAL_LENGTH = 13; // Độ dài tổng cộng của SKU
    private static final Random RANDOM = new SecureRandom();

    public static String generateRandomSKU() {
        StringBuilder skuBuilder = new StringBuilder(PREFIX);

        int remainingLength = TOTAL_LENGTH - PREFIX.length();

        for (int i = 0; i < remainingLength; i++) {
            int digit = RANDOM.nextInt(10);
            skuBuilder.append(digit);
        }

        return skuBuilder.toString();
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Page<Product> getAllProducts(int page, int perPage, String sort, String filter, String order) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("DESC"))
            direction = Sort.Direction.DESC;

        JsonNode filterJson;
        try {
            filterJson = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (filterJson.has("title")) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("title"), "%" + filterJson.get("title").asText() + "%"));
            }
            if (filterJson.has("active")) {
                Boolean active = Boolean.valueOf(filterJson.get("active").asText());
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("active"), active));
            }
            return predicate;
        };
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(direction, sort));
        return productRepository.findAll(specification, pageRequest);
    }

    @Override
    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }

    @Override
    public List<Product> getAllProductWithPromotion() {
        List<Product> allProducts = productRepository.findAll();
        List<Product> productsWithPromotion = new ArrayList<>();
        for (Product product : allProducts) {
            Optional<Promotion> promotion = promotionRepository.findByProductId(product.getId());
            if (promotion.isPresent()) {
                productsWithPromotion.add(product);
            }
        }
        return productsWithPromotion;
    }

    @Override
    public void setDiscountPrice(int id, int discountPrice) {
        Optional<Product> optionalProduct = productRepository.findById(id);
        if (optionalProduct.isPresent()) {
            Product product = optionalProduct.get();
            if (discountPrice == product.getOldPrice()) {
                product.setCurrentPrice(product.getOldPrice());
            } else {
                product.setCurrentPrice(discountPrice);
            }
            productRepository.save(product);
        }
    }


    @Override
    public Page<Product> getProductsByCategory(Integer categoryId, int page, int perPage, String sort, String filter, String order) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("DESC")) {
            direction = Sort.Direction.DESC;
        }

        JsonNode filterJson;
        try {
            filterJson = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Specification<Product> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            // Lọc theo tiêu đề sản phẩm
            if (filterJson.has("title")) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("title"), "%" + filterJson.get("title").asText() + "%"));
            }
            if (filterJson.has("currentPrice")) {
                String priceRange = filterJson.get("currentPrice").asText();
                String[] prices = priceRange.split("-");
                if (prices.length == 2) {
                    try {
                        double minPrice = Double.parseDouble(prices[0]);
                        double maxPrice = Double.parseDouble(prices[1]);
                        predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("currentPrice"), minPrice, maxPrice));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                } else if (prices.length == 1) {
                    try {
                        double minPrice = Double.parseDouble(prices[0]);
                        predicate = criteriaBuilder.and(predicate, criteriaBuilder.greaterThanOrEqualTo(root.get("currentPrice"), minPrice));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Lọc theo danh mục, danh mục cha và danh mục cha của danh mục cha
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("category").get("id"), categoryId),
                    criteriaBuilder.equal(root.get("category").get("parentCategory").get("id"), categoryId),
                    criteriaBuilder.equal(root.get("category").get("parentCategory").get("parentCategory").get("id"), categoryId)
            ));
            Predicate activePredicate = criteriaBuilder.equal(root.get("active"), true);

            return criteriaBuilder.and(predicate, activePredicate);
        };
        switch (sort) {
            case "atoz", "ztoa" -> {
                return productRepository.findAll(specification, PageRequest.of(page, perPage, Sort.by(direction, "title")));
            }
            case "price-asc", "price-desc" -> {
                return productRepository.findAll(specification, PageRequest.of(page, perPage, Sort.by(direction, "currentPrice")));
            }
            case "latest" -> {
                return productRepository.findAll(specification, PageRequest.of(page, perPage, Sort.by(direction, "id")));
            }
        }

        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(direction, sort));
        return productRepository.findAll(specification, pageRequest);
    }

    @Override
    public List<Product> getThreeLatestProduct() {
        return productRepository.findTop3ByActiveTrueOrderByIdDesc();
    }

    @Override
    public List<Product> getProductsByCategoryId(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getFeatureProducts() {
        return productRepository.findRandomProducts();
    }

    @Override
    public List<Product> getTopReviewProducts() {
        return productRepository.findTopReviewProducts();
    }

    @Override
    public List<Product> getTopSellingProducts(int limit) {
        List<Product> bestSellingProductsFromOrderDetail = orderDetailRepository.findBestSellingProducts(limit);

        if (bestSellingProductsFromOrderDetail.size() < limit) {
            int additionalProductsNeeded = limit - bestSellingProductsFromOrderDetail.size();
            Set<Integer> ids = toIdSet(bestSellingProductsFromOrderDetail);
            List<Product> additionalProducts = productRepository.findTopNProductsNotInSet(additionalProductsNeeded, ids);
            bestSellingProductsFromOrderDetail.addAll(additionalProducts);
        }

        return bestSellingProductsFromOrderDetail;
    }

    private Set<Integer> toIdSet(List<Product> products) {
        Set<Integer> idSet = new HashSet<>();
        for (Product product : products) {
            idSet.add(product.getId());
        }
        return idSet;
    }

    @Override
    public Product createProduct(Product product) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                throw new RuntimeException("User not found");
            }
            User user = userOptional.get();

            Product newProduct = new Product();
            newProduct.setCategory(product.getCategory());
            newProduct.setTitle(product.getTitle());
            newProduct.setImage(product.getImage());
            newProduct.setOldPrice(product.getOldPrice());
            newProduct.setCurrentPrice(product.getCurrentPrice());
            newProduct.setActive(product.isActive());
            newProduct.setCreatedAt(CurrentTime.getCurrentTimeInVietnam());
            newProduct.setCreatedBy(user);
            newProduct.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
            newProduct.setUpdatedBy(user);

            Product savedProduct = productRepository.save(newProduct);
            System.out.println(savedProduct);

            ProductDetail detail = new ProductDetail();
            detail.setProduct(savedProduct);
            detail.setProductSku(generateRandomSKU());
            detail.setSupplier(product.getDetail().getSupplier());
            detail.setPublisher(product.getDetail().getPublisher());
            detail.setPublishYear(product.getDetail().getPublishYear());
            detail.setAuthor(product.getDetail().getAuthor());
            detail.setBrand(product.getDetail().getBrand());
            detail.setOrigin(product.getDetail().getOrigin());
            detail.setColor(product.getDetail().getColor());
            detail.setWeight(product.getDetail().getWeight());
            detail.setSize(product.getDetail().getSize());
            detail.setQuantityOfPage(product.getDetail().getQuantityOfPage());
            detail.setDescription(product.getDetail().getDescription());

            ProductDetail savedDetail = productDetailRepository.save(detail);
            savedProduct.setDetail(savedDetail);

            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            }

            List<ProductImage> productImages = new ArrayList<>();

            for (ProductImage productImage : product.getImages()) {
                ProductImage newProductImage = new ProductImage();
                newProductImage.setProduct(savedProduct);
                newProductImage.setImage(productImage.getImage());
                newProductImage.setCreatedAt(CurrentTime.getCurrentTimeInVietnam());
                newProductImage.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
                newProductImage.setDeleted(false);
                productImages.add(newProductImage);
            }
            productImageRepository.saveAll(productImages);

            savedProduct.setImages(productImages);
            Log.info("Người dùng " + customUserDetails.getUsername() + " đã tạo sản phẩm mới " + savedProduct.getTitle());
            return productRepository.save(savedProduct);
        } catch (Exception e) {
            Log.error("Lỗi khi tạo mới sản phẩm: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Product updateProduct(Integer productId, Product product) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found");
            }
            User user = userOptional.get();

            Optional<Product> optionalProduct = productRepository.findById(productId);
            if (optionalProduct.isEmpty()) {
                throw new RuntimeException("Product not found");
            }
            Product existingProduct = optionalProduct.get();

            existingProduct.setCategory(product.getCategory());
            existingProduct.setTitle(product.getTitle());
            existingProduct.setOldPrice(product.getOldPrice());
            existingProduct.setCurrentPrice(product.getCurrentPrice());
            existingProduct.setActive(product.isActive());
            existingProduct.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
            existingProduct.setUpdatedBy(user);

            ProductDetail existedDetail = existingProduct.getDetail();
            if (existedDetail == null) {
                existedDetail = new ProductDetail();
                existedDetail.setProduct(existingProduct);
            }
            existedDetail.setSupplier(product.getDetail().getSupplier());
            existedDetail.setPublisher(product.getDetail().getPublisher());
            existedDetail.setPublishYear(product.getDetail().getPublishYear());
            existedDetail.setAuthor(product.getDetail().getAuthor());
            existedDetail.setBrand(product.getDetail().getBrand());
            existedDetail.setOrigin(product.getDetail().getOrigin());
            existedDetail.setColor(product.getDetail().getColor());
            existedDetail.setWeight(product.getDetail().getWeight());
            existedDetail.setSize(product.getDetail().getSize());
            existedDetail.setQuantityOfPage(product.getDetail().getQuantityOfPage() != 0 ? product.getDetail().getQuantityOfPage() : -1);
            existedDetail.setDescription(product.getDetail().getDescription());

            ProductDetail savedDetail = productDetailRepository.save(existedDetail);
            existingProduct.setDetail(savedDetail);

            String newMainImageUrl = product.getImage();
            if (newMainImageUrl != null && !newMainImageUrl.isEmpty() && !newMainImageUrl.equals(existingProduct.getImage())) {
                existingProduct.setImage(newMainImageUrl);
            }

            List<ProductImage> newProductImages = new ArrayList<>();
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                for (ProductImage updateImage : product.getImages()) {
                    ProductImage existingProductImage = null;
                    for (ProductImage productImage : existingProduct.getImages()) {
                        if (updateImage.getImage() != null && updateImage.getImage().equals(productImage.getImage())) {
                            existingProductImage = productImage;
                            break;
                        }
                    }

                    if (existingProductImage == null) {
                        ProductImage newImage = new ProductImage();
                        newImage.setImage(updateImage.getImage());
                        newImage.setCreatedAt(CurrentTime.getCurrentTimeInVietnam());
                        newImage.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
                        newImage.setDeleted(false);
                        newImage.setProduct(existingProduct);
                        newProductImages.add(newImage);
                    } else {
                        newProductImages.add(existingProductImage);
                    }
                }
            } else {
                newProductImages = existingProduct.getImages();
            }

            List<ProductImage> imagesToDelete = new ArrayList<>();
            for (ProductImage existingProductImage : existingProduct.getImages()) {
                boolean found = false;
                for (ProductImage newProductImage : newProductImages) {
                    if (existingProductImage.getImage() != null && existingProductImage.getImage().equals(newProductImage.getImage())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    imagesToDelete.add(existingProductImage);
                }
            }
            for (ProductImage imageToDelete : imagesToDelete) {
                productImageRepository.delete(imageToDelete);
            }
            existingProduct.setImages(newProductImages);

            Log.info("Người dùng " + customUserDetails.getUsername() + " đã cập nhật sản phẩm " + existingProduct.getTitle());
            return productRepository.save(existingProduct);
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
