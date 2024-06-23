package vn.edu.hcmuaf.fit.websubject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
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
import vn.edu.hcmuaf.fit.websubject.service.OrderService;
import vn.edu.hcmuaf.fit.websubject.repository.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.log4j.Logger;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger Log = Logger.getLogger(OrderServiceImpl.class);

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderStatusRepository orderStatusRepository;

    @Autowired
    OrderDetailRepository orderDetailRepository;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    PromotionRepository promotionRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ORDER_CODE_LENGTH = 10;
    private static final Random RANDOM = new SecureRandom();

    @Override
    public List<Order> getUserOrders(Integer userId) {
        return orderRepository.findByUserIdOrderByIdDesc(userId);
    }

    @Override
    public Order getLatestOrder(Integer userId) {
        return orderRepository.findTopByUserIdOrderByIdDesc(userId);
    }

    @Override
    public Order createOrder(Order order) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                Log.info("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                throw new RuntimeException("User not found");
            }
            User user = userOptional.get();
            order.setUser(user);
            Optional<Promotion> promotionOptional = promotionRepository.findById(order.getPromotion().getId());
            if (promotionOptional.isEmpty()) {
                order.setPromotion(null);
            } else {
                Promotion promotion = promotionOptional.get();
                order.setPromotion(promotion);
            }
            order.setOrderCode(generateOrderCode());
            order.setOrderDate(CurrentTime.getCurrentTimeInVietnam());
            if (order.getPaymentMethod().equals("cashondelivery")) {
                order.setPaymentMethod("Thanh toán khi nhận hàng");
            }
            Log.info("Đơn hàng " + order.getOrderCode() + " được tạo bởi " + user.getUsername());
            return orderRepository.save(order);
        } catch (Exception e) {
            Log.error("Lỗi khi tạo đơn hàng:" + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Order getOrderByPromoCode(String code, Integer userId) {
        List<Order> orders = orderRepository.findByPromoCode(code, userId);
        return orders.isEmpty() ? null : orders.get(0);
    }

    @Override
    public void createOrderDetail(OrderDetail orderDetail) {
        updateInventory(orderDetail.getProduct().getId(), orderDetail.getQuantity());
        orderDetailRepository.save(orderDetail);
    }

    @Override
    public String generateOrderCode() {
        StringBuilder sb = new StringBuilder(ORDER_CODE_LENGTH);
        for (int i = 0; i < ORDER_CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private void updateInventoryCancel(int productId, int cancelQuantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        inventory.setRemainingQuantity(inventory.getRemainingQuantity() + cancelQuantity);
        inventory.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
        inventoryRepository.save(inventory);
    }

    private void updateInventory(int productId, int quantity) {
        try {
            Product existingProduct = productRepository.findById(productId).get();
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
            if (inventory.getRemainingQuantity() < quantity) {
                Log.warn("Không đủ hàng cho sản phẩm: " + existingProduct.getTitle());
                throw new RuntimeException("Không đủ hàng cho sản phẩm: " + existingProduct.getTitle());
            }
            inventory.setRemainingQuantity(inventory.getRemainingQuantity() - quantity);
            inventory.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
            if (inventory.getRemainingQuantity() == 0) {
                inventory.setActive(false);
            }
            Log.info("Cập nhật kho hàng cho sản phẩm: " + existingProduct.getTitle());
            inventoryRepository.save(inventory);
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật kho hàng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Order> getOrder(Integer orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getOrderByProductIdAndUserId(Integer productId, Integer userId) {
        return orderRepository.findByProductIdAndUserId(productId, userId);
    }

    @Override
    public void cancelOrder(Integer orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
            if (order.getStatus().getId() == 6) {
                Log.warn("Đơn hàng với id " + orderId + " đã bị hủy");
                throw new RuntimeException("Đơn hàng đã bị hủy");
            } else if (order.getStatus().getId() >= 2) {
                Log.warn("Đơn hàng đã được xác nhận, không thể hủy");
                throw new RuntimeException("Đơn hàng đã được xác nhận, không thể hủy");
            } else {
                OrderStatus orderStatus = orderStatusRepository.findById(6).orElseThrow(() -> new RuntimeException("Order status not found"));
                order.setStatus(orderStatus);
                Log.info("Hủy đơn hàng " + order.getOrderCode() + " thành công");
                orderRepository.save(order);
                List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
                for (OrderDetail orderDetail : orderDetails) {
                    updateInventoryCancel(orderDetail.getProduct().getId(), orderDetail.getQuantity());
                }
            }
        } catch (Exception e) {
            Log.error("Lỗi khi hủy đơn hàng: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<Order> getAllOrders(int page, int perPage, String sort, String filter, String order) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("DESC"))
            direction = Sort.Direction.DESC;

        JsonNode filterJson;
        try {
            filterJson = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Specification<Order> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (filterJson.has("q")) {
                String searchStr = filterJson.get("q").asText().toLowerCase();
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("orderCode")), "%" + searchStr + "%"));
            }
            if (filterJson.has("slug")) {
                String slug = filterJson.get("slug").asText().toLowerCase();
                Join<Order, OrderStatus> statusJoin = root.join("status", JoinType.INNER);
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(statusJoin.get("slug")), "%" + slug + "%"));
            }
            return predicate;
        };
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(direction, sort));
        return orderRepository.findAll(specification, pageRequest);
    }

    @Override
    public List<OrderStatus> getOrderStatus() {
        return orderStatusRepository.findAll();
    }

    @Override
    public void updateOrderStatus(Integer orderId, Order order) {
        Optional<Order> optionalOrder = orderRepository.findById(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        Optional<OrderStatus> optionalOrderStatus = orderStatusRepository.findById(order.getStatus().getId());
        if (optionalOrderStatus.isEmpty()) {
            throw new RuntimeException("Order status not found");
        }
        OrderStatus status = optionalOrderStatus.get();
        Order existedOrder = optionalOrder.get();
        existedOrder.setStatus(status);
        orderRepository.save(existedOrder);
    }

}
