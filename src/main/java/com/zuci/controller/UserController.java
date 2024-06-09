package com.zuci.controller;

import com.zuci.entity.User;
import com.zuci.service.EmailService;
import com.zuci.service.otpservice;
import com.zuci.repository.UserRepository;
import com.zuci.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private otpservice otpService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username is already taken");
        }
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        try {
            String token = userService.login(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }
    @GetMapping("/listusers")
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }



    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @PutMapping("/users/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);

            return ResponseEntity.ok("User updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(404).body("User not found");
        }
    }
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        User user = otpService.findByUsername(username);
        if (user != null) {
            otpService.createAndSendOtp(user);
            return "OTP sent to your email.";
        }
        return "User not found.";
    }

    @PostMapping("/validate-otp")
    public String validateOtp(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");
        if (otpService.validateOTP(username, otp)) {
            otpService.saveOrUpdateOTP(username, otp);
            return "OTP is valid.";

        }
        return "Invalid OTP or OTP expired.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");
        if (otpService.validateOTP(username, otp)) {
            User user = otpService.findByUsername(username);
            otpService.updatePassword(user.getUserid(), newPassword);
            return "Password updated successfully.";
        }
        return "Invalid OTP or OTP expired.";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@RequestBody Map<String, String> request) {
        String username = request.get("username"); // Assuming "username" is the user's email
        User user = otpService.findByUsername(username);
        if (user != null) {
            // Regenerate OTP
            String newOtp = otpService.generateOTP();

            // Update OTP in the database
            user.setOtp(newOtp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);

            // Send new OTP via email
            emailService.sendOtpEmail(user.getUsername(), newOtp);

            return "New OTP sent to your email.";
        }
        return "User not found.";
    }

}
