package com.nextgenhub.lms.services;

import com.nextgenhub.lms.entities.*;
import com.nextgenhub.lms.repositories.*;
//import org.bson.types.String;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
@Component
public class CourseService {
    @Autowired
    UserServiceRepo userServiceRepo;
    @Autowired
    CourseServiceRepo courseServiceRepo;
    @Autowired
    ProgressServiceRepo progressServiceRepo;
    @Autowired
    MockTestResultRepo mockTestResultRepo;
    @Autowired
    CourseGrantRepo courseGrantRepo;

    @Autowired
    CertificateRepo certificateRepo;


    @Autowired
    SmsService smsService;
    @Autowired
        EmailService emailService;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Transactional
    public Course createCourse(String userName, Course course) {
        User user=userServiceRepo.findByUserName(userName);
        if(user==null)
            throw new RuntimeException("User with userName '"+userName+"' not found.");
        if(!user.getRoles().contains("TEACHER"))
            throw new RuntimeException("UnAuthorised User.");
        if(user.getRoles().contains("TEACHER")&&course.getTeacherId()==null)
            course.setTeacherId(user.getId());

        course.setTeacherName(user.getName());
        course.setTeacherId(user.getId());
        Date date = new Date(); // Get current date
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = formatter.format(date);
        course.setCreatedDate(formattedDate);
        if(course.getLessons().size()<2)
            throw new RuntimeException("Course must contain minimum 2 lessions");

        AtomicInteger index = new AtomicInteger(1);  // Start the index from 1
        List<Lesson> updatedLessons = course.getLessons().stream()
                .map(lesson -> {
                    lesson.setId("lesson" + index.getAndIncrement());
                    return lesson;
                })
                .collect(Collectors.toList());
        course.setLessons(updatedLessons);
        course.setMockTests(new ArrayList<>());
        Course saved=courseServiceRepo.save(course);
        if(saved==null)
            throw new RuntimeException("Course Creation failed.");
        List<String> list=user.getCreatedCourcesId();
        if(list==null)
            list=new ArrayList<>();
        list.add(saved.getId());

        user.setCreatedCourcesId(list);
        userServiceRepo.save(user);
        return saved;
    }

public CourseGrant setCourseForApproval(String userName, CourseGrant course) {
        User user=userServiceRepo.findByUserName(userName);
        if(user==null)
            throw new RuntimeException("User with userName '"+userName+"' not found.");
        if(!user.getRoles().contains("TEACHER"))
            throw new RuntimeException("UnAuthorised User.");
        if(user.getRoles().contains("TEACHER")&&course.getTeacherId()==null)
            course.setTeacherId(user.getId());

        course.setTeacherName(user.getName());
        course.setTeacherId(user.getId());
        Date date = new Date(); // Get current date
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = formatter.format(date);
        course.setCreatedDate(formattedDate);
        if(course.getLessons().size()<2)
            throw new RuntimeException("Course must contain minimum 2 lessions");

        AtomicInteger index = new AtomicInteger(1);  // Start the index from 1
        List<Lesson> updatedLessons = course.getLessons().stream()
                .map(lesson -> {
                    lesson.setId("lesson" + index.getAndIncrement());
                    return lesson;
                })
                .collect(Collectors.toList());
        course.setLessons(updatedLessons);
        course.setMockTests(new ArrayList<>());
        CourseGrant saved=courseGrantRepo.save(course);
        if(saved==null)
            throw new RuntimeException("Course Creation request failed.");
        String subject = "Course Submission Received: '" + saved.getTitle() + "' is Awaiting Approval";
        String Message= """
                                Dear %s,
                                
                                Thank you for submitting your course "%s" to NextGen Learning.
                                
                                Your request has been successfully received and is currently pending review by our admin team. You will be notified once your course is approved and published on the platform.
                                
                                We appreciate your contribution to helping learners grow!
                                
                                Best regards,  
                                NextGen LMS Team
                                """.formatted(user.getName(), saved.getTitle());
        emailService.sendEmail(user.getEmail(), subject,Message);
        return saved;
    }


    public List<Course> getAllCourseDetails(){
        return courseServiceRepo.findAll();
    }
     @Transactional
     public List<CourseGrant> getPendingCourses(String userName){
        User user=userServiceRepo.findByUserName(userName);

        return courseGrantRepo.findByTeacherName(user.getName());
    }



