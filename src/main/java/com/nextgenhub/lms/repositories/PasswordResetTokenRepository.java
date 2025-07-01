package com.nextgenhub.lms.repositories;
import com.nextgenhub.lms.entities.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUserName(String userName);
}
