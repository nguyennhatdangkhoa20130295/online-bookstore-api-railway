package vn.edu.hcmuaf.fit.websubject.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
public class OTPService {

    private Cache<String, String> otpCache; // Lưu trữ mã OTP tạm thời

    public OTPService() {
        this.otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES) // Mã OTP hết hạn sau 5 phút
                .build();
    }

    public void saveOTP(String key, String otp) {
        otpCache.put(key, otp);
    }

    public String getOTP(String key) {
        return otpCache.getIfPresent(key);
    }

    public void removeOTP(String key) {
        otpCache.invalidate(key);
    }

}

