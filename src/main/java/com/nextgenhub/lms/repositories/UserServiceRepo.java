package com.nextgenhub.lms.repositories;

import com.nextgenhub.lms.entities.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserServiceRepo extends MongoRepository<User, String> {
    User findByUserName(String username);
    Optional<User> findById(String id);
}
