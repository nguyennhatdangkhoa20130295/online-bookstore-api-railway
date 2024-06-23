package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequest {
    private String fullName;
    private String email;
    private String title;
    private String content;
    private String contentReply;
}
