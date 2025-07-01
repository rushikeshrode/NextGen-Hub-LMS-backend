package com.nextgenhub.lms.entities;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "mock_tests")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MockTest {

    @Id
    private String id;

    @NonNull
    private String title; // Title of the mock test

    @NonNull
    private List<Question> questions; // List of questions in the mock test

    @NonNull
    private String courseId; // Associated course ID

    private String createdAt; // Timestamp of when the mock test was created
}
