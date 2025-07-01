package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.CourseGrant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CourseGrantRepo extends MongoRepository<CourseGrant, String> {
//    CourseGrant findByUserName(String teacherName);
    List<CourseGrant> findByTeacherName(String teacherName);
        Optional<CourseGrant> findById(String id);
        }