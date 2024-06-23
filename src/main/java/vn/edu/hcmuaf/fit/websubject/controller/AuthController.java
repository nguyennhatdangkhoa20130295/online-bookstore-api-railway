package vn.edu.hcmuaf.fit.websubject.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import vn.edu.hcmuaf.fit.websubject.jwt.JwtUtils;
import vn.edu.hcmuaf.fit.websubject.entity.*;
import vn.edu.hcmuaf.fit.websubject.payload.others.CurrentTime;
import vn.edu.hcmuaf.fit.websubject.payload.request.SendEmailRequest;
import vn.edu.hcmuaf.fit.websubject.payload.request.LoginRequest;
import vn.edu.hcmuaf.fit.websubject.payload.request.SignupRequest;
import vn.edu.hcmuaf.fit.websubject.payload.response.JwtResponse;
import vn.edu.hcmuaf.fit.websubject.payload.response.MessageResponse;
import vn.edu.hcmuaf.fit.websubject.repository.RoleRepository;
import vn.edu.hcmuaf.fit.websubject.repository.TokenRepository;
import vn.edu.hcmuaf.fit.websubject.repository.UserRepository;
import vn.edu.hcmuaf.fit.websubject.service.EmailService;
import vn.edu.hcmuaf.fit.websubject.service.OTPService;
import vn.edu.hcmuaf.fit.websubject.service.impl.CustomUserDetailsImpl;

