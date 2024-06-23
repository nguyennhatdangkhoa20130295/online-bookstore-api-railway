package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.Category;
import vn.edu.hcmuaf.fit.websubject.service.CategoryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/all")
    public List<Category> getAllCategoriesUser() {
        return categoryService.getAllCategories();
    }

    @GetMapping
    public ResponseEntity<Page<Category>> getAllCategories(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int perPage,
                                                           @RequestParam(defaultValue = "id") String sort,
                                                           @RequestParam(defaultValue = "{}") String filter,
                                                           @RequestParam(defaultValue = "ASC") String order) {
        Page<Category> categories = categoryService.getAllCategories(page, perPage, sort, filter, order);
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/main-categories")
    public List<Category> getMainCategories() {
        return categoryService.getMainCategories();
    }

    @GetMapping("/{id}/subcategories")
    public List<Category> getSubCategories(@PathVariable Integer id) {
        return categoryService.getSubCategories(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        Optional<Category> categoryOptional = categoryService.getCategoryById(id);
        return categoryOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(@PathVariable Integer id, @RequestBody Category category) {
        System.out.println(category);
        Category updatedCategory = categoryService.updateCategory(id, category);
        System.out.println(updatedCategory);
        return updatedCategory != null ? ResponseEntity.ok(updatedCategory) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
