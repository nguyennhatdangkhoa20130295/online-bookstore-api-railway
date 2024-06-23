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
import vn.edu.hcmuaf.fit.websubject.entity.Category;
import org.apache.log4j.Logger;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.repository.CategoryRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.CategoryService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime.getCurrentTimeInVietnam;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final Logger Log = Logger.getLogger(CategoryServiceImpl.class);
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> getAllCategories(int page, int perPage, String sort, String filter, String order) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("DESC"))
            direction = Sort.Direction.DESC;

        JsonNode filterJson;
        try {
            filterJson = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Specification<Category> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();
            if (filterJson.has("name")) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.like(root.get("name"), "%" + filterJson.get("name").asText() + "%"));
            }
            if (filterJson.has("parentCategory")) {
                JsonNode parentCategoryNode = filterJson.get("parentCategory");

                if (parentCategoryNode.isNull()) {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.isNull(root.get("parentCategory"))
                    );
                } else {
                    predicate = criteriaBuilder.and(
                            predicate,
                            criteriaBuilder.equal(root.get("parentCategory").get("id"), parentCategoryNode.asInt())
                    );
                }
            }
            if (filterJson.has("active")) {
                Boolean active = Boolean.valueOf(filterJson.get("active").asText());
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("active"), active));
            }

            return predicate;
        };
        if (sort.equals("id")) {
            return categoryRepository.findAll(specification, PageRequest.of(page, perPage, Sort.by(direction, "id")));
        }
        if (sort.equals("name")) {
            return categoryRepository.findAll(specification, PageRequest.of(page, perPage, Sort.by(direction, "name")));
        }
        if (sort.equals("createdAt")) {
            return categoryRepository.findAll(specification, PageRequest.of(page, perPage, Sort.by(direction, "createdAt")));
        }
        PageRequest pageRequest = PageRequest.of(page, perPage, Sort.by(direction, sort));
        return categoryRepository.findAll(specification, pageRequest);
    }

    @Override
    public List<Category> getMainCategories() {
        return categoryRepository.findByParentCategoryIsNullAndActiveTrue();
    }

    @Override
    public List<Category> getSubCategories(Integer parentId) {
        return categoryRepository.findByParentCategoryIdAndActiveTrue(parentId);
    }

    @Override
    public Optional<Category> getCategoryById(Integer id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Category createCategory(Category category) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> user = userRepository.findByUsername(customUserDetails.getUsername());
            if (user.isPresent()) {
                User currentUser = user.get();
                boolean existedCategory = categoryRepository.existsByNameAndParentCategory(category.getName(), category.getParentCategory());
                if (!existedCategory) {
                    Category newCategory = new Category();
                    newCategory.setName(category.getName());
                    newCategory.setParentCategory(category.getParentCategory());
                    newCategory.setCreatedBy(currentUser);
                    newCategory.setCreatedAt(getCurrentTimeInVietnam());
                    newCategory.setActive(category.isActive());
                    Log.info(currentUser.getUserInfo().getFullName() + " đã tạo danh mục " + newCategory.getName());
                    return categoryRepository.save(newCategory);
                } else {
                    Log.warn("Danh mục " + category.getName() + " đã tồn tại");
                    System.out.println("Không thể tạo danh mục mới");
                }
            } else {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                System.out.println("Người dùng không tồn tại");
            }
            return null;
        } catch (Exception e) {
            Log.error("Lỗi khi tạo danh mục mới với lỗi " + e.getMessage());
            System.out.println("Lỗi khi tạo danh mục mới");
            return null;
        }
    }

    @Override
    public Category updateCategory(Integer id, Category category) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found");
            }

            User currentUser = userOptional.get();

            Optional<Category> existingCategoryOptional = categoryRepository.findById(id);

            if (existingCategoryOptional.isEmpty()) {
                Log.warn("Danh mục với id #" + id + " không tồn tại");
                throw new RuntimeException("Category not found");
            }

            Category updatingCategory = existingCategoryOptional.get();
            updatingCategory.setName(category.getName());
            updatingCategory.setParentCategory(category.getParentCategory());
            updatingCategory.setUpdatedBy(currentUser);
            updatingCategory.setUpdatedAt(getCurrentTimeInVietnam());
            updatingCategory.setActive(category.isActive());
            Log.info(currentUser.getUserInfo().getFullName() + " đã cập nhật danh mục " + updatingCategory.getName());
            return categoryRepository.save(updatingCategory);
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật danh mục với lỗi " + e.getMessage());
            System.out.println("Lỗi khi cập nhật danh mục");
            return null;
        }
    }

    @Override
    public void deleteCategory(Integer id) {
        try {
            if (categoryRepository.existsProductWithCategory(id)) {
                Optional<Category> categoryOptional = categoryRepository.findById(id);
                if (categoryOptional.isPresent()) {
                    Category category = categoryOptional.get();
                    category.setActive(false);
                    category.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
                    categoryRepository.save(category);
                    Log.info("Đã xóa danh mục #" + id);
                } else {
                    throw new RuntimeException("Category not found with id " + id);
                }
            } else {
                categoryRepository.deleteById(id);
                Log.info("Đã xóa danh mục #" + id);
            }
        } catch (Exception e) {
            Log.error("Lỗi khi xóa danh mục #" + id + " với lỗi " + e.getMessage());
            System.out.println("Lỗi khi xóa danh mục");
        }
    }
}
