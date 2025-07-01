package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.MockTest;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MockTestServiceRepo extends MongoRepository<MockTest, String> {
    List<MockTest> findByCourseId(String courseId);

    void deleteByCourseId(String id);
}
