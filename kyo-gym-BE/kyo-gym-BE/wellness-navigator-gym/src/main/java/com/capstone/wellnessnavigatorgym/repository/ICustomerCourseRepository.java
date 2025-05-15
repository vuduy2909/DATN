package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ICustomerCourseRepository extends JpaRepository<CustomerCourse, Integer> {
    List<CustomerCourse> findByCustomer_CustomerId(Integer customerId);
}
