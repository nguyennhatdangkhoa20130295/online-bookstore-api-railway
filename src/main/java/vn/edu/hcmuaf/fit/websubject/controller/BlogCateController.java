package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.BlogCategory;
import vn.edu.hcmuaf.fit.websubject.payload.request.AddBlogCateRequest;
import vn.edu.hcmuaf.fit.websubject.service.BlogCateService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blogCate")
public class BlogCateController {
    BlogCateService blogCateService;
    @Autowired
    public BlogCateController(BlogCateService blogCateService) {
        this.blogCateService = blogCateService;
    }

    @GetMapping("/all")
    public List<BlogCategory> getAllCates() {
        return blogCateService.getAllCate();
    }

    @GetMapping("")
    public ResponseEntity<Page<BlogCategory>> getAllBlogCate(@RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "") String filter,
                                                             @RequestParam(defaultValue = "5") int perPage,
                                                             @RequestParam(defaultValue = "id") String sort,
                                                             @RequestParam(defaultValue = "ASC") String order) {
        try {
            Page<BlogCategory> blogCate = blogCateService.findAll(page, perPage, sort, order, filter);
            return ResponseEntity.ok(blogCate);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogCategory> getCateById(@PathVariable int id) {
        Optional<BlogCategory> blogcate = blogCateService.getCateById(id);
        if (blogcate.isPresent()) {
            return ResponseEntity.ok(blogcate.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<?> addBlogCate(@RequestBody AddBlogCateRequest addBlogCateRequest) {

        try {
            blogCateService.addBlogCategory(addBlogCateRequest.getName(), addBlogCateRequest.getCreatedBy(), addBlogCateRequest.getUpdatedBy());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.badRequest().body("User not found");
            }
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<?> editBlogCate(@PathVariable int id, @RequestBody AddBlogCateRequest addBlogCateRequest) {
        try {
            blogCateService.editBlogCategory(id, addBlogCateRequest.getName(), addBlogCateRequest.getUpdatedBy());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().equals("Blog category not found")) {
                return ResponseEntity.badRequest().body("Blog category not found");
            }
            if (e.getMessage().equals("User not found")) {
            return ResponseEntity.badRequest().body("User not found");
            }
        return ResponseEntity.notFound().build();
    }
}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBlogCate(@PathVariable int id) {
        try {
            blogCateService.deleteBlogCategory(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage().equals("This category is being used by some blogs")) {
                return ResponseEntity.badRequest().body("This category is being used by some blogs");
            }
            return ResponseEntity.notFound().build();
        }
    }
}
