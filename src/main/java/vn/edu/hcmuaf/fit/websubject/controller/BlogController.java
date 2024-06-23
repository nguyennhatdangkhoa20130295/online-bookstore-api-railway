package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.Blog;
import vn.edu.hcmuaf.fit.websubject.payload.request.AddBlogRequest;
import vn.edu.hcmuaf.fit.websubject.service.BlogCateService;
import vn.edu.hcmuaf.fit.websubject.service.BlogService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/blog")
public class BlogController {
    @Autowired
    BlogService blogService;

    @Autowired
    BlogCateService blogCateService;

    @GetMapping("")
    public ResponseEntity<Page<Blog>> getAllBlogs(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "") String filter,
                                                  @RequestParam(defaultValue = "25") int perPage,
                                                  @RequestParam(defaultValue = "title") String sort,
                                                  @RequestParam(defaultValue = "DESC") String order) {
        try {
            Page<Blog> blogs = blogService.findAll(page, perPage, sort, order, filter);
            return ResponseEntity.ok(blogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Blog>> getAllBlogsUser() {
        try {
            List<Blog> blogs = blogService.getAllBlogsUser();
            return ResponseEntity.ok(blogs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/cate/{categoryId}")
    public ResponseEntity<?> getBlogByCate(@PathVariable Integer categoryId,
                                                    @RequestParam(defaultValue = "0") Integer page,
                                                    @RequestParam(defaultValue = "3") Integer perPage,
                                                    @RequestParam(defaultValue = "id") String sort,
                                                    @RequestParam(defaultValue = "{}") String filter,
                                                    @RequestParam(defaultValue = "ASC") String order) {
        try {
            if(categoryId==0){
                Page<Blog> blogs = blogService.getAllBlogs(page, perPage);
                return ResponseEntity.ok(blogs);
            } else {
                Page<Blog> blogs = blogService.getBlogByCate(categoryId, page, perPage, sort, filter, order);
                return ResponseEntity.ok(blogs);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Blog> getBlogById(@PathVariable int id) {
        Optional<Blog> blog = blogService.getBlogById(id);
        return blog.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @PreAuthorize("@authController.hasRole('MODERATOR') || @authController.hasRole('ADMIN')")
    public ResponseEntity<String> addBlog(@RequestBody AddBlogRequest addBlogRequest) {
        try {
            blogService.addBlog(addBlogRequest.getBlogCate(), addBlogRequest.getTitle(), addBlogRequest.getContent(), addBlogRequest.getImage());
            return ResponseEntity.ok("Added blog successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add blog" + addBlogRequest);
        }
    }
    @PutMapping("/edit/{id}")
    @PreAuthorize("@authController.hasRole('MODERATOR') || @authController.hasRole('ADMIN')")
    public ResponseEntity<String> editBlog(@PathVariable int id, @RequestBody AddBlogRequest addBlogRequest) {
        try {
            blogService.editBlog(id, addBlogRequest.getBlogCate(), addBlogRequest.getTitle(), addBlogRequest.getContent(), addBlogRequest.getImage());
            return ResponseEntity.ok("Edited blog successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to edit blog" + addBlogRequest);
        }
    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@authController.hasRole('MODERATOR') || @authController.hasRole('ADMIN')")
    public ResponseEntity<String> deleteBlog(@PathVariable int id) {
        try {
            blogService.deleteBlog(id);
            return ResponseEntity.ok("Deleted blog successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete blog");
        }
    }
}
