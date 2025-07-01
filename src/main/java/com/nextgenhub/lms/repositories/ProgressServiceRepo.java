package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.Progress;
//import org.bson.types.String;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProgressServiceRepo  extends MongoRepository<Progress, String> {
    void deleteByStudentIdAndCourseId(String studentId, String courseId);
    void deleteByStudentId(String studentId);
    void deleteByCourseId(String courseId);

    List<Progress> findByCourseId(String couseId);
    Progress findByCourseIdAndStudentId(String courseId,String studentId);
    Progress findByIdAndCourseId(String id,String courseId);
}
