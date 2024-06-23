package vn.edu.hcmuaf.fit.websubject.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Table(name = "blogs")
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    @JoinColumn(name = "blog_cate_id")
    private BlogCategory blogCate;

    private String image;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "short_description")
    private String shortDesc;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at")
    private Date createdAt;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @OneToOne
    @JoinColumn(name = "updated_by")
    private User updateBy;

    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private Date updatedAt;

}
