package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.Teacher;
import com.nextgenhub.lms.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TeacherRepo  extends MongoRepository<Teacher, String> {
    Teacher findByUserName(String teacherName);
    Optional<Teacher> findById(String id);
}