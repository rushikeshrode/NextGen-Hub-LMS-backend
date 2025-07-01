package com.nextgenhub.lms.services;

import com.nextgenhub.lms.entities.Teacher;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.repositories.TeacherRepo;
import com.nextgenhub.lms.repositories.UserServiceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class PublicTaskService {

    @Autowired
    UserServiceRepo userServiceRepo;
    @Autowired
    EmailService emailService;

    @Autowired
    TeacherRepo teacherRepo;

    private static final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();

    @Transactional
    public User createUser(User user)
    {
        if(checkName(user))
            throw new RuntimeException("Name Required");
        if(checkUserName(user))
            throw new RuntimeException("Name Required");
        if(checkPassword(user))
            throw new RuntimeException("Password Required");
        if(checkEmail(user))
            throw new RuntimeException("Email Required.");
        if(checkMobileNo(user))
            throw new RuntimeException("MobileNo. Required.");
        if(user.getRoles().isEmpty()) {
//            user.setRoles(List.of("STUDENT"));
            throw new RuntimeException("Specify the role of User.");
        }

        if(user.getRoles().contains("TEACHER")&&!user.getRoles().contains("ADMIN"))
            return null;

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setEnrolledCourses(new ArrayList<>());
        user.setProgressIdList(new ArrayList<>());
        if(user.getRoles().contains("TEACHER"))
            user.setCreatedCourcesId(new ArrayList<>());

        User newUser= userServiceRepo.save(user);
        if(user!=null){
            String subject = "Account Successfully Created – Welcome to NextGen Learning!";
            String Message = String.format("""
                    Dear %s,
                    
                    Welcome to NextGen Learning!
                    
                    Your student account has been successfully created. You can now log in, enroll in available courses, and start your learning journey.
                    
                    We’re happy to have you with us!
                    
                    Best wishes,  
                    NextGen LMS Team
                    """, user.getName());
            emailService.sendEmail(user.getEmail(), subject,Message);
        }
        return user;

    }
    @Transactional
    public Teacher teacherRequest(Teacher user)
    {
        if(checkName(user))
            throw new RuntimeException("Name Required");
        if(checkUserName(user))
            throw new RuntimeException("Name Required");
        if(checkPassword(user))
            throw new RuntimeException("Password Required");
        if(checkEmail(user))
            throw new RuntimeException("Email Required.");
        if(checkMobileNo(user))
            throw new RuntimeException("MobileNo. Required.");
        if(user.getEducation()==null||user.getEducation().isEmpty()){
            user.setEducation("None.");
        }
        User exist=userServiceRepo.findByUserName(user.getUserName());
        if(exist!=null){
            return null;
        }
        if(user.getRoles().isEmpty()) {
//            user.setRoles(List.of("STUDENT"));
            throw new RuntimeException("Specify the role of User.");
        }
        if(user.getRoles().contains("TEACHER"))
            user.setRoles(List.of("STUDENT","TEACHER",user.getEducation()));

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setEnrolledCourses(new ArrayList<>());
        user.setProgressIdList(new ArrayList<>());
        if(user.getRoles().contains("TEACHER"))
            user.setCreatedCourcesId(new ArrayList<>());

        Teacher newTeacher= teacherRepo.save(user);
        if(newTeacher!=null){
            String subject = "Teacher Account Request Received – Awaiting Admin Approval";
            String emailBody = "Dear " + newTeacher.getName() + ",\n\n" +
                    "Thank you for your interest in joining our platform as a teacher.\n\n" +
                    "We have successfully received your request to create a teacher account. " +
                    "Our admin team will review the details you provided and take the necessary action.\n\n" +
                    "You will be notified via email once your request has been approved or if any additional information is required.\n\n" +
                    "We appreciate your patience and look forward to having you onboard.\n\n" +
                    "Best regards,\n" +
                    "NextGen LMS";
            emailService.sendEmail(newTeacher.getEmail(),subject, emailBody);
        }
        return newTeacher;
    }
    private boolean checkName(User user)
    {
        return (user.getName()==null) || (user.getName().isEmpty());
    }
    private boolean checkUserName(User user)
    {
        return (user.getUserName()==null) || (user.getUserName().isEmpty());
    }
    private boolean checkPassword(User user)
    {
        return (user.getPassword()==null) || (user.getPassword().isEmpty());
    }
    private boolean checkEmail(User user) {
        return (user.getEmail()==null) || (user.getEmail().isEmpty());
    }
    private boolean checkMobileNo(User user) {
        return (user.getMobileNo()==null) || (user.getMobileNo().isEmpty());
    }
private boolean checkName(Teacher user)
    {
        return (user.getName()==null) || (user.getName().isEmpty());
    }
    private boolean checkUserName(Teacher user)
    {
        return (user.getUserName()==null) || (user.getUserName().isEmpty());
    }
    private boolean checkPassword(Teacher user)
    {
        return (user.getPassword()==null) || (user.getPassword().isEmpty());
    }
    private boolean checkEmail(Teacher user) {
        return (user.getEmail()==null) || (user.getEmail().isEmpty());
    }
    private boolean checkMobileNo(Teacher user) {
        return (user.getMobileNo()==null) || (user.getMobileNo().isEmpty());
    }

}