import javax.mail.MessagingException;
import java.util.Random;
import org.apache.log4j.Logger;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger Log =  Logger.getLogger(AuthController.class);
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    OTPService otpService;

    @Autowired
    EmailService emailService;


    private static final int OTP_LENGTH = 6;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            CustomUserDetailsImpl userDetails = (CustomUserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            var user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            revokeAllUserToken(user);
            saveUserToken(user, jwt);

            Log.info("Người dùng " + user.getUsername() + " đã đăng nhập");

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles));
        } catch (Exception e) {
            Log.error("Người dùng đăng nhâp thất bại với lỗi "+ e.getMessage());
            return ResponseEntity
                    .badRequest()
                    .body("Sai tên đăng nhập hoặc mật khẩu.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        try{
        String savedOTP = otpService.getOTP(signUpRequest.getEmail());
        if (savedOTP != null && savedOTP.equals(signUpRequest.getOtp())) {
            if (userRepository.existsByUsername(signUpRequest.getUsername())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Username is already taken!");
            }

            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity
                        .badRequest()
                        .body("Error: Email is already in use!");
            }

            // Create new user's account
            User user = new User(signUpRequest.getUsername(),
                    signUpRequest.getEmail(),
                    encoder.encode(signUpRequest.getPassword()));

            Set<String> strRoles = signUpRequest.getRole();
            Set<Role> roles = new HashSet<>();

            if (strRoles == null) {
                Role userRole = roleRepository.findByDescription(EnumRole.USER)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                roles.add(userRole);
            } else {
                strRoles.forEach(role -> {
                    switch (role) {
                        case "admin":
                            Role adminRole = roleRepository.findByDescription(EnumRole.ADMIN)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(adminRole);

                            break;
                        case "mod":
                            Role modRole = roleRepository.findByDescription(EnumRole.MODERATOR)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(modRole);

                            break;
                        default:
                            Role userRole = roleRepository.findByDescription(EnumRole.USER)
                                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                            roles.add(userRole);
                    }
                });
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setUser(user);
            userInfo.setAvatar("https://i.ibb.co/C1ymX1n/user.png");
            userInfo.setCreatedAt(CurrentTime.getCurrentTimeInVietnam());
            user.setUserInfo(userInfo);

            user.setRoles(roles);
            user.setCreatedAt(CurrentTime.getCurrentTimeInVietnam());
            user.setUpdatedAt(CurrentTime.getCurrentTimeInVietnam());
            user.setLocked(false);
            user.setIsSocial(false);
            userRepository.save(user);
//        var jwtToken = jwtUtils.generateJwtToken((Authentication) user);
//        revokeAllUserToken(saveUser);
//        saveUserToken(saveUser, jwtToken);
            Log.info("Người dùng " + user.getUsername() + " đã đăng ký thành công");
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } else {
            Log.error("Người dùng đăng ký thất bại với lỗi Invalid OTP.");
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }
        } catch (Exception e) {
            Log.error("Người dùng đăng ký thất bại với lỗi " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to register user: " + e.getMessage());
        }
    }

    @PostMapping("/checkToken/{token}")
    public ResponseEntity<?> checkToken(@Valid @PathVariable String token) {
        try {
            jwtUtils.validateJwtToken(token);
            return ResponseEntity.ok("Token is valid");
        } catch (ExpiredJwtException e) {
            return ResponseEntity.badRequest().body("Token is expired");
        }
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> createAccount(@Valid @RequestBody SendEmailRequest sendMailRequest) throws
            MessagingException {
        try {
            if (sendMailRequest.getType() == 1) {
                if (!userRepository.existsByEmail(sendMailRequest.getEmail())) {
                    Log.warn("Không tìm thấy email "+ sendMailRequest.getEmail());
                    return ResponseEntity.badRequest().body("Không tìm thấy email.");
                }
            } else if (sendMailRequest.getType() == 0) {
                if (userRepository.existsByEmail(sendMailRequest.getEmail())) {
                    Log.warn("Email đã tồn tại "+ sendMailRequest.getEmail());
                    return ResponseEntity.badRequest().body("Email đã tồn tại.");
                }
            }
            System.out.println(sendMailRequest.getType());
            // Logic để gửi mã OTP đến email
            String otp = generateOTP();
            emailService.sendEmailForgot(sendMailRequest.getEmail(), otp, sendMailRequest.getType());
            otpService.saveOTP(sendMailRequest.getEmail(), otp);
            // Gửi mã OTP đến email
            Log.info("Gửi email thành công đến "+ sendMailRequest.getEmail() +" với mã OTP " + otp);
            return ResponseEntity.ok("OTP " + otp + " sent successfully.");
        } catch (Exception e) {
            Log.error("Gửi email thất bại với lỗi " + e.getMessage());
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody SendEmailRequest forgotPassRequest) {
        String savedOTP = otpService.getOTP(forgotPassRequest.getEmail());
        if (savedOTP != null && savedOTP.equals(forgotPassRequest.getOtp())) {
            // Xác thực thành công, thiết lập lại mật khẩu
            var user = userRepository.findByEmail(forgotPassRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user != null) {
                user.setPassword(encoder.encode(forgotPassRequest.getNewPassword()));
                userRepository.save(user);
                // Xóa mã OTP sau khi đã sử dụng
                otpService.removeOTP(forgotPassRequest.getEmail());
                // Trả về thông báo thành công và mật khẩu mới cho người dùng
                Log.info("Mật khẩu đã được đặt lại cho người dùng "+ user.getUsername());
                return ResponseEntity.ok("Password reset successfully.");
            } else {
                Log.warn("Không tìm thấy người dùng với email"+ forgotPassRequest.getEmail());
                return ResponseEntity.badRequest().body("User not found."); // Không tìm thấy người dùng
            }
        } else {
            Log.error("Xác thực thất bại với lỗi Invalid OTP.");
            return ResponseEntity.badRequest().body("Invalid OTP.");
        }
    }

    public String generateOTP() {
        // Dùng các ký tự từ 0-9 để tạo mã OTP
        String digits = "0123456789";
        Random random = new Random();
        StringBuilder otp = new StringBuilder();

        // Tạo mã OTP bằng cách chọn ngẫu nhiên các ký tự từ digits và thêm vào chuỗi OTP
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = random.nextInt(digits.length());
            otp.append(digits.charAt(index));
        }

        return otp.toString();
    }

    private void revokeAllUserToken(User user) {
        var validToken = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validToken.isEmpty())
            return;
        validToken.forEach(t -> {
            t.setExpired(true);
            t.setRevoked(true);
        });
        tokenRepository.saveAll(validToken);
    }

    private void saveUserToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .revoked(false)
                .expired(false)
                .build();
        tokenRepository.save(token);
    }

    public boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if (authority.getAuthority().equals(roleName)) {
                return true;
            }
        }
        return false;
    }
}