    public Course findCourseById(String id)
    {
        return courseServiceRepo.findById(id).orElse(null);
    }
    public Course saveCourse(Course course) {
        return courseServiceRepo.save(course);
    }
    public Course getCourseDetails(String courseId)
    {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        if(course==null)
            throw new RuntimeException("Course not found");

        return course;
    }

    public Course updateCourse(String courseId, Course newCourse) {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        if(course==null)
            throw new RuntimeException("Course not found");

        course.setTitle(checkTitle(newCourse)?course.getTitle():newCourse.getTitle());
        course.setDescription(checkDescription(newCourse)?course.getDescription():newCourse.getDescription());
        course.setCategory(checkCategory(newCourse)?course.getCategory():newCourse.getCategory());
        List<String> lessonsId=new ArrayList<>();

        if(newCourse.getLessons().size()>=2){
            AtomicInteger index = new AtomicInteger(1);
            for(Lesson lesson: newCourse.getLessons())
            {
                lesson.setId("lesson"+index.getAndIncrement());
                lessonsId.add(lesson.getId());
            }
            System.out.println(newCourse.getLessons());
            course.setLessons(newCourse.getLessons());
        }else{
            System.out.println("Lesson Length less than 2"+newCourse.getLessons().size());
        }

        Course saved= courseServiceRepo.save(course);
        if(saved==null){
            return null;
        }
        List<Progress> courseProgresses=progressServiceRepo.findByCourseId(course.getId());
        if(courseProgresses!=null && !courseProgresses.isEmpty()) {
            for (Progress progress : courseProgresses) {
                if (progress == null)
                    continue;
                progress.getLessonProgressList().removeIf(
                        lesson -> !lessonsId.contains(lesson.getLessonId()));

                for (String lessonId : lessonsId) {
                    boolean lessonExists = progress.getLessonProgressList().stream().anyMatch(lesson -> lesson.getLessonId().equals(lessonId));
                    if (!lessonExists) {
                        // Add the missing lesson with "completed" set to false
                        progress.getLessonProgressList().add(new LessonProgress(lessonId, false));
                    }
                }
            }
            for(Progress progress:courseProgresses){
                recalculateCompletionPercentage(progress);
                progressServiceRepo.save(progress);
            }
        }

        return saved;
    }

    public Course deleteCourse(String courseId)
    {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        if(course==null)
            throw new RuntimeException("Course not found.");

        // Remove the course from all enrolled students' enrolledCourses list
        for (String studentId : course.getJoinedStudents()) {
            User student=userServiceRepo.findById(studentId).orElse(null);
            if(student==null){
                continue;
            }
            student.getEnrolledCourses().remove(course.getId());

            // Delete progress records of the student related to this course
            Progress progress=progressServiceRepo.findByCourseIdAndStudentId(course.getId(),student.getId());

            student.getProgressIdList().remove(progress.getId());
            userServiceRepo.save(student); // Save changes to the student
//            progressServiceRepo.deleteById(progress.getId());
            progressServiceRepo.deleteByStudentIdAndCourseId(student.getId(), courseId);
            mockTestResultRepo.deleteByUserIdAndCourseId(student.getId(),courseId);
        }
        // Remove the course from the teacher's createdCourses list
        if (course.getTeacherId() != null) {
            User teacher = userServiceRepo.findById(course.getTeacherId()).orElse(null);
            if (teacher != null) {
                teacher.getCreatedCourcesId().remove(course.getId());
                userServiceRepo.save(teacher); // Save changes to the teacher
            }
        }
        // Delte Course.
        progressServiceRepo.deleteByCourseId(courseId);
        mockTestResultRepo.deleteByCourseId(courseId);
        courseServiceRepo.deleteById(courseId);

        return course;
    }
    public CourseGrant deletePendingCourses(String userName,String courseId)
    {
        CourseGrant courseGrant=courseGrantRepo.findById(courseId).orElse(null);

        if(courseGrant==null)
            throw new RuntimeException("Course not found.");

        courseGrantRepo.deleteById(courseId);
        return courseGrant;
    }

