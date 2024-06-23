package vn.edu.hcmuaf.fit.websubject.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "product_comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JsonIgnoreProperties("comments")
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "comment_detail")
    private String cmtDetail;

    @Column(name = "created_at")
    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy")
    private Date created_at;

    @Column(name = "updated_at")
    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy")
    private Date updated_at;
}
