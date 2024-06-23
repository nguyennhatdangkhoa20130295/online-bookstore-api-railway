package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.websubject.entity.Blog;

import java.util.List;
import java.util.Optional;

public interface BlogService {
    Page<Blog> getAllBlogs(int page, int perPage);

    Optional<Blog> getBlogById(int id);

    List<Blog> getAllBlogsUser();

    List<Blog> getBlogsByCateId(int cateId);
    Page<Blog> getBlogByCate(Integer categoryId, int page, int perPage, String sort, String filter, String order);

    Page<Blog> findAll(int page, int size, String sort, String order, String filter);

    void addBlog(int blogCate, String title, String content, String image);

    void editBlog(int id, int blogCate, String title, String content, String image);

    void deleteBlog(int id);
}
