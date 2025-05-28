package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;

import java.util.List;

public interface ICustomerCourseService {

    void save(CustomerCourse customerCourse);

    void saveRecommendedCourses(Course courses, Integer customerId);
    
    void saveRecommendedCourseById(Integer courseId, Integer customerId);

    List<CustomerCourse> findByCustomerId(Integer customerId);
    
    List<CustomerCourse> findRecommendedCoursesByUsername(String username);
    
    // New methods for customer self-enrollment
    boolean isCustomerEnrolledInCourse(Integer customerId, Integer courseId);
    
    void addCourseForCustomer(Integer courseId, Integer customerId);
    
    // Method to delete course from customer
    void deleteCourseFromCustomer(Integer courseId, Integer customerId);
}
