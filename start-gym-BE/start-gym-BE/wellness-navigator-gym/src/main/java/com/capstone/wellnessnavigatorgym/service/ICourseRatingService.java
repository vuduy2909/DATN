package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.CourseRating;

import java.util.List;
import java.util.Map;

public interface ICourseRatingService {
    
    // Submit rating for a course (only allowed if customer completed the course)
    CourseRating submitCourseRating(Integer customerId, Integer courseId, Integer rating, String reviewText);
    
    // Update existing rating
    CourseRating updateCourseRating(Integer customerId, Integer courseId, Integer rating, String reviewText);
    
    // Get all ratings for a course
    List<CourseRating> getCourseRatings(Integer courseId);
    
    // Get customer's rating for a course
    CourseRating getCustomerRatingForCourse(Integer customerId, Integer courseId);
    
    // Check if customer can rate this course (completed but not rated yet)
    Boolean canCustomerRateCourse(Integer customerId, Integer courseId);
    
    // Get course rating statistics
    Map<String, Object> getCourseRatingStatistics(Integer courseId);
    
    // Update course aggregate rating fields after new rating is submitted
    void updateCourseAggregateRating(Integer courseId);
    
    // Get top rated courses
    List<Map<String, Object>> getTopRatedCourses(Integer limit);
} 