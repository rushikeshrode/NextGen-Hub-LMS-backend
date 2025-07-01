package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.entities.Progress;
import com.nextgenhub.lms.services.CourseService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.naming.spi.ResolveResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private CourseService courseService;

    private final String message = "message";
    private final String error = "error";

    @GetMapping("{teacherId}/course/{courseId}/progress")

    public ResponseEntity<Map<String, Object>> getStudentProgress(@PathVariable String teacherId, @PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Progress> progresses = courseService.getStudentProgressForCourse(teacherId, courseId);
            if (progresses == null || progresses.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("progresses", progresses);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{courseId}/removeStudent/{studentId}")
    public ResponseEntity<Map<String, Object>> removeStudentFromCourse(
            @PathVariable String courseId,
            @PathVariable String studentId) {

        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            String teacherName=authentication.getName();
            courseService.removeStudentFromCourse(teacherName, studentId, courseId);
            response.put(message, "Student removed for course successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (RuntimeException e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{courseId}/student/{studentId}/getDetails")
    public ResponseEntity<Map<String,Object>> getAllInformationOfStudent(@PathVariable String courseId,@PathVariable String studentId){
        Map<String,Object> response=new HashMap<>();
        try{
            Map<String,Object> data=courseService.getStudentsAllDetails(courseId,studentId);
            if(data==null)
            {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            response.put("details",data);
            return new ResponseEntity<>(response,HttpStatus.OK);
        }catch (Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
