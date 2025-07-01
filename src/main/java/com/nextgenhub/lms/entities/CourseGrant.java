package com.nextgenhub.lms.entities;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "course-pending-approval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CourseGrant {
    @Id
    private String id;

    @NonNull
    private String title;

    @NonNull
    private String description;
    @NonNull
    private String teacherName;
    @NonNull
    private String createdDate;
    @NonNull
    private String category;

    @NonNull
    private String teacherId;

    @NonNull
    private List<String> joinedStudents;

    @DBRef
    private List<MockTest> MockTests;

    private List<Lesson> lessons;


}
