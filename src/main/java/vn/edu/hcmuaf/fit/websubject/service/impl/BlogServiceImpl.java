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
import vn.edu.hcmuaf.fit.websubject.repository.BlogCateRepository;
import vn.edu.hcmuaf.fit.websubject.repository.BlogRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.BlogService;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BlogServiceImpl implements BlogService {

    private static final Logger Log = Logger.getLogger(BlogServiceImpl.class);

    private BlogRepository blogRepository;
    private UserRepository userRepository;
    private BlogCateRepository blogCateRepository;

    @Autowired
    public BlogServiceImpl(BlogRepository blogRepository, UserRepository userRepository, BlogCateRepository blogCateRepository) {
        this.blogRepository = blogRepository;
        this.userRepository = userRepository;
        this.blogCateRepository = blogCateRepository;
    }


    public Page<Blog> getAllBlogs(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return blogRepository.findAll(pageable);
    }

    public List<Blog> getAllBlogsUser() {
        return blogRepository.findAll();
    }

    @Override
    public List<Blog> getBlogsByCateId(int cateId) {
        return blogRepository.findAllByBlogCateId(cateId);
    }

    @Override
    public Page<Blog> getBlogByCate(Integer categoryId, int page, int perPage, String sort, String filter, String order) {
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
            // Lọc theo danh mục
            predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
                    criteriaBuilder.equal(root.get("blogCate").get("id"), categoryId)
            ));

            return predicate;
        };

        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(direction, sort));
        return blogRepository.findAll(specification, pageRequest);
    }

    public Page<Blog> findAll(int page, int size, String sort, String order, String filter) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
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
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + searchStr.toLowerCase() + "%");
            }
            return predicate;
        };

        return blogRepository.findAll(specification, pageable);
    }

    @Override
    public void addBlog(int blogCate, String title, String content, String image) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isPresent()) {
                User currentUser = user.get();
                Optional<BlogCategory> blogCategory = blogCateRepository.findById(blogCate);
                if (blogCategory.isPresent()) {
                    BlogCategory presentBlogCate = blogCategory.get();
                    Blog newBlog = new Blog();
                    newBlog.setBlogCate(presentBlogCate);
                    newBlog.setCreatedBy(currentUser);
                    newBlog.setTitle(title);
                    newBlog.setContent(content);
                    if (content.length() > 100) {
                        newBlog.setShortDesc(content.substring(0, 100));
                    } else {
                        newBlog.setShortDesc(content);
                    }
                    newBlog.setImage(image);
                    newBlog.setUpdateBy(currentUser);
                    newBlog.setCreatedAt(getCurrentTimeInVietnam());
                    newBlog.setUpdatedAt(getCurrentTimeInVietnam());
                    blogRepository.save(newBlog);
                }
            } else {
                Log.warn("Không tìm thấy user " + customUserDetails.getUsername());
                System.out.println("Không tìm thấy user hiện tại");
            }
        } catch (Exception e) {
            Log.error("Lỗi khi thêm blog " + e.getMessage());
            System.out.println(e);
        }
    }

    @Override
    public void editBlog(int id, int blogCate, String title, String content, String image) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isPresent()) {
                User currentUser = user.get();
                Optional<BlogCategory> blogCategory = blogCateRepository.findById(blogCate);
                if (blogCategory.isPresent()) {
                    BlogCategory presentBlogCate = blogCategory.get();
                    Optional<Blog> blog = blogRepository.findById(id);
                    if (blog.isPresent()) {
                        Blog presentBlog = blog.get();
                        presentBlog.setBlogCate(presentBlogCate);
                        presentBlog.setUpdateBy(currentUser);
                        presentBlog.setTitle(title);
                        presentBlog.setContent(content);
                        if (content.length() > 100) {
                            presentBlog.setShortDesc(content.substring(0, 100));
                        } else {
                            presentBlog.setShortDesc(content);
                        }
                        presentBlog.setImage(image);
                        presentBlog.setUpdatedAt(getCurrentTimeInVietnam());
                        blogRepository.save(presentBlog);
                    }
                }
            } else {
                Log.warn("Không tìm thấy user " + customUserDetails.getUsername());
                System.out.println("Không tìm thấy user hiện tại");
            }
        } catch (Exception e) {
            Log.error("Lỗi khi sửa blog " + e.getMessage());
            System.out.println(e);
        }
    }

    @Override
    public void deleteBlog(int id) {
        try {
            blogRepository.deleteById(id);
            Log.info("Xóa blog với id #" + id + " thành công");
        } catch (Exception e) {
            Log.error("Lỗi khi xóa blog " + e.getMessage());
            System.out.println(e);
        }
    }

    public static Date getCurrentTimeInVietnam() {
        ZoneId zoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime localDateTime = LocalDateTime.now(zoneId);
        return Date.from(localDateTime.atZone(zoneId).toInstant());
    }

    public Optional<Blog> getBlogById(int id) {
        return blogRepository.findById(id);
    }

//    public Optional<Blog> getBlogByCate(int cateId) {
//        return blogRepository.findByBlogCateId(cateId);
//    }
}
