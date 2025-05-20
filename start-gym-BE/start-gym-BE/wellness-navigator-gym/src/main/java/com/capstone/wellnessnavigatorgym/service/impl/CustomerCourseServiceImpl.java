package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerCourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
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
        Customer customer = customerRepository.findByAccountUserName(username);
        if (customer == null) {
            return new ArrayList<>();
        }
        
        return customerCourseRepository.findByCustomer_CustomerIdAndRecommendedStatusIsTrue(customer.getCustomerId());
    }
}
