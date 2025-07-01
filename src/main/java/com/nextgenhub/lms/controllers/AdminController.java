package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.CourseGrant;
import com.nextgenhub.lms.entities.Teacher;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.repositories.UserServiceRepo;
import com.nextgenhub.lms.services.AdminService;
import com.nextgenhub.lms.services.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    AdminService adminService;

    @Autowired
    CourseService courseService;


    @GetMapping("/summary")
    public ResponseEntity<Map<String,Object>> getSummary(){
        Map<String,Object> response=new HashMap<>();
        try{
            Map<String,Object> summary=adminService.getSummary();
            if(summary==null||summary.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("details",summary);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/all-users")
    public ResponseEntity<Map<String,Object>> getAllUsers(){
        Map<String,Object> response=new HashMap<>();
        try{
            List<User> users=adminService.getUsers();
            if(users==null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("users",users);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/all-courses")
    public ResponseEntity<Map<String,Object>> getAllCourses(){
        Map<String,Object> response=new HashMap<>();
        try{
            List<Course> courses=adminService.getCourses();
            if(courses==null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("courses",courses);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/teacher-requests")
    public ResponseEntity<Map<String,Object>> pendingTeachers(){
        Map<String,Object> response=new HashMap<>();
        try{
            List<Teacher> users=adminService.getPendingTeachers();
            if(users==null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("pending",users);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping("/course-requests")
    public ResponseEntity<Map<String,Object>> pendingCourses(){
        Map<String,Object> response=new HashMap<>();
        try{
            List<CourseGrant> courses=adminService.getPendingCourses();
            if(courses==null){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("pending",courses);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }


    @DeleteMapping("delete-user/{userId}")
    public ResponseEntity<Map<String,Object>> deleteUser(@PathVariable String userId){
        Map<String,Object> response =new HashMap<>();
        try{
            User user=adminService.deleteUser(userId);
            if(user==null){
                response.put("error","Failed to delete the User Accourt. Try after some time");
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
            response.put("message","User Account has been deleted sucessfully.");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }
    @DeleteMapping("delete-course/{courseId}")
    public ResponseEntity<Map<String,Object>> deleteCourse(@PathVariable String courseId){
        Map<String,Object> response =new HashMap<>();
        try{
            Course course=courseService.deleteCourse(courseId);
            if(course==null){
                response.put("error","Failed to delete the Course. Try after some time");
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
            response.put("message","Course has deleted sucessfully.");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/accept-request/{userId}")
    public ResponseEntity<Map<String , Object>> acceptTeacherRequest(@PathVariable String userId){
        Map<String , Object> response=new HashMap<>();
        try{
            User user=adminService.createTeacher(userId);
            if(user==null){
                response.put("error","pending request not found.");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("message","Request Accepted.");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/reject-request/{userId}")
    public ResponseEntity<Map<String , Object>> regestTeacherRequest(@PathVariable String userId){
        Map<String , Object> response=new HashMap<>();
        try{
            Teacher user=adminService.rejectTeacher(userId);
            if(user==null){
                response.put("error","pending request not found.");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("message","Request Rejected.");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }


    @PostMapping("/approve-course/{courseId}")
    public ResponseEntity<Map<String , Object>> approveCourse(@PathVariable String courseId){
        Map<String , Object> response=new HashMap<>();
        try{
            Course course=adminService.approveCourse(courseId);
            if(course==null){
                response.put("error","pending request not found.");
                return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("message","Course Approved.");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/reject-course/{courseId}")
    public ResponseEntity<Map<String , Object>> rejectCourse(@PathVariable String courseId){
        Map<String , Object> response=new HashMap<>();
        try{
            CourseGrant course=adminService.rejectCourse(courseId);
            if(course==null){
                response.put("error","pending request not found.");
                return  new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("message","Course Rejected.");

            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e){
            response.put("error",e.getMessage());
            return  new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("send-mail")
    public ResponseEntity<Map<String,Object>> sendMail(@RequestBody Map<String, Object> mail){
        Map<String,Object> response=new HashMap<>();
        try{
            String to= (String) mail.get("to");
            String subject= (String) mail.get("subject");
            String body= (String) mail.get("body");
            System.out.println(to);
            System.out.println(subject);
            System.out.println(body);
            boolean status=adminService.sendMail(to,subject,body);
            if(status){
                response.put("message","Mail send to the user's registered email id.");
                return new ResponseEntity<>(response,HttpStatus.OK);
            }
            response.put("message","Faild to send mail on user's registered email id, Email id not exist.");
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }


}
