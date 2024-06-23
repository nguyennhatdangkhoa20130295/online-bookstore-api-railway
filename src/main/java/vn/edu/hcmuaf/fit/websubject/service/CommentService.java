package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.data.domain.Page;
import vn.edu.hcmuaf.fit.websubject.entity.Blog;
import vn.edu.hcmuaf.fit.websubject.entity.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentService {
    List<Comment> getListCommentByProductId(int idProduct);

    List<Comment> getListCommentByUserIdAndProductId(int idUser, int idProduct);

    Comment getCommentById(int idComment);

    void addComment(int idProduct, int rate, String description);

    Page<Comment> getAllComments(int page, int perPage);

    Page<Comment> findAll(int page, int size, String sort, String order, String filter);

    void updateComment(int idComment, int rate, String description);

    void deleteComment(int idComment);
}
