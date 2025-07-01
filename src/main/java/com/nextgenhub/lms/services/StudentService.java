package com.nextgenhub.lms.services;

import com.nextgenhub.lms.entities.Certificate;
import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.repositories.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Component
public class StudentService {

    @Autowired
    private UserServiceRepo userServiceRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private CourseServiceRepo courseServiceRepo;
    @Autowired
    private ProgressServiceRepo progressServiceRepo;
    @Autowired
    private MockTestServiceRepo mockTestServiceRepo;

    @Autowired
    private MockTestResultRepo mockTestResultRepo;

    @Autowired
    private CertificateRepo certificateRepo;



    private static final BCryptPasswordEncoder passwordEncoder=new BCryptPasswordEncoder();


    public User updateStudent(String userName, User newUser)
    {
        User User =userServiceRepo.findByUserName(userName);
        if(User ==null)
            throw new RuntimeException("BaseUser Not Found with Email: "+userName);

        User.setName(checkName(newUser)? User.getName(): newUser.getName());
        User.setEmail(checkEmail(newUser)? User.getEmail(): newUser.getEmail());
        User.setMobileNo(checkMobileNo(newUser)? User.getMobileNo(): newUser.getMobileNo());
        User.setPassword(checkPassword(newUser)? User.getPassword(): passwordEncoder.encode(newUser.getPassword()));

        return userServiceRepo.save(User);
    }
    public User updateDetails(String userName,String name,String email){
        User User =userServiceRepo.findByUserName(userName);
        if(User ==null)
            throw new RuntimeException("BaseUser Not Found with User Name: "+userName);

        User.setName(name.trim());
        User.setEmail(email.trim());
        return userServiceRepo.save(User);
    }
    public User changeMobileNo(String userName,String mobileNo){
        User User =userServiceRepo.findByUserName(userName);
        if(User ==null)
            return null;
        if(mobileNo.startsWith("+91"))
        {
            mobileNo = mobileNo.replaceAll("^\\+\\d{1,3}", "");
        }
        if(mobileNo.trim().length()!=10)
            return null;
        User.setMobileNo(mobileNo);
        return userServiceRepo.save(User);
    }

    public User changePassword(String userName, String currentPass,String newPass,String confirmPass){
        User user =userServiceRepo.findByUserName(userName);
        if(user ==null)
            throw new RuntimeException("BaseUser Not Found with Email: "+userName);

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Compare passwords
        boolean isMatch = passwordEncoder.matches(currentPass, user.getPassword());

        if(!isMatch){
            System.out.println("Password not Matched");
            return  null;
        }
        if(!Objects.equals(newPass, confirmPass)){
            System.out.println("new Password and confirmed Password not Matched");
            return  null;
        }
        user.setPassword(passwordEncoder.encode(newPass));

        return userServiceRepo.save(user);
    }

//    public User deleteStudent(String userName){
//        User user =UserServiceRepo.findByUserName(userName);
//        if(user ==null)
//            throw  new RuntimeException("BaseUser Not Found with Email: "+userName);
//        if(!user.getEnrolledCourses().isEmpty()){
//            List<Course> courses=user.getEnrolledCourses();
//        }
//
//
//        // Remove user from all courses where they are enrolled.
//        Query query = new Query(Criteria.where("joinedStudents._id").is(user.getId()));
//        Update update = new Update().pull("joinedStudents", new Query(Criteria.where("_id").is(user.getId())));
//
//        mongoTemplate.updateMulti(query, update, Course.class);
//        // Delete User.
//        UserServiceRepo.deleteById(user.getId());
//
//        return user;
//    }

    @Transactional
    public User deleteUser(String userName) {
        User user = userServiceRepo.findByUserName(userName);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Remove user from enrolled courses
        if (user.getEnrolledCourses() != null) {
            for (String courseId : user.getEnrolledCourses()) {
                Course course=courseServiceRepo.findById(courseId).orElse(null);
                course.getJoinedStudents().removeIf(student -> student.equals(user.getId()));
                courseServiceRepo.save(course);
            }
        }

        // Remove user's progress records
        progressServiceRepo.deleteByStudentId(user.getId());

        // Remove user's mock test records
        mockTestResultRepo.deleteByUserId(user.getId());
        // If user is a teacher, delete created courses
        if (user.getCreatedCourcesId() != null) {
            for (String courseId : user.getCreatedCourcesId()) {
                Course course=courseServiceRepo.findById(courseId).orElse(null);
                // Remove course from students who enrolled
                for (String id : course.getJoinedStudents()) {
                    User student =userServiceRepo.findById(id).orElse(null);
                    student.getEnrolledCourses().removeIf(c -> c.equals(course.getId()));
                    userServiceRepo.save(student);
                }
                mockTestServiceRepo.deleteByCourseId(course.getId());
                courseServiceRepo.delete(course);
            }
        }

        // Finally, delete the user
        userServiceRepo.delete(user);
        return user;
    }
    public User getStudent(String userName)
    {
        return userServiceRepo.findByUserName(userName);

    }

    public List<Certificate> getAllCertificates(String userName){
        User user =userServiceRepo.findByUserName(userName);
        if(user==null){
            return null;
        }
        List<Certificate> certificates=certificateRepo.findByStudentId(user.getId());
        return certificates;
    }


    private boolean checkName(User User)
    {
        return (User.getName()==null) || (User.getName().isEmpty());
    }
    private boolean checkPassword(User User)
    {
        return (User.getPassword()==null) || (User.getPassword().isEmpty());
    }
    private boolean checkEmail(User User) {
        return (User.getEmail()==null) || (User.getEmail().isEmpty());
    }
    private boolean checkMobileNo(User User) {
            return (User.getMobileNo()==null) || (User.getMobileNo().isEmpty());
    }


    public User updateUser(User user) {
        return userServiceRepo.save(user);
    }
}