    @Transactional
    public Course addNewLession(String courseId, Lesson lesson)
    {
        if(lesson.getTitle()==null||lesson.getResourceUrl()==null||lesson.getContent()==null||lesson.getId()==null
                || lesson.getTitle().isEmpty()||lesson.getResourceUrl().isEmpty()||lesson.getContent().isEmpty()||lesson.getId().isEmpty() )
            return null;

        Course course=courseServiceRepo.findById(courseId).orElse(null);
        if(course==null)
            throw new RuntimeException("Course not found.");
        if(course.getLessons().contains(lesson))
            throw new RuntimeException("Lesson already exist.");
        course.getLessons().add(lesson);
        for (String id : course.getJoinedStudents()) {
            User student =userServiceRepo.findById(id).orElse(null);
            for (String progressId : student.getProgressIdList()) {
                Progress progress=progressServiceRepo.findById(progressId).orElse(null);
                if (progress.getCourseId().equals(courseId)) {
                    // Add new LessonProgress for the new lesson
                    progress.getLessonProgressList().add(new LessonProgress(lesson.getId(), false));
                    progressServiceRepo.save(progress);  // Save progress update
                    break;
                }
            }
        }
        return courseServiceRepo.save(course);
    }
    @Transactional
    public Lesson deleteLesson(String courseId, String lessonId) {
        // Find the course by courseId
        Course course = courseServiceRepo.findById(courseId).orElse(null);
        if (course == null) {
            return null; // Course not found
        }

        // Remove the lesson from the course's lessons list
        Lesson lessonToRemove = null;
        for (Lesson lesson : course.getLessons()) {
            if (lesson.getId().equals(lessonId)) {
                lessonToRemove = lesson;
                break;
            }
        }

        if (lessonToRemove != null) {
            course.getLessons().remove(lessonToRemove); // Remove the lesson from the course
            courseServiceRepo.save(course); // Save the updated course
        } else {
            return null; // Lesson not found in the course
        }

        // Update the progress for each student
        for (String id : course.getJoinedStudents()) {
            User student =userServiceRepo.findById(id).orElse(null);
            for (String progressId : student.getProgressIdList()) {
                Progress progress=progressServiceRepo.findById(progressId).orElse(null);
                if (progress.getCourseId().equals(courseId)) {
                    // Update the lessonProgressList by removing the lesson
                    progress.getLessonProgressList().removeIf(lessonProgress -> lessonProgress.getLessonId().equals(lessonId));

                    // Recalculate the course completion percentage after lesson removal
                    recalculateCompletionPercentage(progress);

                    progressServiceRepo.save(progress); // Save the updated progress
                }
            }
        }

        return lessonToRemove; // Lesson deleted successfully
    }



    public Course updateLession(String courseId,String id,Lesson newLesson)
    {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        if(course==null)
            throw new RuntimeException("Course not found.");
        Lesson lesson=course.getLessons().stream().filter(les->les.getId().equals(id))
                                    .findFirst()
                                    .orElse(null);
        if(lesson!=null)
        {
            lesson.setTitle(newLesson.getTitle()==null||newLesson.getTitle().isEmpty()? lesson.getTitle() : newLesson.getTitle());
            lesson.setContent(newLesson.getContent()==null||newLesson.getContent().isEmpty()? lesson.getContent() : newLesson.getContent());
            lesson.setResourceUrl(newLesson.getResourceUrl()==null||newLesson.getResourceUrl().isEmpty()? lesson.getResourceUrl() : newLesson.getResourceUrl());
            course.getLessons().removeIf(lession->lession.getId().equals(id));


        }else{ lesson=newLesson;}

        List<Lesson> list=course.getLessons();
        list.add(lesson);
        course.setLessons(list);
        return courseServiceRepo.save(course);
    }

