package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerCourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerDayCompletionRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerCourseServiceImpl implements ICustomerCourseService {

    @Autowired
    private ICustomerCourseRepository customerCourseRepository;

    @Autowired
    private ICustomerRepository customerRepository;
    
    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICustomerDayCompletionRepository customerDayCompletionRepository;

    @Override
    public void save(CustomerCourse customerCourse) {
        customerCourseRepository.save(customerCourse);
    }

    @Override
    public void saveRecommendedCourses(Course course, Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null && course != null) {
            // Fetch a fresh Course entity from the repository instead of using the potentially detached one
            Course managedCourse = courseRepository.findById(course.getCourseId()).orElse(null);
            if (managedCourse != null) {
                CustomerCourse customerCourse = new CustomerCourse();
                customerCourse.setCourse(managedCourse);
                customerCourse.setCustomer(customer);
                customerCourse.setRecommendedStatus(true);
                customerCourseRepository.save(customerCourse);
            }
        }
    }
    
    @Override
    public void saveRecommendedCourseById(Integer courseId, Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        Optional<Course> course = courseRepository.findById(courseId);
        
        if (customer != null && course.isPresent()) {
            // Check if this course is already recommended for this customer
            List<CustomerCourse> existingCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
            boolean exists = existingCourses.stream()
                    .anyMatch(cc -> cc.getCourse().getCourseId().equals(courseId));
            
            if (!exists) {
                CustomerCourse customerCourse = new CustomerCourse();
                customerCourse.setCourse(course.get());
                customerCourse.setCustomer(customer);
                customerCourse.setRecommendedStatus(true);
                customerCourseRepository.save(customerCourse);
            }
        }
    }

    @Override
    public List<CustomerCourse> findByCustomerId(Integer customerId) {
        return customerCourseRepository.findByCustomer_CustomerId(customerId);
    }
    
    @Override
    public List<CustomerCourse> findRecommendedCoursesByUsername(String username) {
        Customer customer = customerRepository.findActiveCustomerByAccountUserName(username);
        if (customer == null) {
            return new ArrayList<>();
        }
        
        return customerCourseRepository.findByCustomer_CustomerIdAndRecommendedStatusIsTrue(customer.getCustomerId());
    }
    
    @Override
    public boolean isCustomerEnrolledInCourse(Integer customerId, Integer courseId) {
        List<CustomerCourse> existingCourses = customerCourseRepository.findByCustomer_CustomerId(customerId);
        return existingCourses.stream()
                .anyMatch(cc -> cc.getCourse().getCourseId().equals(courseId));
    }
    
    @Override
    public void addCourseForCustomer(Integer courseId, Integer customerId) {
        // Validate customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
        
        // Validate course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
        
        // Check if already enrolled
        if (isCustomerEnrolledInCourse(customerId, courseId)) {
            throw new IllegalArgumentException("Customer is already enrolled in this course");
        }
        
        // Create new customer course record
        CustomerCourse customerCourse = new CustomerCourse();
        customerCourse.setCustomer(customer);
        customerCourse.setCourse(course);
        customerCourse.setRecommendedStatus(false); // User self-enrolled, not recommended
        
        customerCourseRepository.save(customerCourse);
    }
    
    @Override
    public void deleteCourseFromCustomer(Integer courseId, Integer customerId) {
        // Validate customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + customerId));
        
        // Validate course exists
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with ID: " + courseId));
        
        // Check if customer is enrolled in this course
        if (!isCustomerEnrolledInCourse(customerId, courseId)) {
            throw new IllegalArgumentException("Customer is not enrolled in this course");
        }
        
        // IMPORTANT: Delete all day completion records first before deleting course enrollment
        customerDayCompletionRepository.deleteByCustomerAndCourse(customerId, courseId);
        
        // Delete the customer course record
        customerCourseRepository.deleteByCustomerIdAndCourseId(customerId, courseId);
    }
}
