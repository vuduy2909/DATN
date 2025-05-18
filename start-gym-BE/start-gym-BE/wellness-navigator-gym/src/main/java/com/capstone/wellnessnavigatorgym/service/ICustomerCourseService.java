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
}
