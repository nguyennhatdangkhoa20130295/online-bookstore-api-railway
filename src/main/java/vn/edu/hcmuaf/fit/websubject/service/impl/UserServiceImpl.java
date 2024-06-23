package vn.edu.hcmuaf.fit.websubject.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.entity.*;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.payload.request.UpdateUserRequest;
import vn.edu.hcmuaf.fit.websubject.repository.RoleRepository;
import vn.edu.hcmuaf.fit.websubject.repository.TokenRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserInfoRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.log4j.Logger;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger Log = Logger.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder, UserInfoRepository userInfoRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.userInfoRepository = userInfoRepository;
        this.tokenRepository = tokenRepository;
    }

    public Page<User> getAllUsers(int page, int perPage) {
        Pageable pageable = PageRequest.of(page, perPage);
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(int idUser) {
        return userRepository.findById(idUser).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Page<User> findAllUsers(int page, int size, String sort, String order, String filter) {
        Sort.Direction direction = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("desc")) {
            direction = Sort.Direction.DESC;
        }
        Sort sortPa = Sort.by(direction, sort);
        Pageable pageable = PageRequest.of(page, size, sortPa);

        JsonNode jsonFilter;
        try {
            jsonFilter = new ObjectMapper().readTree(java.net.URLDecoder.decode(filter, StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Specification<User> specification = (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (jsonFilter.has("q")) {
                String searchStr = jsonFilter.get("q").asText();
                predicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("userInfo").get("fullName")), "%" + searchStr.toLowerCase() + "%");
            }
            if(jsonFilter.has("role")) {
                String type = jsonFilter.get("role").asText();
                Join<User, Role> roleJoin = root.join("roles");
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.isTrue(roleJoin.get("description").in(type)));

            }
            return predicate;
        };

        return userRepository.findAll(specification, pageable);
    }

    @Override
    public void addUser(String username, String password, String email,
                        int role, String avatar, String fullName,
                        String phone, String locked, String isSocial) {
        try {
            if (userRepository.existsByUsername(username)) {
                Log.warn("Tên tài khoản " + username + " đã tồn tại!");
                System.out.println("Username is already taken!");
            } else if (userRepository.existsByEmail(email)) {
                Log.warn("Email " + email + " đã được sử dụng!");
                System.out.println("Email is already in use!");
            } else {
                User user = new User();
                UserInfo userInfo = new UserInfo();
                userInfo.setUser(user);
                userInfo.setAvatar(avatar);
                userInfo.setFullName(fullName);
                userInfo.setPhoneNumber(phone);
                user.setUserInfo(userInfo);
                user.setUsername(username);
                user.setPassword(encoder.encode(password));
                user.setEmail(email);
                Set<Role> roles = new HashSet<>();
                switch (role) {
                    case 1:
                        Role adminRole = roleRepository.findByDescription(EnumRole.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case 2:
                        Role modRole = roleRepository.findByDescription(EnumRole.MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    case 3:
                        Role userRole = roleRepository.findByDescription(EnumRole.USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);

                        break;
                    default:
                        Role userR = roleRepository.findByDescription(EnumRole.USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userR);
                }

                user.setRoles(roles);
                user.setCreatedAt(CurrentTime.getCurrentTimeInVietnam());
                user.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
                if (locked.equals("false"))
                    user.setLocked(false);
                else
                    user.setLocked(true);
                if (isSocial.equals("false"))
                    user.setIsSocial(false);
                else
                    user.setIsSocial(true);
                Log.info("Tạo tài khoản " + username + " thành công");
                userRepository.save(user);
            }
        } catch (Exception e) {
            Log.error("Lỗi khi tạo tài khoản: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public User editUser(int id, String email,
                         int role, String avatar, String fullName, String phone,
                         String locked, String isSocial) {
        try {
            User newInforUser = null;
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                newInforUser = userOptional.get();
                newInforUser.setEmail(email);
                newInforUser.getUserInfo().setFullName(fullName);
                newInforUser.getUserInfo().setPhoneNumber(phone);
                if(avatar == null || avatar.isEmpty()) {
                    newInforUser.getUserInfo().setAvatar(newInforUser.getUserInfo().getAvatar());
                } else {
                    newInforUser.getUserInfo().setAvatar(avatar);
                }
                newInforUser.getRoles().clear();
                Set<Role> roles = new HashSet<>();
                switch (role) {
                    case 1:
                        Role adminRole = roleRepository.findByDescription(EnumRole.ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case 2:
                        Role modRole = roleRepository.findByDescription(EnumRole.MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    case 3:
                        Role userRole = roleRepository.findByDescription(EnumRole.USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);

                        break;
                    default:
                        Role userR = roleRepository.findByDescription(EnumRole.USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userR);
                }

                newInforUser.setRoles(roles);
                newInforUser.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
                if (locked.equals("false"))
                    newInforUser.setLocked(false);
                else
                    newInforUser.setLocked(true);
                if (isSocial.equals("false"))
                    newInforUser.setIsSocial(false);
                else
                    newInforUser.setIsSocial(true);

                Log.info("Cập nhật tài khoản " + newInforUser.getUsername() + " thành công");
                userRepository.save(newInforUser);
            }
            return newInforUser;
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật tài khoản: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteUser(int idUser) {
        try {
            User user = userRepository.findById(idUser).orElseThrow(() -> new RuntimeException("User not found"));
            List<User> admins = userRepository.findAllByRoles(idUser, EnumRole.ADMIN.toString());
            List<User> mods = userRepository.findAllByRoles(idUser, EnumRole.MODERATOR.toString());
            if (user.getRoles().contains(roleRepository.findByDescription(EnumRole.ADMIN).get()) && admins.size() == 1) {
                Log.warn("Không thể xóa tài khoản admin");
                throw new RuntimeException("Cannot delete admin");
            }
            if (user.getRoles().contains(roleRepository.findByDescription(EnumRole.MODERATOR).get()) && mods.size() == 1) {
                Log.warn("Không thể xóa tài khoản moderator");
                throw new RuntimeException("Cannot delete moderator");
            } else {
                tokenRepository.deleteAll(tokenRepository.findAllTokenByUser(idUser));
                userRepository.deleteById(idUser);
                Log.info("Xóa tài khoản " + user.getUsername() + " thành công");
            }
        } catch (Exception e) {
            Log.error("Lỗi khi xóa tài khoản: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<?> updateUserInformation(UpdateUserRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            Optional<User> userOptional = userRepository.findByUsername(customUserDetails.getUsername());
            if (userOptional.isEmpty()) {
                throw new RuntimeException("User not found");
            }
            User user = userOptional.get();
            if (request.getCurrentPassword() != null && !request.getCurrentPassword().isEmpty()) {
                if (!encoder.matches(request.getCurrentPassword(), user.getPassword())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu hiện tại không đúng.");
                }
                if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mật khẩu không trùng khớp.");
                }
                user.setPassword(encoder.encode(request.getNewPassword()));
            }
            userRepository.save(user);

            Optional<UserInfo> userInfoOptional = userInfoRepository.findByUserId(user.getId());
            if (userInfoOptional.isEmpty()) {
                throw new RuntimeException("User information not found");
            }
            UserInfo userInfo = userInfoOptional.get();
            userInfo.setFullName(request.getFullName());
            userInfo.setPhoneNumber(request.getPhoneNumber());
            userInfo.setGender(request.getGender());
            userInfo.setDateOfBirth(request.getDateOfBirth());
            userInfo.setAvatar(request.getAvatar());
            userInfo.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());

            userInfoRepository.save(userInfo);
            Log.info("Cập nhật thông tin người dùng " + user.getUsername() + " thành công");
            return ResponseEntity.ok("Cập nhật thông tin thành công!");
        } catch (Exception e) {
            Log.error("Lỗi khi cập nhật thông tin: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật thông tin");
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean checkIfUsernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean checkIfEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}
