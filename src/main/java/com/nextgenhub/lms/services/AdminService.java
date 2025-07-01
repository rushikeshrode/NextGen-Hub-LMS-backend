package com.nextgenhub.lms.services;

import com.nextgenhub.lms.entities.*;
import com.nextgenhub.lms.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AdminService {

    @Autowired
    UserServiceRepo userServiceRepo;
    @Autowired
    TeacherRepo teacherRepo;
    @Autowired
    CourseGrantRepo courseGrantRepo;

    @Autowired
    CourseServiceRepo courseServiceRepo;
    @Autowired
    ProgressServiceRepo progressServiceRepo;
    @Autowired
    MockTestResultRepo mockTestResultRepo;


    @Autowired
    EmailService emailService;
    @Autowired
    CourseService courseService;


    public List<User> getUsers(){
        return userServiceRepo.findAll();
    }
    public List<Course> getCourses(){
        return courseServiceRepo.findAll();
    }

    public List<Teacher> getPendingTeachers(){
        return teacherRepo.findAll();
    }
    public List<CourseGrant> getPendingCourses(){
        return courseGrantRepo.findAll();
    }

    public Map<String, Object> getSummary(){
        Map<String ,Object> res=new HashMap<>();
        List<User> users=userServiceRepo.findAll();
        List<CourseGrant> pendCourses=courseGrantRepo.findAll();
        List<Teacher> pendTeachers=teacherRepo.findAll();
        List<Course> courses=courseServiceRepo.findAll();
        int student=0,teacher=0,course=0,pendCourse=0,test=0,pendTeacher=0;
        for(User user: users){
            if(user.getRoles().contains("TEACHER")&&!user.getRoles().contains("ADMIN")){
                teacher++;
            }
            if(user.getRoles().contains("STUDENT")&&!user.getRoles().contains("TEACHER")){
                student++;
            }
        }
        for(Course cour:courses){
            test+=cour.getMockTests().size();
        }
        course=courses.size();
        pendCourse=pendCourses.size();
        pendTeacher=pendTeachers.size();
        res.put("student",student);
        res.put("teacher",teacher);
        res.put("course",course);
        res.put("test",test);
        res.put("pendingCourses",pendCourse);
        res.put("pendingTeachers",pendTeacher);
        return  res;
    }


    public User createTeacher(String teacherId){
        Teacher teacher=teacherRepo.findById(teacherId).orElse(null);

        if(teacher==null){
            return null;
        }
        User exist=userServiceRepo.findByUserName(teacher.getUserName());
        if(exist!=null){
            teacher.setUserName(teacher.getUserName()+(teacher.getMobileNo().substring(0, 4)));
        }
        User user=new User();
        user.setUserName(teacher.getUserName());
        user.setName(teacher.getName());
        user.setEmail(teacher.getEmail());
        user.setPassword(teacher.getPassword());
        user.setMobileNo(teacher.getMobileNo());
        user.setRoles(teacher.getRoles());
        user.setEnrolledCourses(new ArrayList<>());
        user.setCreatedCourcesId(new ArrayList<>());
        user.setProgressIdList(new ArrayList<>());
        User newUser=userServiceRepo.save(user);
        teacherRepo.deleteById(teacherId);

        String subject = "Account Successfully Created – Welcome to NextGen LMS!";
        String Message="Dear "+newUser.getName()+",\n\n" +
                "Congratulations! Your teacher account has been successfully created on the NextGen LMS platform.\n\n" +
                "User Name : "+newUser.getUserName()+"\n"+
                "You can now log in and start creating your courses, engaging with students, and managing your teaching resources.\n\n" +
                "We're excited to have you onboard!\n\n" +
                "Best regards,\n" +
                "NextGen LMS Team";
        emailService.sendEmail(user.getEmail(), subject,Message);
        return newUser;
    }
    public Teacher rejectTeacher(String teacherId){
        Teacher teacher=teacherRepo.findById(teacherId).orElse(null);

        if(teacher==null){
            return null;
        }

        teacherRepo.deleteById(teacherId);

        String subject = "Account Request Rejected – NextGen LMS";
        String message = "Dear " + teacher.getName() + ",\n\n" +
                "Thank you for your interest in joining the NextGen Learning platform as a teacher.\n\n" +
                "After careful review of your application, we regret to inform you that your account request has not been approved at this time.\n\n" +
                "If you believe this was a mistake or would like to provide additional information, feel free to contact our support team.\n\n" +
                "We appreciate your understanding.\n\n" +
                "Best regards,\n" +
                "NextGen LMS Team";

        emailService.sendEmail(teacher.getEmail(), subject, message);

        return teacher;
    }


    @Transactional
    public Course approveCourse(String courseId)
    {
        CourseGrant courseGrant=courseGrantRepo.findById(courseId).orElse(null);
        if(courseGrant==null){
            return null;
        }
        User teacher=userServiceRepo.findById(courseGrant.getTeacherId()).orElse(null);
        if(teacher==null){
            return null;
        }
        Course course=new Course();
        course.setTitle(courseGrant.getTitle());
        course.setDescription(courseGrant.getDescription());
        course.setTeacherName(courseGrant.getTeacherName());
        Date date=new Date();
        SimpleDateFormat formater=new SimpleDateFormat("dd/MM/yyyy");
        String formated=formater.format(date);
        course.setCreatedDate(formated);
        course.setTeacherId(courseGrant.getTeacherId());
        course.setJoinedStudents(new ArrayList<>());
        course.setLessons(courseGrant.getLessons());
        course.setMockTests(courseGrant.getMockTests());
        course.setCategory(courseGrant.getCategory());

        Course saved=courseServiceRepo.save(course);
        if(saved!=null){
            List<String> list=teacher.getCreatedCourcesId();
            if(list==null)
                list=new ArrayList<>();
            list.add(saved.getId());

            teacher.setCreatedCourcesId(list);
            userServiceRepo.save(teacher);

            String subject = "Account Successfully Created – Welcome to NextGen Learning!";
            String Message = """
                            Dear [Teacher Name],
                            
                            Great news! Your course titled "[Course Title]" has been reviewed and approved by the admin team.
                            
                            It is now live on the NextGen Learning platform and available for students to explore and enroll in.
                            
                            Thank you for contributing to the learning community. We look forward to seeing learners benefit from your expertise.
                            
                            Best regards,  
                            NextGen LMS Team
                            """.replace("[Teacher Name]", teacher.getName())
                               .replace("[Course Title]", saved.getTitle());

            emailService.sendEmail(teacher.getEmail(), subject,Message);
            courseGrantRepo.deleteById(courseId);
            return saved;
        }else{
            return null;
        }
    }

    @Transactional
    public CourseGrant rejectCourse(String courseId){
        CourseGrant courseGrant=courseGrantRepo.findById(courseId).orElse(null);
        if (courseGrant==null){
            return null;
        }
        User teacher=userServiceRepo.findById(courseGrant.getTeacherId()).orElse(null);
        if (teacher==null){
            return null;
        }
        String rejectionMessage = """
                    Dear [Teacher Name],
                    
                    Thank you for submitting your course titled "[Course Title]" to NextGen Learning.
                    
                    We regret to inform you that your course has not been approved at this time. Our admin team has reviewed the content and determined that it does not meet our current requirements.
                    
                    Please review the feedback provided and feel free to revise and resubmit your course for approval.
                    
                    We appreciate your contribution and look forward to working with you again.
                    
                    Best regards,  
                    NextGen Learning Team
                    """.replace("[Teacher Name]",courseGrant.getTeacherName() )
                       .replace("[Course Title]", courseGrant.getTitle());
        courseGrantRepo.deleteById(courseId);
        emailService.sendEmail(teacher.getEmail(),"Update on Your Course Submission: Not Approved",rejectionMessage);
        return courseGrant;
    }
    @Transactional
    public User deleteUser(String userId){
        User user=userServiceRepo.findById(userId).orElse(null);
        if(user==null){
            return null;
        }
        for(String courseId:user.getEnrolledCourses()){
            Course course=courseServiceRepo.findById(courseId).orElse(null);
            if(course!=null) {
                course.getJoinedStudents().remove(user.getId());
                mockTestResultRepo.deleteByUserIdAndCourseId(user.getId(), course.getId());
                courseServiceRepo.save(course);
            }
        }
        if(user.getProgressIdList()!=null) {
            for (String progressId : user.getProgressIdList()) {
                progressServiceRepo.deleteById(progressId);
            }
        }
        if(user.getCreatedCourcesId()!=null&&!user.getCreatedCourcesId().isEmpty()) {
            for (String courseId : user.getCreatedCourcesId()) {
                courseService.deleteCourse(courseId);
            }
        }
        userServiceRepo.deleteById(user.getId());
        String emailMessage= """
                Dear [User's First Name],
                                
                We hope this message finds you well.
                                
                This is to inform you that your account with NextGen LMS has been permanently deleted. As part of this action, all of your associated data has been removed from our systems and cannot be recovered.
                                
                Important Notes:
                - Your account and all associated content, including personal data, have been permanently deleted.
                - Once deleted, the process cannot be undone, and all information is irretrievable.
                - If you had any active subscriptions, they have been canceled, and any future billing will cease.
                                
                We understand that this might be a significant change. If you have any concerns or if you believe this action was taken in error, please don't hesitate to contact us at [support email or phone number]. We are here to assist you.
                                
                Thank you for being part of our community. We wish you all the best in your future endeavors.
                                
                Sincerely, \s
                The NextGen LMS Team
                                
                """.replace("[User's First Name]",user.getName());
        emailService.sendEmail(user.getEmail(),"Your Account Has Been Permanently Deleted by Admin",emailMessage);
        return user;
    }

    @Transactional
    public boolean sendMail(String to,String subject, String body){
        try {
            emailService.sendEmail(to, subject, body);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
