package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.entities.Certificate;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.services.CourseService;
import com.nextgenhub.lms.services.SmsService;
import com.nextgenhub.lms.services.StudentService;
import com.nextgenhub.lms.utils.JwtUtil;
import org.apache.tomcat.util.http.parser.HttpParser;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentService studentService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private SmsService smsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    private final String message = "message";
    private final String error = "error";

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStudentDetails() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            User student = studentService.getStudent(userName);
            if (student == null) {
                response.put(message, "User with userName '" + userName + "' not found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("user", student);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping
    public ResponseEntity<Map<String, Object>> updateStudent(@RequestBody User student) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            if (userName == null || student == null || userName.isEmpty() || student.getEmail().isEmpty()) {
                response.put(message, "insufficient data.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            User newStudent = studentService.updateStudent(userName, student);
            if (newStudent == null) {
                response.put(message, "An email userName " + userName + " is already in use");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("user", newStudent);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update/mobile/{mobileNo}")
    public ResponseEntity<Map<String, Object>> updateMobileNumber(@PathVariable String mobileNo) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            User user = studentService.changeMobileNo(userName, mobileNo);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("user", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("update/details")
    public ResponseEntity<Map<String, Object>> updateDetails(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            String name = (String) body.get("name");
            String email = (String) body.get("email");
            User user = studentService.updateDetails(userName, name, email);
            if (user == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("user", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> deleteStudent() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            if (userName == null || userName.isEmpty()) {
                response.put(message, "insufficient data.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            User student = studentService.deleteUser(userName);
            if (student == null) {
                response.put(message, "An user name " + userName + " is already in use");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("user", student);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("{courseId}")
    public ResponseEntity<Map<String, Object>> enrollForCourse(@PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();

            User user = courseService.enrollForCourse(courseId, userName);
            if (user == null) {
                response.put("user", "Course Not Found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("user", user);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/enrolled-courses")
    public ResponseEntity<Map<String, Object>> getEnrolledCoursesProgress() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Map<String, Double> progresses = courseService.getEnrolledCoursesProgress(userName);
            if (progresses == null || progresses.isEmpty()) {
                response.put(message, "Progress Not Found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("courses", progresses.keySet());
            response.put("progresses", progresses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("change-mobile/getOtp/{mobileNo}")
    public ResponseEntity<Map<String, Object>> getOtpForMobile(@PathVariable String mobileNo) {
        Map<String, Object> response = new HashMap<>();
        try {
            String otp = smsService.sendOTP(mobileNo, "registered mobile number");
            if (otp == null) {
                response.put("error", "Failed to send OTP.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("otp", otp);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("change/password")
    public ResponseEntity<Map<String, Object>> getOtpForMobile(@RequestBody Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        try{
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            String currnet=(String)body.get("current");
            String New=(String)body.get("new");
            String confirm=(String)body.get("confirm");
            User user=studentService.changePassword(userName,currnet,New,confirm);
            if(user==null){
                response.put("message","Failed to Update Password");
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUserName(), New));

            final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUserName());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());


            response.put("token", jwt);
            response.put("user",user);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            response.put("error","Failed to Update Password");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }
    }

    @GetMapping("certificate")
    public ResponseEntity<?> getCertificates(){
        Map<String, Object> response=new HashMap<>();
        try{
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            List<Certificate> certificateList=studentService.getAllCertificates(userName);
            if(certificateList==null){
                response.put("message","Data not found");
                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }
            response.put("certificates",certificateList);
            return new ResponseEntity<>(response,HttpStatus.OK);

        }catch (Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }
}

