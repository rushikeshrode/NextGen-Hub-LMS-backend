package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.Certificate;
import com.nextgenhub.lms.entities.CourseGrant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CertificateRepo extends MongoRepository<Certificate, String> {

    Certificate findByStudentIdAndCourseId(String studentId,String courseId);
    List<Certificate> findByStudentId(String studentId);
}
