package vn.edu.hcmuaf.fit.websubject.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "imported_quantity")
    private int importedQuantity;

    @Column(name = "remaining_quantity")
    private int remainingQuantity;

    @Column(name = "import_price")
    private int importPrice;

    @Column(name = "sale_price")
    private int salePrice;

    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "created_at")
    private Date createdAt;

    @JsonFormat(pattern = "HH:mm:ss dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "active", columnDefinition = "boolean default true")
    private boolean active;

}