    @Transactional
    public User enrollForCourse(String courseId,String userName) {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        User user=userServiceRepo.findByUserName(userName);
        if(course==null||user==null )
            return null;
        if(course.getJoinedStudents().stream().anyMatch(s -> s.equals(user.getId()))||
                user.getEnrolledCourses().stream().anyMatch(c -> c.equals(course.getId())))
            throw new RuntimeException("Already enrolled in this course.");

        if(course.getJoinedStudents()==null)
            course.setJoinedStudents(new ArrayList<String>(Arrays.asList(user.getId())));
        else
            course.getJoinedStudents().add(user.getId());

        user.getEnrolledCourses().add(course.getId());
        Progress progress=new Progress();
        progress.setStudentId(user.getId());
        progress.setCourseId(course.getId());
        Date date=new Date();
        SimpleDateFormat format=new SimpleDateFormat("dd/MM/YYYY");
        String formated= format.format(date);
        progress.setEnrolledDate(formated);
        progress.setCompletionDate("");
        progress.setCourseCompleted(false);
        progress.setCompletionPercentage(0.00);
        progress.setCurrentLessonIndex(0);

        List<LessonProgress> lessons=course.getLessons().stream().map(les->new LessonProgress(les.getId(),false)).collect(Collectors.toList());

        progress.setLessonProgressList(lessons);
        Progress saved=progressServiceRepo.save(progress);
        if(user.getProgressIdList()==null)
            user.setProgressIdList((new ArrayList<String>(Arrays.asList(progress.getId()))));
        else
            user.getProgressIdList().add(saved.getId());
        courseServiceRepo.save(course);
//        String message="Congratulations you are sucessfully enrolled for course '"+course.getTitle()+"' with Student Id: '"+user.getId()+"' for more details check on side.";
//        smsService.sendSms(user.getMobileNo(),message);
        User savedUser=userServiceRepo.save(user);
        if(saved!=null){
            String enrollmentMessage = String.format("""
                    Dear %s,
                    
                    Congratulations! You have successfully enrolled in the course: **%s**.
                    
                    You can now access the course content, watch lessons, and track your progress anytime on our platform.
                    
                    Wishing you a great learning experience!
                    
                    Best regards,  
                    NextGen Learning Team
                    """, savedUser.getName(), course.getTitle());
            emailService.sendEmail(savedUser.getEmail(),"Enrollment Confirmation – " + course.getTitle(),enrollmentMessage);
        }
        return savedUser;

    }

    @Transactional
    public User unenrollFromCourse(String courseId, String userName) {
        // Find the course by ID
        Course course = courseServiceRepo.findById(courseId).orElse(null);
        // Find the student by userName (or email)
        User student = userServiceRepo.findByUserName(userName);

        if (course == null || student == null) {
            return null;  // If course or student doesn't exist, return null
        }

        // Remove the student from the course's list of joined students using removeIf
        course.getJoinedStudents().removeIf(studentInCourse -> studentInCourse.equals(student.getId()));

        // Remove the course from the student's list of enrolled courses using removeIf
        student.getEnrolledCourses().removeIf(enrolledCourse -> enrolledCourse.equals(courseId));

        // Remove the progress related to this course from the student's progress list using removeIf
        List<Progress> allProgress = progressServiceRepo.findAllById(student.getProgressIdList());

        Map<String, Progress> progressMap = allProgress.stream()
                .collect(Collectors.toMap(Progress::getId, p -> p));

        student.getProgressIdList().removeIf(progressId -> {
            Progress progress = progressMap.get(progressId);  // O(1) lookup
            return progress != null && progress.getCourseId().equals(courseId);
        });

        // Also delete the progress record from the progress collection using progressServiceRepo
        progressServiceRepo.deleteByStudentIdAndCourseId(student.getId(), courseId);
        mockTestResultRepo.deleteByUserIdAndCourseId(student.getId(), courseId);
        // Save the updated course, student, and progress records
        courseServiceRepo.save(course);
        userServiceRepo.save(student);

        return student;  // Return the updated student object
    }

