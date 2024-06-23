package vn.edu.hcmuaf.fit.websubject.payload.request;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateUserRequest {
    private String currentPassword;
    private String newPassword;
    private String newPasswordConfirm;

    private String fullName;
    private String phoneNumber;
    private String gender;
    private Date dateOfBirth;
    private String avatar;
}
