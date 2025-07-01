package com.nextgenhub.lms.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    private String id;
    private String userName;  // Store userName instead of email
    private String token;
    @CreatedDate
    @Indexed(expireAfterSeconds = 900)// time in seconds to set the validity of token
    private Date createdAt;

    public PasswordResetToken(String userName, String token) {
        this.userName = userName;
        this.token = token;
        this.createdAt = new Date();
    }

}

