package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddBlogRequest {

    private int blogCate;

    private String title;

    private String content;

    private String image;

}
