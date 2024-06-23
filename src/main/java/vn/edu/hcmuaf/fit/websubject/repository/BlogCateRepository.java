package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.hcmuaf.fit.websubject.entity.Blog;
import vn.edu.hcmuaf.fit.websubject.entity.BlogCategory;

import java.util.Optional;

@Repository
public interface BlogCateRepository extends JpaRepository<BlogCategory, Integer> {
    Optional<BlogCategory> findById(int id);

    Page<BlogCategory> findAll(Specification<Blog> specification, Pageable pageable);

    Optional<BlogCategory> findByName(String name);

    @Query("SELECT bc FROM BlogCategory bc inner join Blog b on b.blogCate.id = bc.id WHERE b.id = :id")
    Optional<BlogCategory> findByBlogId(int id);
}
