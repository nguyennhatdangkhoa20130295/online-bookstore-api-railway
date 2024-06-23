package vn.edu.hcmuaf.fit.websubject.service;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.payload.request.UpdateUserRequest;

import java.util.Optional;

public interface UserService {
    Page<User> getAllUsers(int page, int perPage);

    User getUserById(int idUser);

    void addUser(String username, String password, String email,
                 int role, String avatar, String fullName,
                 String phone, String locked, String isSocial);

    User editUser(int id, String email,
                  int role, String avatar, String fullName, String phone,
                  String locked, String isSocial);

    Page<User> findAllUsers(int page, int size, String sort, String order, String filter);

    Optional<User> getUserByUsername(String username);

    boolean checkIfUsernameExists(String username);

    boolean checkIfEmailExists(String email);

    void deleteUser(int idUser);

    ResponseEntity<?> updateUserInformation(UpdateUserRequest updateUserRequest);
}
