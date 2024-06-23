package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendEmailRequest {
    private String email;
    private String otp;
    private String newPassword;
    private int type;

}
