package com.zuci.service;


import com.zuci.entity.User;
import com.zuci.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class otpservice {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${otp.validity.minutes}")
    private int otpValidityMinutes;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updatePassword(int userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public void createAndSendOtp(User user) {
        String otp = generateOTP();
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);
        emailService.sendOtpEmail(user.getUsername(), otp);
    }
    public void saveOrUpdateOTP(String username, String otp) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setOtp(otp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);
        } else {
            // User not found, handle the situation accordingly
        }
    }

    public boolean validateOTP(String username, String otp) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getOtp().equals(otp)) {
            return false;
        }
        if (user.getOtpGeneratedTime().isBefore(LocalDateTime.now().minusMinutes(5))) {
            return false;
        }
        user.setOtp(null);
        user.setOtpGeneratedTime(null);
        userRepository.save(user);
        return true;
    }
}

