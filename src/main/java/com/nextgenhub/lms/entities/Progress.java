package com.nextgenhub.lms.entities;

import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "Progress")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Progress {
    @Id
    private String Id;
    @NonNull
    private String studentId;
    @NonNull
    private String courseId;

    private int currentLessonIndex;

    private String enrolledDate;

    private String completionDate;
    private double completionPercentage;
    private boolean isCourseCompleted;

    private List<LessonProgress> lessonProgressList;

    public Progress(double completionPercentage, boolean courseCompleted) {
        this.completionPercentage=completionPercentage;
        this.isCourseCompleted=courseCompleted;
    }
}
