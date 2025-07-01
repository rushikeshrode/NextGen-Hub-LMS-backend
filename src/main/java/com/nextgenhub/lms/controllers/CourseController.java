package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.CourseGrant;
import com.nextgenhub.lms.entities.Lesson;
import com.nextgenhub.lms.entities.Progress;
import com.nextgenhub.lms.services.CourseService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    private final String message = "message";
    private final String error = "error";

    @PostMapping
    public ResponseEntity<?> createCourse( @RequestBody CourseGrant course) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            CourseGrant saved = courseService.setCourseForApproval(userName, course);
            if (saved == null){
                response.put(error, "Failed to create a course");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("course", saved);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        }

    }

    @GetMapping("{courseId}")
    public ResponseEntity<?> getCourseDetails(@PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course saved = courseService.getCourseDetails(courseId);
            response.put("course", saved);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        }
    }
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingCourses() {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            List<CourseGrant> saved = courseService.getPendingCourses(userName);
            response.put("pending", saved);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        }
    }

    @DeleteMapping("/pending-course/{courseId}")
    public ResponseEntity<?> deletePendingCourses(@PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            CourseGrant deleted = courseService.deletePendingCourses(userName,courseId);
            response.put("message", "Course Deleted Sucessfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        }
    }


    @PutMapping("{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable String courseId, @RequestBody Course course) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course saved = courseService.updateCourse(courseId, course);
            if (saved == null){
                response.put(error, "Failed to update course");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("course", saved);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("{courseId}/enrolled-student")
    public ResponseEntity<?> getEnrolledStudentsOfCourse(@PathVariable String courseId){
        Map<String,Object> response=new HashMap<>();
        try {
            List<?> list = courseService.getentrolledStudents(courseId);
            if (list == null){
                response.put(error, "Failed Fetch Data.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("enrolledStudents", list);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course course = courseService.deleteCourse(courseId);
            if (course == null){
                response.put(error, "Failed to delete course");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("course", course);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);

        }
    }

    @PostMapping("{courseId}/lesson")
    public ResponseEntity<?> addLesson(@PathVariable String courseId, @RequestBody Lesson lesson) {
        Map<String, Object> response = new HashMap<>();
        try {
            Course course = courseService.deleteCourse(courseId);
            if (course == null){
                response.put(error, "Failed to add new  lesson");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("course", course);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("{courseId}/{lessonId}")
    public ResponseEntity<?> deleteLesson(@PathVariable String courseId, @PathVariable String lessonId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Lesson lesson = courseService.deleteLesson(courseId, lessonId);
            if (lesson == null){
                response.put(error, "Failed to delete lesson");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("lesson", lesson);
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("{courseId}/{lessonId}")
    public ResponseEntity<?> updateLesson(@PathVariable String courseId, @PathVariable String lessonId, @RequestBody Lesson lesson) {
        Map<String, Object> response = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();

        try {
            Course course = courseService.updateLession(courseId, lessonId, lesson);
            if (course == null){
                response.put(error, "Failed to update lesson");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            response.put("course", course);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/progress/{courseId}")
    public ResponseEntity<?> getProgressDetails(@PathVariable String courseId){
        Map<String,Object> response=new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            Progress progress = courseService.getProgress(courseId, userName);
            if (progress == null) {
                response.put(message, "Progress Not Found.");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            response.put("progress", progress);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e)
        {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("progress/{courseId}/{lessonId}")
    public ResponseEntity<?> updateLessonProgress(@PathVariable String courseId,@PathVariable String lessonId)
    {
        Map<String,Object> response=new HashMap<>();
        try{
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            int status=courseService.updateLessonProgress(userName,courseId,lessonId,true);
            if(status== -1||status==0){
                response.put(message,"Progress Updation Failed.");
                response.put("status",status);
            }else {
                response.put(message, "Progress Updation Sucessed.");
                response.put("status", status);
            }
            return new ResponseEntity<>(response,HttpStatus.OK);

        }catch (Exception e){
            response.put("error",e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }


}
