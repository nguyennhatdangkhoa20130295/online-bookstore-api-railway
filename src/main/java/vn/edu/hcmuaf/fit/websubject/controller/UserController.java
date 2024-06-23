package vn.edu.hcmuaf.fit.websubject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.entity.Address;
import vn.edu.hcmuaf.fit.websubject.entity.FavoriteProduct;
import vn.edu.hcmuaf.fit.websubject.entity.User;
import vn.edu.hcmuaf.fit.websubject.entity.UserInfo;
import vn.edu.hcmuaf.fit.websubject.payload.request.AddUserRequest;
import vn.edu.hcmuaf.fit.websubject.payload.request.EditUserRequest;
import vn.edu.hcmuaf.fit.websubject.service.AddressService;
import vn.edu.hcmuaf.fit.websubject.service.UserService;
import vn.edu.hcmuaf.fit.websubject.payload.request.UpdateUserRequest;
import vn.edu.hcmuaf.fit.websubject.service.FavoriteProductService;
import vn.edu.hcmuaf.fit.websubject.service.UserInfoService;
import vn.edu.hcmuaf.fit.websubject.service.impl.CustomUserDetailsImpl;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private FavoriteProductService favoriteProductService;

    @GetMapping("")
    public ResponseEntity<Page<User>> getAllBlogs(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "") String filter,
                                                  @RequestParam(defaultValue = "25") int perPage,
                                                  @RequestParam(defaultValue = "id") String sort,
                                                  @RequestParam(defaultValue = "DESC") String order) {
        Page<User> users = userService.findAllUsers(page, perPage, sort, order, filter);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/info")
    public ResponseEntity<?> getUserInformation() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userService.getUserByUsername(customUserDetails.getUsername());
        if (user.isPresent()) {
            return ResponseEntity.ok().body(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{idUser}")
    public ResponseEntity<User> getUserInformation(@PathVariable int idUser) {
        User user = userService.getUserById(idUser);
        if (user != null) {
            return ResponseEntity.ok().body(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/add")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<String> addUser(@RequestBody AddUserRequest addReq) {
        try {
            userService.addUser(addReq.getUsername(), addReq.getPassword(), addReq.getEmail(), addReq.getRole(),
                    addReq.getAvatar(), addReq.getFullName(),
                    addReq.getPhone(), addReq.getLocked(), addReq.getIsSocial());
            return ResponseEntity.ok("Added user successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add user" + e.getMessage());
        }
    }

    @PutMapping("/edit/{idUser}")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<String> editUser(@RequestBody EditUserRequest editReq, @PathVariable Integer idUser) {
        try {
            User editedUser = userService.editUser(idUser, editReq.getEmail(), editReq.getRole(),
                    editReq.getAvatar(), editReq.getFullName(), editReq.getPhone(),
                    editReq.getLocked(), editReq.getIsSocial());
            if (editedUser != null) {
                return ResponseEntity.ok().body("Edit user successfully");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to edit user: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{idUser}")
    @PreAuthorize("@authController.hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable int idUser) {
        try {
            userService.deleteUser(idUser);
            return ResponseEntity.ok("Delete user successfully");
        } catch (RuntimeException e) {
            if (e.getMessage().equals("User not found")) {
                return ResponseEntity.badRequest().body("User not found");
            } else if (e.getMessage().equals("Cannot delete admin")) {
                return ResponseEntity.badRequest().body("Cannot delete admin");
            } else {
                return ResponseEntity.badRequest().body("Failed to delete user: " + e.getMessage());
            }
        }
    }

    @PostMapping("/info")
    public ResponseEntity<?> changeInformation(@RequestBody UserInfo userInfo) {
        try {
            userInfoService.createInformation(userInfo);
            return ResponseEntity.ok("Information created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create information");
        }
    }

//    @PutMapping("/info/{id}")
//    public ResponseEntity<?> changeInformation(@PathVariable Integer id, @RequestBody UserInfo userInfo) {
//        try {
//            userInfoService.changeInformation(id, userInfo);
//            return ResponseEntity.ok("Information changed successfully");
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("Failed to change information");
//        }
//    }

    @GetMapping("/addresses")
    public ResponseEntity<?> getUserAddresses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsImpl customUserDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
        Optional<User> user = userService.getUserByUsername(customUserDetails.getUsername());
        if (user.isPresent()) {
            List<Address> addresses = addressService.getUserAddresses(user.get().getId());
            return ResponseEntity.ok().body(addresses);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<?> getAddressById(@PathVariable Integer id) {
        Optional<Address> addressOptional = addressService.getAddressById(id);
        if (addressOptional.isPresent()) {
            return ResponseEntity.ok().body(addressOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/addresses/default/{id}")
    public ResponseEntity<?> getAddressDefaultByUserId(@PathVariable Integer id) {
        Optional<Address> addressOptional = addressService.getAddressDefaultByUserId(id);
        if (addressOptional.isPresent()) {
            return ResponseEntity.ok().body(addressOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/addresses")
    public ResponseEntity<?> createAddress(@RequestBody Address address) {
        System.out.println(address);
        Address createdAddress = addressService.createAddress(address);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAddress);
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable Integer id, @RequestBody Address address) {
        Address updatedAddress = addressService.updateAddress(id, address);
        return updatedAddress != null ? ResponseEntity.ok(updatedAddress) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/addresses/default/{id}")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Integer id) {
        addressService.setDefaultAddress(id);
        return ResponseEntity.ok("The user's address has been set by default");
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> getAllFavoriteProducts() {
        List<FavoriteProduct> favoriteProducts = favoriteProductService.getAllFavoriteProducts();
        return ResponseEntity.ok().body(favoriteProducts);
    }

    @PostMapping("/favorites/{productId}")
    public ResponseEntity<?> addFavoriteProduct(@PathVariable Integer productId) {
        FavoriteProduct favorite = favoriteProductService.addFavorite(productId);
        return ResponseEntity.ok().body(favorite);
    }

    @DeleteMapping("/favorites/{id}")
    public ResponseEntity<?> deleteFavoriteProduct(@PathVariable Integer id) {
        favoriteProductService.deleteFavorite(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/info")
    public ResponseEntity<?> updateUserInfo( @RequestBody UpdateUserRequest request) {
        return userService.updateUserInformation(request);
    }
}
