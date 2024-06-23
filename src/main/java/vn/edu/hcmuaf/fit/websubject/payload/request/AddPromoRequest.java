package vn.edu.hcmuaf.fit.websubject.payload.request;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class AddPromoRequest {
    private Integer idProduct;
    private String code;
    private Integer discount;
    private Date startDate;
    private Date endDate;
}
