package com.nextgenhub.lms.services;


import com.nextgenhub.lms.entities.Course;
import com.nextgenhub.lms.entities.MockTest;
import com.nextgenhub.lms.entities.MockTestResult;
import com.nextgenhub.lms.entities.User;
import com.nextgenhub.lms.repositories.MockTestResultRepo;
import com.nextgenhub.lms.repositories.MockTestServiceRepo;
import com.nextgenhub.lms.repositories.UserServiceRepo;
//import jdk.incubator.vector.VectorOperators;
//import org.bson.types.String;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.crypto.Data;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
@Component
public class MockTestService {

    @Autowired
    private MockTestServiceRepo mockTestRepository;

    @Autowired
    private CourseService courseService;
    @Autowired
    private UserServiceRepo userServiceRepo;
    @Autowired
    private MockTestResultRepo mockTestResultRepo;

    // Method to create and add a mock test to a course
    public MockTest addMockTestToCourse(String courseId, MockTest mockTest) {
        // Get the course
        Course course = courseService.findCourseById(courseId);
        if (course != null) {
            // Add the mock test to the course

            mockTest.setCourseId(courseId);
            mockTest.setId(""+System.currentTimeMillis());
            Date date = new Date(); // Get current date
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = formatter.format(date);
            mockTest.setCreatedAt(formattedDate);
            // Save the course and mock test
            course.getMockTests().add(mockTest);
            courseService.saveCourse(course);
            return mockTestRepository.save(mockTest);
        }
        return null;
    }

    @Transactional
    public boolean deleteMockTest(String mockTestId, String courseId) {
        // First, delete the mock test
        mockTestRepository.deleteById(mockTestId);

        // Retrieve the course
        Course course = courseService.findCourseById(courseId);
        if (course != null) {
            // Remove the mock test from the course's mockTests list
            course.getMockTests().removeIf(mockTest -> mockTest.getId().equals(mockTestId));
            // Save the updated course
            courseService.saveCourse(course);
            return true;
        }
        return false;
    }



    // Get all mock tests for a course
    public List<MockTest> getMockTestsForCourse(String courseId) {
        return mockTestRepository.findByCourseId(courseId);
    }

    public MockTestResult saveMockTestResult(String userName, String mockTestId, List<String> userAnswers) {
        // Retrieve the mock test and user
        MockTest mockTest = mockTestRepository.findById(mockTestId).orElse(null);
        User user = userServiceRepo.findByUserName(userName);

        if (mockTest == null || user == null) {
            return null;  // Return null if mock test or user not found
        }

        // Calculate score (for simplicity, assume each correct answer gives 1 point)
        double score = calculateScore(mockTest, userAnswers);

        // Determine if the user passed the mock test (for simplicity, assume 50% pass mark)
        boolean isPassed = score >= (float)(mockTest.getQuestions().size() / 2);

        // Create a new MockTestResult entity
        MockTestResult prevResult=mockTestResultRepo.findByUserIdAndMockTestId(user.getId(),mockTestId);
        if(prevResult!=null){
            mockTestResultRepo.deleteById(prevResult.getId());
        }
        MockTestResult result = new MockTestResult();
        result.setUserId(user.getId());
        result.setCourseId(mockTest.getCourseId());
        Date date=new Date();
        SimpleDateFormat format=new SimpleDateFormat("dd/MM/yyyy");
        String formatedDate=format.format(date);
        result.setSubmitedDate(formatedDate);
        result.setMockTestId(mockTest.getId());
        result.setUserAnswers(userAnswers);
        result.setScore(score);
        result.setPassed(isPassed);

        // Save the result
        return mockTestResultRepo.save(result);
    }

    // Method to calculate the score based on user answers (this is just an example, adjust as necessary)
    private double calculateScore(MockTest mockTest, List<String> userAnswers) {
        double score = 0.0;
        for (int i = 0; i < mockTest.getQuestions().size(); i++) {
            if (mockTest.getQuestions().get(i).getCorrectAnswer().equals(userAnswers.get(i))) {
                score++;
            }
        }
        return score;
    }
    public void deleteMockTestResults(String studentId, String courseId) {
        // Fetch all mock test results for this student and course
        List<MockTestResult> mockTestResults = mockTestResultRepo.findByUserIdAndCourseId(studentId, courseId);

        // Delete the mock test results
        mockTestResultRepo.deleteAll(mockTestResults);
    }
    @Transactional
    public List<MockTestResult> getResultOfAllTest(String userName ){
        User user=userServiceRepo.findByUserName(userName);
        return mockTestResultRepo.findByUserId(user.getId());
    }
    public MockTestResult getResultOf(String userId,String testId)
    {
        return mockTestResultRepo.findByUserIdAndMockTestId(userId,testId);
    }

}

