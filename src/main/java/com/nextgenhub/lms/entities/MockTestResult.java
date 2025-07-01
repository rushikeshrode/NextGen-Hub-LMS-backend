package com.nextgenhub.lms.entities;
import com.mongodb.lang.NonNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "mock_test_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MockTestResult {

    @Id
    private String id;

    @NonNull
    private String userId;
    @NonNull
    private String courseId;
    // User who took the mock test
    @NonNull
    private String mockTestId;

    @CreatedDate
    @NonNull
    private String submitedDate;
    @NonNull
    private List<String> userAnswers;  // User's answers to the mock test questions
    @NonNull
    private double score;  // Score achieved in the mock test
    @NonNull
    private boolean isPassed;  // Whether the user passed the mock test or not
}
