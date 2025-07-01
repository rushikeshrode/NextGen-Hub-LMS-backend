package com.nextgenhub.lms.entities;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Lesson {

    private String id;
    private String title;
    private String content;
    private String resourceUrl;
}
