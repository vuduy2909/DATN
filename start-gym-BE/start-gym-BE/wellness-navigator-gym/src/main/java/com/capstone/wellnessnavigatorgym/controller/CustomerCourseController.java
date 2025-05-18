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
} 