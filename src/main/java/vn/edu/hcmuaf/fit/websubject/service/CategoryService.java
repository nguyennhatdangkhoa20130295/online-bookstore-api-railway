package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.websubject.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> getAllCategories();

    Page<Category> getAllCategories(int page, int perPage, String sort, String filter, String order);

    List<Category> getMainCategories();

    List<Category> getSubCategories(Integer parentId);

    Optional<Category> getCategoryById(Integer id);

    Category createCategory(Category category);

    Category updateCategory(Integer id, Category category);

    void deleteCategory(Integer id);

}
