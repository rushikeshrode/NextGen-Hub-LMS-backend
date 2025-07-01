package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.Teacher;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.services.CourseService;
import com.nextgenhub.lms.services.PublicTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired
    private PublicTaskService publicTaskService;
    @Autowired
    private CourseService courseService;

    private final String message = "message";
    private final String error = "error";


    @PostMapping("create-user")
    public ResponseEntity<Map<String, Object>> createStudent(@RequestBody User newUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (newUser == null) {
                response.put(message, "insufficient data.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            User saved = publicTaskService.createUser(newUser);

            if (saved == null) {
                response.put(message, "Student with UserName '" + newUser.getUserName() + "' already exist.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put(message, "User registered successfully!");
            response.put("user", saved);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("courses")
    public ResponseEntity<Map<String , Object>> getAllCourses(){
        Map<String,Object> response=new HashMap<>();
        try{
            List<Course> courses=courseService.getAllCourseDetails();
            if(courses.isEmpty())
            {
                response.put("message","Courses not Found.");
                return new ResponseEntity<>(response,HttpStatus.NOT_FOUND);
            }
            response.put("courses",courses);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch(Exception e)
        {
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping("create-teacher")
    public ResponseEntity<Map<String, Object>> requestTeacherAccount(@RequestBody Teacher newUser) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (newUser == null) {
                response.put(message, "insufficient data.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            Teacher saved = publicTaskService.teacherRequest(newUser);

            if (saved == null) {
                response.put(message, "Teacher with UserName '" + newUser.getUserName() + "' already exist.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put(message, "Request send Sucessfully.");
            response.put("user", saved);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }



    @GetMapping("health")
    public String healthCheck() {
        return "Application Status :: OK";
    }


}
