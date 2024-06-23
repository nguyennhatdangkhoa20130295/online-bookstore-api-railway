package vn.edu.hcmuaf.fit.websubject.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "idUser")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "isReply")
    private boolean isReply;

    @Column(name = "replyContent")
    private String replyContent;

    @Column(name = "createdDate")
    private Date createdDate;

    @Column(name = "replyDate")
    private Date replyDate;


}
