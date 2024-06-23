package vn.edu.hcmuaf.fit.websubject.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.entity.UserInfo;
import vn.edu.hcmuaf.fit.websubject.service.UserInfoService;
import vn.edu.hcmuaf.fit.websubject.repository.UserInfoRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;

import java.util.Optional;

import static vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime.getCurrentTimeInVietnam;

import org.apache.log4j.Logger;

@Service
public class UserInfoServiceImpl implements UserInfoService {
    private static final Logger Log = Logger.getLogger(UserInfoServiceImpl.class);
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Optional<UserInfo> getUserInfo(Integer userId) {
        return userInfoRepository.findByUserId(userId);
    }

    @Override
    public UserInfo changeInformation(Integer id, UserInfo userInfo) {
        try {
            Optional<UserInfo> infoOptional = userInfoRepository.findById(id);
            if (infoOptional.isEmpty()) {
                Log.warn("Thông tin người dùng #" + id + " không tồn tại");
                throw new RuntimeException("User info not found");
            }
            UserInfo currentInfo = infoOptional.get();
            if (userInfo.getFullName() != null) {
                currentInfo.setFullName(userInfo.getFullName());
            }
            if (userInfo.getPhoneNumber() != null) {
                currentInfo.setPhoneNumber(userInfo.getPhoneNumber());
            }
            if (userInfo.getGender() != null) {
                currentInfo.setGender(userInfo.getGender());
            }
            if (userInfo.getDateOfBirth() != null) {
                currentInfo.setDateOfBirth(userInfo.getDateOfBirth());
            }
            if (userInfo.getAvatar() != null) {
                currentInfo.setAvatar(userInfo.getAvatar());
            }
            currentInfo.setUpdatedAt(getCurrentTimeInVietnam());
            Log.info("Cập nhật thông tin người dùng #" + id + " thành công");
            return userInfoRepository.save(currentInfo);
        } catch (Exception e) {
            Log.error("Cập nhật thông tin người dùng #" + id + " thất bại: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserInfo createInformation(UserInfo userInfo) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                Log.warn("Người dùng " + customUserDetails.getUsername() + " không tồn tại");
                throw new RuntimeException("User not found");
            }
            User user = userOptional.get();
            userInfo.setUser(user);
            userInfo.setCreatedAt(getCurrentTimeInVietnam());
            Log.info("Tạo thông tin người dùng " + customUserDetails.getUsername() + " thành công");
            return userInfoRepository.save(userInfo);
        } catch (Exception e) {
            Log.error("Tạo thông tin người dùng thất bại: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
