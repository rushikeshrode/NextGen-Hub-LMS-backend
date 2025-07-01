package com.nextgenhub.lms.entities;

import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Question {

    @NonNull
    private String question; // The question text

    @NonNull
    private List<String> options; // Multiple choice options

    @NonNull
    private String correctAnswer; // Correct answer
}

