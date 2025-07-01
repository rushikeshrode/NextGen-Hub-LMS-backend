package com.nextgenhub.lms.controllers;

import com.nextgenhub.lms.entities.MockTest;
import com.nextgenhub.lms.entities.MockTestResult;
import com.nextgenhub.lms.services.MockTestService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("mock")
public class MockTestController {

    @Autowired
    private MockTestService mockTestService;

    private final String message = "message";
    private final String error = "error";

    @PostMapping("/add/{courseId}")
    public ResponseEntity<?> addMockTest(@PathVariable String courseId, @RequestBody MockTest mockTest) {
        Map<String, Object> response = new HashMap<>();
        try {
            MockTest addedTest = mockTestService.addMockTestToCourse(courseId, mockTest);
            if (addedTest != null) {
                response.put("mock", addedTest);
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }catch (Exception e){
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint to get all mock tests for a specific course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Map<String, Object>> getMockTestsForCourse(@PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<MockTest> mockTests = mockTestService.getMockTestsForCourse(courseId);
//            if (mockTests == null || mockTests.isEmpty()) {
//                response.put("mock-list", mockTests);
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
            response.put("mocklist", mockTests);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    // Endpoint to delete a mock test from a course
    @DeleteMapping("/delete/{mockTestId}/{courseId}")

    public ResponseEntity<Map<String, Object>> deleteMockTest(@PathVariable String mockTestId, @PathVariable String courseId) {
        Map<String, Object> response = new HashMap<>();
        boolean isDeleted = mockTestService.deleteMockTest(mockTestId, courseId);
        if (isDeleted) {
            response.put(message, "Mock test deleted successfully.");
            return ResponseEntity.ok(response);
        }
        response.put(error, "Mock test or course not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping("/submit-result/{mockTestId}")
    public ResponseEntity<Map<String, Object>> submitMockTestResult(
                                                               @PathVariable String mockTestId,
                                                               @RequestBody List<String> userAnswers) {
        Map<String, Object> response = new HashMap<>();
        try {
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            String userName=authentication.getName();
            MockTestResult result = mockTestService.saveMockTestResult(userName, mockTestId, userAnswers);

            if (result != null) {
                response.put("result", result);
                return ResponseEntity.ok(response);  // Return the saved result
            }
            response.put("error", "Failed to Submit Test.");
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);  // Return error if something went wrong
        }catch (Exception e){
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response,HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("all-result")
    public ResponseEntity<?> getAllResults() {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String userName=authentication.getName();
        Map<String, Object> response = new HashMap<>();
        try {
            List<MockTestResult> results = mockTestService.getResultOfAllTest(userName);

            if (results == null || results.isEmpty())
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            response.put("results", results);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }catch (Exception e){
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("result")

    public ResponseEntity<?> getResult(@PathVariable String userId, @PathVariable String testId) {
        Map<String, Object> response = new HashMap<>();
        try {
            MockTestResult testResult = mockTestService.getResultOf(userId, testId);
            if (testResult == null)
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            response.put("mock", testResult);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        catch (Exception e) {
            response.put(error, e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

}
