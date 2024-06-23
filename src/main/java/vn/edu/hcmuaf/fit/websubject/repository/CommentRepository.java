package vn.edu.hcmuaf.fit.websubject.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.edu.hcmuaf.fit.websubject.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer>, JpaSpecificationExecutor {
    List<Comment> findAllByProductId(Integer id);

    @Query("""
        select c from Comment c where c.user.id = :userId and c.product.id = :productId
    """)
    List<Comment> findAllByUserIdAndProductId(Integer userId, Integer productId);

    Optional<Comment> findById(Integer id);
}
