package com.nextgenhub.lms.entities;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class LessonProgress {

    private String lessonId;
    private boolean completed;

}
