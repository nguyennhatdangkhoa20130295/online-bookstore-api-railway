package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class InventoryRequest {
    private int productId;
    private int importPrice;
    private int salePrice;
    private int quantity;
    private Date createdAt;
}
