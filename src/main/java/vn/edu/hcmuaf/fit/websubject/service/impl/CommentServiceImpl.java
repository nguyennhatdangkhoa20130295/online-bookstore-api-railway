package vn.edu.hcmuaf.fit.websubject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.*;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.service.CommentService;
import vn.edu.hcmuaf.fit.websubject.repository.CommentRepository;
import vn.edu.hcmuaf.fit.websubject.repository.ProductRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger Log = Logger.getLogger(CommentServiceImpl.class);
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Comment> getListCommentByProductId(int idProduct) {
        return commentRepository.findAllByProductId(idProduct);
    }

    @Override
    public List<Comment> getListCommentByUserIdAndProductId(int idUser, int idProduct) {
        return commentRepository.findAllByUserIdAndProductId(idUser, idProduct);
    }

    @Override
    public Comment getCommentById(int idComment) {
        return commentRepository.findById(idComment).orElse(null);
    }

    public Page<Comment> getAllComments(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return commentRepository.findAll(pageable);
    }

    public Page<Comment> findAll(int page, int size, String sort, String order, String filter) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }
        Sort sortPa = Sort.by(direction, sort);
        Pageable pageable = PageRequest.of(page, size, sortPa);

        JsonNode jsonFilter;
        try {
            jsonFilter = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Specification<Blog> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (jsonFilter.has("q")) {
                String searchStr = jsonFilter.get("q").asText();
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("cmtDetail")), "%" + searchStr.toLowerCase() + "%");
            }
            return predicate;
        };

        return commentRepository.findAll(specification, pageable);
    }

    @Override
    public void addComment(int idProduct, int rate, String description) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isPresent()) {
                User currentUser = user.get();
                Optional<Product> productOptional = productRepository.findById(idProduct);
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    Comment comment = new Comment();
                    comment.setProduct(product);
                    comment.setUser(currentUser);
                    comment.setRating(rate);
                    comment.setCmtDetail(description);
                    comment.setCreated_at(CurrentTime.getCurrentTimeInVietnam());
                    System.out.println(CurrentTime.getCurrentTimeInVietnam());
                    Log.info(user.get().getUserInfo().getFullName() + " đã bình luận sản phẩm " + product.getTitle() + " vào lúc " + CurrentTime.getCurrentTimeInVietnam());
                    commentRepository.save(comment);
                } else {
                    Log.warn("Sản phẩm #" + idProduct + " không tồn tại");
                    System.out.println("Không thể lưu bình luận");
                }
            }
        } catch (Exception e) {
            Log.error("Lỗi khi thêm bình luận: " + e.getMessage());
            System.out.println("Không thể lưu bình luận");
        }
    }

    @Override
    public void updateComment(int idComment, int rate, String description) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isPresent()) {
                Optional<Comment> commentOptional = commentRepository.findById(idComment);
                if (commentOptional.isPresent()) {
                    Comment currentCmt = commentOptional.get();
                    currentCmt.setRating(rate);
                    currentCmt.setCmtDetail(description);
                    currentCmt.setUpdated_at(CurrentTime.getCurrentTimeInVietnam());
                    Log.info(user.get().getUserInfo().getFullName() + " đã cập nhật bình luận vào lúc " + CurrentTime.getCurrentTimeInVietnam());
                    commentRepository.save(currentCmt);
                } else {
                    Log.warn("Bình luận #" + idComment + " không tồn tại");
                    System.out.println("Bình luận không tồn tại");
                }
            } else {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                System.out.println("Người dùng không tồn tại");
            }
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật bình luận: " + e.getMessage());
            System.out.println("Không thể cập nhật bình luận");
        }
    }

    @Override
    public void deleteComment(int idComment) {
        try {
            Log.info("Đã xóa bình luận #" + idComment);
            commentRepository.deleteById(idComment);
        } catch (Exception e) {
            Log.error("Lỗi khi xóa bình luận: " + e.getMessage());
            System.out.println("Không thể xóa bình luận");
        }
    }

}
