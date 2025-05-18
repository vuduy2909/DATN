package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ICustomerCourseRepository extends JpaRepository<CustomerCourse, Integer> {
    List<CustomerCourse> findByCustomer_CustomerId(Integer customerId);
    
    @Query("SELECT cc FROM CustomerCourse cc WHERE cc.customer.customerId = :customerId AND cc.recommendedStatus = true")
    List<CustomerCourse> findByCustomer_CustomerIdAndRecommendedStatusIsTrue(@Param("customerId") Integer customerId);
}
