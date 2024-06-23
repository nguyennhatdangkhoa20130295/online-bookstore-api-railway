package vn.edu.hcmuaf.fit.websubject.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "product_promotions")
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JsonIgnoreProperties("promotion")
    @ManyToOne
    @JoinColumn(name = "idProduct")
    private Product product;

    @Column(name = "code")
    private String code;

    @Column(name = "discount")
    private Integer discount;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "isCode")
    private Boolean isCode;

}