    @Transactional
    public boolean removeStudentFromCourse(String teacherName,String studentId, String courseId) {
        // Fetch the student and course from the repository
        User teacher =userServiceRepo.findByUserName(teacherName);
        User student = userServiceRepo.findById(studentId).orElse(null);
        Course course= courseServiceRepo.findById(courseId).orElse(null);

        if (teacher==null || student==null || course==null)
            throw  new RuntimeException("User or Course Not Found.");
        if(teacher.getCreatedCourcesId().stream().noneMatch(c->c.equals(course.getId())))
            throw  new RuntimeException("UnAuthorised User.");
            // Remove the student from the course's joinedStudents list
        course.getJoinedStudents().removeIf(s -> s.equals(studentId));

            // Remove the course from the student's enrolledCourses list
            student.getEnrolledCourses().removeIf(c -> c.equals(courseId));
        List<Progress> allProgress = progressServiceRepo.findAllById(student.getProgressIdList());

        Map<String, Progress> progressMap = allProgress.stream()
                .collect(Collectors.toMap(Progress::getId, p -> p));

        student.getProgressIdList().removeIf(progressId -> {
            Progress progress = progressMap.get(progressId);  // O(1) lookup
            return progress != null && progress.getCourseId().equals(courseId);
        });
        userServiceRepo.save(student);
        courseServiceRepo.save(course);

        // Delete the MockTestResult related to the user and course
        mockTestResultRepo.deleteByUserIdAndCourseId(studentId, courseId);
        progressServiceRepo.deleteByStudentIdAndCourseId(studentId,courseId);
        String message="Alert, "+student.getName()+" you have been removed(unenrolled) from course '"+course.getTitle()+"' by course teacher "+teacher.getName()+" and your all progress to the course also deleted permonently, due to suspicious activity. Good luck for the future progress!";
        smsService.sendSms(student.getMobileNo(),message);
            return true;
    }


    public int getCurrentLessonIndex(String courseId,String userName)
    {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        User user=userServiceRepo.findByUserName(userName);
        boolean hasProgressForCourse = user.getProgressIdList().stream()
                .map(progressId -> progressServiceRepo.findById(progressId).orElse(null))
                .anyMatch(progress -> progress != null && progress.getCourseId().equals(courseId));

        if(user==null||course==null||user.getProgressIdList().isEmpty()||!hasProgressForCourse)
            return -1;

        Progress progress =progressServiceRepo.findByCourseIdAndStudentId(courseId,user.getId());
        if (progress == null)
            return -1;

        return progress.getCurrentLessonIndex();
    }

    public List<Progress> getStudentProgressForCourse(String teacherId, String courseId) {
        Course course = courseServiceRepo.findById(courseId).orElse(null);

        if (course == null || !course.getTeacherId().equals(teacherId)) {
            throw new RuntimeException("You are not authorized to view progress for this course.");
        }

        return progressServiceRepo.findByCourseId(course.getId())
                .stream()
                .map(progress -> new Progress(progress.getCompletionPercentage(), progress.isCourseCompleted()))
                .collect(Collectors.toList());
    }

    public Progress getProgress(String courseId,String userName)
    {
        User user=userServiceRepo.findByUserName(userName);
        if(user==null||user.getProgressIdList().isEmpty())
            return null;
        Progress progress =progressServiceRepo.findByCourseIdAndStudentId(courseId,user.getId());

        return progress;
    }

    @Transactional
    public int updateLessonProgress(String userName, String courseId, String lessonId, boolean isCompleted) {
        User user = userServiceRepo.findByUserName(userName);
        Progress progress =progressServiceRepo.findByCourseIdAndStudentId(courseId,user.getId());
        if(user == null || user.getProgressIdList().isEmpty()||progress==null){
            return -1;
        }
        // 🔹 Find and update the lesson progress using Streams
        AtomicBoolean updated = new AtomicBoolean(false);
        progress.getLessonProgressList()
                .stream()
                .filter(lessProgress -> lessProgress.getLessonId().equals(lessonId))
                .findFirst()
                .ifPresent(lessonProgress -> {
                    lessonProgress.setCompleted(isCompleted);
                    updated.set(true);
                });

        if (!updated.get()) {
            return 0;  // Lesson not found in progress list
        }

        // 🔹 Recalculate course completion percentage
        long completedLessons = progress.getLessonProgressList()
                .stream()
                .filter(LessonProgress::isCompleted)
                .count();

        double completionPercentage = (completedLessons * 100.0) / progress.getLessonProgressList().size();
        progress.setCompletionPercentage(completionPercentage);
        // 🔹 Mark course as completed if all lessons are completed
        if (completionPercentage == 100.0 &&(progress.getCompletionDate()==null||progress.getCompletionDate().isEmpty())) {
            progress.setCourseCompleted(true);
            Date date=new Date();
            SimpleDateFormat dayFormat = new SimpleDateFormat("d");
            int day = Integer.parseInt(dayFormat.format(date));
            String suffix = getDaySuffix(day);

            // Format full date
            SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy");
            String formattedDate = day + suffix + " " + monthYearFormat.format(date);

            progress.setCompletionDate(formattedDate);
            generateCertificate(user.getId(),progress.getCourseId(),formattedDate);
        }else{
            List<LessonProgress> lessonProgressList=progress.getLessonProgressList();
            int index=lessonProgressList.stream()
                    .filter(lessonProgress -> !lessonProgress.isCompleted())
                    .map(lessonProgressList::indexOf)
                    .findFirst().orElse(0);
            progress.setCurrentLessonIndex(index);
        }


        // 🔹 Save updated progress
        progressServiceRepo.save(progress);
        return 1;  // Success
    }

