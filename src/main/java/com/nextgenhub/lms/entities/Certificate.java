package com.nextgenhub.lms.entities;

import com.mongodb.lang.NonNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "certificates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Certificate {
    @Id
    private String id;

    @NonNull
    private String courseId;

    @NonNull
    private String studentId;

    @NonNull
    private String completionDate;

}
