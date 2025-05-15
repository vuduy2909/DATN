package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import com.capstone.wellnessnavigatorgym.repository.ICustomerCourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerCourseServiceImpl implements ICustomerCourseService {

    @Autowired
    private ICustomerCourseRepository customerCourseRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Override
    public void save(CustomerCourse customerCourse) {
        customerCourseRepository.save(customerCourse);
    }

    @Override
    public void saveRecommendedCourses(Course course, Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer != null && course != null) {
            CustomerCourse customerCourse = new CustomerCourse();
            customerCourse.setCourse(course);
            customerCourse.setCustomer(customer);
            customerCourse.setRecommendedStatus(true);
            customerCourseRepository.save(customerCourse);
        }
    }
}
