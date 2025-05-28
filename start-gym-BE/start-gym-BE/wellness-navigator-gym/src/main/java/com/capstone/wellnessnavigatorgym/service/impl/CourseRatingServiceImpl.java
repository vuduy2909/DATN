package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CourseRating;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.repository.ICourseRatingRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerDayCompletionRepository;
import com.capstone.wellnessnavigatorgym.service.ICourseRatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseRatingServiceImpl implements ICourseRatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(CourseRatingServiceImpl.class);
    
    @Autowired
    private ICourseRatingRepository courseRatingRepository;
    
    @Autowired
    private ICourseRepository courseRepository;
    
    @Autowired
    private ICustomerRepository customerRepository;
    
    @Autowired
    private ICustomerDayCompletionRepository customerDayCompletionRepository;
    
    @Override
    public CourseRating submitCourseRating(Integer customerId, Integer courseId, Integer rating, String reviewText) {
        try {
            // Validate input
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            // Check if customer already rated this course
            Optional<CourseRating> existingRating = courseRatingRepository.findByCustomerAndCourse(customerId, courseId);
            if (existingRating.isPresent()) {
                throw new IllegalArgumentException("Customer has already rated this course");
            }
            
            // Check if customer completed the course
            Boolean isCourseCompleted = customerDayCompletionRepository.isCourseCompletedByCustomer(customerId, courseId);
            if (!isCourseCompleted) {
                throw new IllegalArgumentException("Customer must complete the course before rating");
            }
            
            // Get customer and course
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
            
            // Create new rating
            CourseRating courseRating = new CourseRating();
            courseRating.setCustomer(customer);
            courseRating.setCourse(course);
            courseRating.setRating(rating);
            courseRating.setReviewText(reviewText);
            courseRating.setIsCourseCompleted(true);
            courseRating.setCompletionDate(LocalDateTime.now());
            
            // Save rating
            CourseRating savedRating = courseRatingRepository.save(courseRating);
            
            // Update course aggregate rating
            updateCourseAggregateRating(courseId);
            
            logger.info("Customer {} submitted rating {} for course {}", customerId, rating, courseId);
            return savedRating;
            
        } catch (Exception e) {
            logger.error("Error submitting course rating", e);
            throw e;
        }
    }
    
    @Override
    public CourseRating updateCourseRating(Integer customerId, Integer courseId, Integer rating, String reviewText) {
        try {
            // Validate input
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            // Find existing rating
            CourseRating existingRating = courseRatingRepository.findByCustomerAndCourse(customerId, courseId)
                .orElseThrow(() -> new RuntimeException("No existing rating found to update"));
            
            // Update rating
            existingRating.setRating(rating);
            existingRating.setReviewText(reviewText);
            existingRating.setUpdatedDate(LocalDateTime.now());
            
            // Save updated rating
            CourseRating updatedRating = courseRatingRepository.save(existingRating);
            
            // Update course aggregate rating
            updateCourseAggregateRating(courseId);
            
            logger.info("Customer {} updated rating to {} for course {}", customerId, rating, courseId);
            return updatedRating;
            
        } catch (Exception e) {
            logger.error("Error updating course rating", e);
            throw e;
        }
    }
    
    @Override
    public List<CourseRating> getCourseRatings(Integer courseId) {
        return courseRatingRepository.findByCourseWithCustomerInfo(courseId);
    }
    
    @Override
    public CourseRating getCustomerRatingForCourse(Integer customerId, Integer courseId) {
        return courseRatingRepository.findByCustomerAndCourse(customerId, courseId)
            .orElse(null);
    }
    
    @Override
    public Boolean canCustomerRateCourse(Integer customerId, Integer courseId) {
        try {
            // Check if course is completed
            Boolean isCourseCompleted = customerDayCompletionRepository.isCourseCompletedByCustomer(customerId, courseId);
            if (!isCourseCompleted) {
                return false;
            }
            
            // Check if already rated
            Optional<CourseRating> existingRating = courseRatingRepository.findByCustomerAndCourse(customerId, courseId);
            return !existingRating.isPresent();
            
        } catch (Exception e) {
            logger.error("Error checking if customer can rate course", e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getCourseRatingStatistics(Integer courseId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get basic stats
            Double averageRating = courseRatingRepository.getAverageRatingByCourse(courseId);
            Long totalRatings = courseRatingRepository.getTotalRatingsByCourse(courseId);
            
            stats.put("averageRating", averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0);
            stats.put("totalRatings", totalRatings != null ? totalRatings : 0L);
            
            // Get rating distribution
            Map<String, Long> ratingDistribution = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                Long count = courseRatingRepository.countByCourseAndRating(courseId, i);
                ratingDistribution.put("rating" + i + "Count", count != null ? count : 0L);
            }
            stats.put("ratingDistribution", ratingDistribution);
            
            // Get recent reviews
            List<CourseRating> recentRatings = courseRatingRepository.findByCourseOrderByCreatedDateDesc(courseId)
                .stream()
                .limit(5)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> recentReviews = recentRatings.stream()
                .map(rating -> {
                    Map<String, Object> review = new HashMap<>();
                    review.put("customerName", rating.getCustomer().getCustomerName());
                    review.put("rating", rating.getRating());
                    review.put("reviewText", rating.getReviewText());
                    review.put("createdDate", rating.getCreatedDate());
                    return review;
                })
                .collect(Collectors.toList());
            
            stats.put("recentReviews", recentReviews);
            
            return stats;
            
        } catch (Exception e) {
            logger.error("Error getting course rating statistics", e);
            throw e;
        }
    }
    
    @Override
    public void updateCourseAggregateRating(Integer courseId) {
        try {
            Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
            
            // Calculate new average rating
            Double averageRating = courseRatingRepository.getAverageRatingByCourse(courseId);
            
            // Update course entity - only average rating
            course.setAverageRating(averageRating != null ? Math.round(averageRating * 100.0) / 100.0 : 0.0);
            
            courseRepository.save(course);
            
            logger.info("Updated aggregate rating for course {}: avg={}", 
                courseId, course.getAverageRating());
            
        } catch (Exception e) {
            logger.error("Error updating course aggregate rating", e);
            throw e;
        }
    }
    
    @Override
    public List<Map<String, Object>> getTopRatedCourses(Integer limit) {
        try {
            List<Object[]> topRated = courseRatingRepository.findTopRatedCourses(5L); // Minimum 5 ratings
            
            return topRated.stream()
                .limit(limit != null ? limit : 10)
                .map(result -> {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("courseId", result[0]);
                    courseInfo.put("averageRating", Math.round(((Double) result[1]) * 100.0) / 100.0);
                    courseInfo.put("totalRatings", result[2]);
                    
                    // Get course details
                    Integer courseId = (Integer) result[0];
                    Course course = courseRepository.findById(courseId).orElse(null);
                    if (course != null) {
                        courseInfo.put("courseName", course.getCourseName());
                        courseInfo.put("description", course.getDescription());
                        courseInfo.put("image", course.getImage());
                    }
                    
                    return courseInfo;
                })
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting top rated courses", e);
            throw e;
        }
    }
} 