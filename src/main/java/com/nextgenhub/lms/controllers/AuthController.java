package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.dto.LoginRequest;
import com.nextgenhub.lms.entities.PasswordResetToken;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.repositories.PasswordResetTokenRepository;
import com.nextgenhub.lms.repositories.UserServiceRepo;
import com.nextgenhub.lms.services.EmailService;
import com.nextgenhub.lms.services.StudentService;
import com.nextgenhub.lms.services.UserDetailsServiceImpl;
import com.nextgenhub.lms.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/authenticate")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private StudentService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private static final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();


    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            final UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            User user=userService.getStudent(userDetails.getUsername());
            response.put("user", user);

            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/forgot-password/{userName}")
    public ResponseEntity<Map<String, String>> forgotPassword(@PathVariable String userName) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = userService.getStudent(userName);  // Find user by userName
            if (user == null) {
                throw new RuntimeException("User not found!");
            }
            PasswordResetToken passwordResetToken=tokenRepository.findByUserName(userName);
            if(passwordResetToken!=null){
                throw new RuntimeException("Cant send new token before previous token not get expired.");
            }

            // Generate a unique token
            String token = UUID.randomUUID().toString();
            Date createdDate = new Date(System.currentTimeMillis()); // 15 mins validity

            // Save the token in DB
            PasswordResetToken resetToken = new PasswordResetToken(user.getUserName(), token);
            tokenRepository.save(resetToken);

            // Send reset email to user's email
            String resetLink = "http://localhost:3000/authenticate/reset-password/" + token;
            String emailContent = "Hello "+user.getName()+",\n" +
                    "\n" +
                    "We received a request to reset your password. If this was you, please click the link below to reset your password.  \n" +
                    "This link is only valid for the next 15 minutes.\n" +
                    "\n" +
                    "Reset Password: "+resetLink+"\n" +
                    "\n" +
                    "If you didn’t request this change, you can ignore this email. Your password will remain the same.\n" +
                    "\n" +
                    "Best regards,  \n" +
                    "[NextGen LMS] Support Team\n";

            emailService.sendEmail(user.getEmail(), "Reset Password", emailContent);


            response.put("message", "A password reset link has been sent to your email. The link is valid for 15 minutes.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (RuntimeException e){
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            response.put("error", "Failed to send password reset token to mail. try again later.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/reset-password/{token}/{newPassword}")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable String token, @PathVariable String newPassword) {
        Map<String, String> response = new HashMap<>();
        try {
            PasswordResetToken resetToken = tokenRepository.findByToken(token);

            if (resetToken == null ) {
                throw new RuntimeException("Invalid or expired token!");
            }

            // Find user by userName
            User user = userService.getStudent(resetToken.getUserName());
            if (user == null) {
                throw new RuntimeException("User not found!");
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            User newUser = userService.updateUser(user);

            // Delete token after use
            tokenRepository.delete(resetToken);

            String emailContent = "Hello "+user.getName()+"\n" +
                    "\n" +
                    "Your password has been successfully changed. If this was you, no further action is needed.  \n" +
                    "If you did not request this change, please reset your password immediately or contact our support team.\n" +
                    "\n" +
                    "Best regards,  \n" +
                    "NextGen LMS Support Team" ;

            emailService.sendEmail(user.getEmail(), "Reset Password", emailContent);


            response.put("message", "Password reset successful.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (RuntimeException e){
            response.put("error","Your token has been expired, generate new reset token.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            response.put("error","Due to server error, can't procide with your request. please try after some time.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/check-token/{token}")
    public ResponseEntity<Map<String, String>> checkTokenValidity(@PathVariable String token) {
        Map<String, String> response = new HashMap<>();
        try {
            PasswordResetToken resetToken = tokenRepository.findByToken(token);

            if (resetToken == null ) {
                throw new RuntimeException("The token is Invalid or expired. please generate new token.");
            }
            return new ResponseEntity<>(HttpStatus.OK);
        }catch (RuntimeException e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            response.put("error","Due to server error, can't procide with your request. please try after some time.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

}
