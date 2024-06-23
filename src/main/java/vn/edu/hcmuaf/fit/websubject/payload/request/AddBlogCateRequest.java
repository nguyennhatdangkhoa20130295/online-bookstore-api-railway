package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddBlogCateRequest {
    private String name;
    private int createdBy;
    private int updatedBy;
}
