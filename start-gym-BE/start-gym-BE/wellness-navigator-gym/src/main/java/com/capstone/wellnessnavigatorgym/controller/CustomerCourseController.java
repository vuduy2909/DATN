package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.course.SimpleCourseDto;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customer-course")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerCourseController {

    @Autowired
    private ICustomerCourseService customerCourseService;
    
    @Autowired
    private ICustomerRepository customerRepository;

    @GetMapping("/recommended")
    public ResponseEntity<List<SimpleCourseDto>> getRecommendedCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<CustomerCourse> recommendedCourses = customerCourseService.findRecommendedCoursesByUsername(username);
        
        if (recommendedCourses == null || recommendedCourses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        
        List<SimpleCourseDto> simpleCourses = recommendedCourses.stream()
                .map(SimpleCourseDto::fromCustomerCourse)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        
        if (simpleCourses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        
        return new ResponseEntity<>(simpleCourses, HttpStatus.OK);
    }
    
    @GetMapping("/debug")
    public ResponseEntity<?> debugRecommendations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("username", username);
        
        try {
            // Get customer info
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer != null) {
                debugInfo.put("customerId", customer.getCustomerId());
                debugInfo.put("customerName", customer.getCustomerName());
                
                // Get customer courses
                List<CustomerCourse> allCourses = customerCourseService.findByCustomerId(customer.getCustomerId());
                debugInfo.put("totalCourses", allCourses.size());
                
                List<CustomerCourse> recommendedCourses = allCourses.stream()
                        .filter(cc -> cc.getRecommendedStatus() != null && cc.getRecommendedStatus())
                        .collect(Collectors.toList());
                debugInfo.put("recommendedCoursesCount", recommendedCourses.size());
                
                // Course details
                List<Map<String, Object>> courseDetails = recommendedCourses.stream()
                        .map(cc -> {
                            Map<String, Object> details = new HashMap<>();
                            if (cc.getCourse() != null) {
                                details.put("courseId", cc.getCourse().getCourseId());
                                details.put("courseName", cc.getCourse().getCourseName());
                                details.put("description", cc.getCourse().getDescription());
                                details.put("duration", cc.getCourse().getDuration());
                                details.put("recommendedStatus", cc.getRecommendedStatus());
                            }
                            return details;
                        })
                        .collect(Collectors.toList());
                debugInfo.put("courseDetails", courseDetails);
            } else {
                debugInfo.put("error", "Customer not found for username: " + username);
            }
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/add-recommended/{courseId}")
    public ResponseEntity<?> addRecommendedCourse(@PathVariable Integer courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            // Get customer info
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found for username: " + username);
            }
            
            // Find course ID
            customerCourseService.saveRecommendedCourseById(courseId, customer.getCustomerId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully added recommended course ID " + courseId + " for customer " + customer.getCustomerName());
            response.put("courseId", courseId);
            response.put("customerId", customer.getCustomerId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding recommended course: " + e.getMessage());
        }
    }

    @PostMapping("/join/{courseId}")
    public ResponseEntity<?> joinCourse(@PathVariable Integer courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            // Get customer info
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("message", "Please login to join courses");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Check if customer already joined this course
            boolean alreadyJoined = customerCourseService.isCustomerEnrolledInCourse(customer.getCustomerId(), courseId);
            if (alreadyJoined) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Already enrolled");
                response.put("message", "You have already joined this course");
                response.put("courseId", courseId);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Add course for customer (not recommended, user choice)
            customerCourseService.addCourseForCustomer(courseId, customer.getCustomerId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully joined the course!");
            response.put("courseId", courseId);
            response.put("customerId", customer.getCustomerId());
            response.put("customerName", customer.getCustomerName());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid course");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "Error joining course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Customer not found for username: " + username);
            }
            
            List<CustomerCourse> allCourses = customerCourseService.findByCustomerId(customer.getCustomerId());
            
            List<Map<String, Object>> coursesDetails = allCourses.stream()
                .map(cc -> {
                    Map<String, Object> details = new HashMap<>();
                    if (cc.getCourse() != null) {
                        details.put("courseId", cc.getCourse().getCourseId());
                        details.put("courseName", cc.getCourse().getCourseName());
                        details.put("description", cc.getCourse().getDescription());
                        details.put("duration", cc.getCourse().getDuration());
                        details.put("image", cc.getCourse().getImage());
                        details.put("isRecommended", cc.getRecommendedStatus() != null && cc.getRecommendedStatus());
                        details.put("joinType", cc.getRecommendedStatus() != null && cc.getRecommendedStatus() ? "Recommended" : "Self-enrolled");
                    }
                    return details;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerName", customer.getCustomerName());
            response.put("totalCourses", coursesDetails.size());
            response.put("courses", coursesDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting courses: " + e.getMessage());
        }
    }
} 