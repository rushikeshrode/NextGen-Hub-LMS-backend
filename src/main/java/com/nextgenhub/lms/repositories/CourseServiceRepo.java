package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

//@Repository
public interface CourseServiceRepo extends MongoRepository<Course, String> {
    Course findByTitle(String title);

//    Optional<Course> findById(String courseId);
    void deleteById(String id);

}
