package vn.edu.hcmuaf.fit.websubject.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "product_details")
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_sku")
    private String productSku;

    @Column(name = "supplier")
    private String supplier;

    @Column(name = "publisher")
    private String publisher;

    @Column(name = "publish_year")
    private String publishYear;

    @Column(name = "author")
    private String author;

    @Column(name = "brand")
    private String brand;

    @Column(name = "origin")
    private String origin;

    @Column(name = "color")
    private String color;

    @Column(name = "weight")
    private String weight;

    @Column(name = "size")
    private String size;

    @Column(name = "quantity_of_page")
    private int quantityOfPage;

    @Column(name = "description")
    private String description;
}
