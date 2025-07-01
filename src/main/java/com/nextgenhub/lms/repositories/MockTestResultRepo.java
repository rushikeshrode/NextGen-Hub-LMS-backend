package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.MockTest;
import com.nextgenhub.lms.entities.MockTestResult;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MockTestResultRepo extends MongoRepository<MockTestResult, String> {
    public List<MockTestResult> findByUserId(String id);
    MockTestResult findByUserIdAndMockTestId(String userId,String testId);
    void deleteByCourseId(String courseId);

    List<MockTestResult> findByUserIdAndCourseId(String userId, String courseId);
    void deleteByUserId(String userId);
    void deleteByUserIdAndCourseId(String userId,String testId);
}
