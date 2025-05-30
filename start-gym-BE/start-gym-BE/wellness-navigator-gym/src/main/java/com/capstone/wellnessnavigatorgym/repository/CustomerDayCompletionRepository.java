package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CustomerDayCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerDayCompletionRepository extends JpaRepository<CustomerDayCompletion, Integer> {

    // Các phương thức tìm kiếm cơ bản
    Optional<CustomerDayCompletion> findByCustomer_CustomerIdAndCourse_CourseIdAndDay_DayId(Integer customerId, Integer courseId, Integer dayId);
    
    List<CustomerDayCompletion> findByCustomer_CustomerIdAndCourse_CourseId(Integer customerId, Integer courseId);
    
    @Query("SELECT c FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId AND c.course.courseId = :courseId AND c.day.dayId = :dayId")
    Optional<CustomerDayCompletion> findByCustomerAndCourseAndDay(
            @Param("customerId") Integer customerId, 
            @Param("courseId") Integer courseId, 
            @Param("dayId") Integer dayId);
    
    @Query("SELECT c FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId AND c.day.dayId = :dayId")
    Optional<CustomerDayCompletion> findByCustomerAndDay(
            @Param("customerId") Integer customerId,
            @Param("dayId") Integer dayId);
    
    @Query("SELECT COUNT(c) FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId AND c.course.courseId = :courseId AND c.isCompleted = true")
    Long countCompletedDaysByCustomerAndCourse(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    @Query("SELECT COUNT(c) FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId AND c.course.courseId = :courseId")
    Long countTotalDaysByCustomerAndCourse(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    @Query("SELECT CASE WHEN COUNT(c) = COUNT(CASE WHEN c.isCompleted = true THEN 1 ELSE null END) THEN true ELSE false END " +
           "FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId AND c.course.courseId = :courseId")
    Boolean isCourseCompletedByCustomer(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    // Phương thức tìm kiếm cho Dashboard
    @Query("SELECT c FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId")
    List<CustomerDayCompletion> findByCustomerId(@Param("customerId") Integer customerId);
    
    @Query("SELECT c FROM CustomerDayCompletion c WHERE c.customer.customerId = :customerId AND c.completionDate BETWEEN :startDate AND :endDate")
    List<CustomerDayCompletion> findByCustomerIdAndCompletedDateBetween(
            @Param("customerId") Integer customerId, 
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
} 