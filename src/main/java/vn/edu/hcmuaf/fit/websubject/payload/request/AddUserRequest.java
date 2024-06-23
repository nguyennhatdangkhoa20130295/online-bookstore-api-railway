package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddUserRequest {
    String username;
    String password;
    String email;
    int role;
    String avatar;
    String fullName;
    String phone;
    String locked;
    String isSocial;
}
