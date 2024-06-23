package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class SignupRequest {
    private String username;

    private String email;

    private String otp;

    private Set<String> role;

    private String password;

}