    private static String getDaySuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        return switch (day % 10) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
    public boolean generateCertificate(String userId,String CourseId,String completionDate){
        Certificate exist=certificateRepo.findByStudentIdAndCourseId(userId,CourseId);
        if(exist==null) {
            Certificate certificate = new Certificate(null, CourseId, userId, completionDate);
            Certificate cer=certificateRepo.save(certificate);
            if(cer!=null){
                return true;
            }else{
                return false;
            }
        }else{
            return true;
        }

    }


    public List<Map<String,String>> getentrolledStudents(String courseId)
    {
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        if(course==null)
            return null;
        List<Map<String,String>> response=new ArrayList<>();
        List<String> joinedStudent=course.getJoinedStudents();
        for(String studentId :joinedStudent){
            User student=userServiceRepo.findById(studentId).orElse(null);
            if(student==null)
                continue;
            Progress progress=progressServiceRepo.findByCourseIdAndStudentId(course.getId(),student.getId());
            if(progress==null)
                continue;
            Map<String,String> data=new HashMap<>();
            data.put("id",student.getId());
            data.put("name",student.getName());
            data.put("progress",""+progress.getCompletionPercentage());
            response.add(data);
        }
        return response;
    }




    private void recalculateCompletionPercentage(Progress progress) {
        int completedCount = 0;
        int totalLessons = progress.getLessonProgressList().size();

        // Count completed lessons
        for (LessonProgress lessonProgress : progress.getLessonProgressList()) {
            if (lessonProgress.isCompleted()) {
                completedCount++;
            }
        }

        // Calculate the new completion percentage
        double completionPercentage = (totalLessons > 0) ? ((double) completedCount / totalLessons) * 100 : 0.0;
        if(completionPercentage>=100){
            progress.setCourseCompleted(true);
        }
        progress.setCompletionPercentage(completionPercentage);
    }
    @Transactional
    public Map<String,Double> getEnrolledCoursesProgress(String userName) {
        User user=userServiceRepo.findByUserName(userName);
        Map<String,Double> progresses=new HashMap<>();
        for(String courseId:user.getEnrolledCourses()){
            Progress progress=progressServiceRepo.findByCourseIdAndStudentId(courseId,user.getId());
            if(progress!=null&&user.getProgressIdList().contains(progress.getId())) {
                progresses.put(courseId, progress.getCompletionPercentage());
            }
        }
        return progresses;
    }
    @Transactional
    public Map<String,Object> getStudentsAllDetails(String courseId,String studentId){
        Map<String, Object> response=new HashMap<>();
        User student=userServiceRepo.findById(studentId).orElse(null);
        Course course=courseServiceRepo.findById(courseId).orElse(null);
        Progress progress=progressServiceRepo.findByCourseIdAndStudentId(courseId,studentId);
        if(student==null||course==null||progress==null)
            return null;

        List<MockTestResult> mockTestResult=mockTestResultRepo.findByUserIdAndCourseId(studentId,courseId);



        response.put("studentId",student.getId());
        response.put("studentName",student.getName());
        response.put("mobile",student.getMobileNo());
        response.put("email",student.getEmail());
        response.put("courseId",course.getId());
        response.put("courseTitle",course.getTitle());
        response.put("totalLessons",course.getLessons().size());
        response.put("completed",progress.getCompletionPercentage());
        response.put("enrolledDate",progress.getEnrolledDate());
        response.put("allMockResults",mockTestResult);
        response.put("studentId",student.getId());

        return response;
    }


    private boolean checkTitle(Course course) { return course.getTitle()==null||course.getTitle().isEmpty(); }
    private boolean checkDescription(Course course) { return course.getDescription()==null||course.getDescription().isEmpty(); }
    private boolean checkCategory(Course course) { return course.getCategory()==null||course.getCategory().isEmpty(); }



}
