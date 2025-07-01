package com.nextgenhub.lms.entities;

import lombok.*;
import com.mongodb.lang.NonNull;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


import java.util.List;

@Document(collection = "Users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;
    @NonNull
    private String name;

    @Indexed(unique = true)
    @NonNull
    private String userName;

    @NonNull
    private String email;

    @NonNull
    private String password;

    private String mobileNo;

    @NonNull
    private List<String> roles;

//    For Student Role
    @NonNull
    private List<String> enrolledCourses;
    @NonNull
    private List<String> progressIdList;

    @NonNull
    private List<String> createdCourcesId;
}
