package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.CourseRating;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.service.ICourseRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/course-rating")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseRatingController {

    @Autowired
    private ICourseRatingService courseRatingService;
    
    @Autowired
    private ICustomerRepository customerRepository;

    /**
     * Submit rating for a course (only allowed after course completion)
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> submitCourseRating(
            @RequestParam Integer courseId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String reviewText,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Customer not found"));
            }

            CourseRating courseRating = courseRatingService.submitCourseRating(
                customer.getCustomerId(), courseId, rating, reviewText);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course rating submitted successfully");
            response.put("rating", courseRating);
            
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error submitting rating: " + e.getMessage()));
        }
    }

    /**
     * Update existing rating for a course
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCourseRating(
            @RequestParam Integer courseId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String reviewText,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Customer not found"));
            }

            CourseRating courseRating = courseRatingService.updateCourseRating(
                customer.getCustomerId(), courseId, rating, reviewText);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course rating updated successfully");
            response.put("rating", courseRating);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error updating rating: " + e.getMessage()));
        }
    }

    /**
     * Get all ratings for a specific course
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseRatings(@PathVariable Integer courseId) {
        try {
            List<CourseRating> ratings = courseRatingService.getCourseRatings(courseId);
            Map<String, Object> statistics = courseRatingService.getCourseRatingStatistics(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("ratings", ratings);
            response.put("statistics", statistics);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error getting course ratings: " + e.getMessage()));
        }
    }

    /**
     * Get customer's rating for a specific course
     */
    @GetMapping("/my-rating/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyRatingForCourse(@PathVariable Integer courseId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Customer not found"));
            }

            CourseRating rating = courseRatingService.getCustomerRatingForCourse(customer.getCustomerId(), courseId);
            Boolean canRate = courseRatingService.canCustomerRateCourse(customer.getCustomerId(), courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("rating", rating);
            response.put("canRate", canRate);
            response.put("hasRated", rating != null);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error getting customer rating: " + e.getMessage()));
        }
    }

    /**
     * Check if customer can rate a specific course
     */
    @GetMapping("/can-rate/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> canRateCourse(@PathVariable Integer courseId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("Customer not found"));
            }

            Boolean canRate = courseRatingService.canCustomerRateCourse(customer.getCustomerId(), courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("canRate", canRate);
            response.put("customerId", customer.getCustomerId());
            response.put("courseId", courseId);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error checking rating permission: " + e.getMessage()));
        }
    }

    /**
     * Get top rated courses
     */
    @GetMapping("/top-rated")
    public ResponseEntity<?> getTopRatedCourses(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Map<String, Object>> topRated = courseRatingService.getTopRatedCourses(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topRatedCourses", topRated);
            response.put("limit", limit);
            
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error getting top rated courses: " + e.getMessage()));
        }
    }

    /**
     * Get course rating statistics (public endpoint)
     */
    @GetMapping("/statistics/{courseId}")
    public ResponseEntity<?> getCourseRatingStatistics(@PathVariable Integer courseId) {
        try {
            Map<String, Object> statistics = courseRatingService.getCourseRatingStatistics(courseId);
            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error getting course statistics: " + e.getMessage()));
        }
    }
} 