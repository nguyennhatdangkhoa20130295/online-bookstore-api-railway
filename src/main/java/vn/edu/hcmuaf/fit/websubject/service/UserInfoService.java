package vn.edu.hcmuaf.fit.websubject.service;

import vn.edu.hcmuaf.fit.websubject.entity.UserInfo;

import java.util.Optional;

public interface UserInfoService {
    Optional<UserInfo> getUserInfo(Integer userId);

    UserInfo changeInformation(Integer id, UserInfo userInfo);

    UserInfo createInformation(UserInfo userInfo);
}
