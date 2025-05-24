package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IVipPaymentRepository extends JpaRepository<VipPayment, Long> {
    
    Optional<VipPayment> findByVnpTxnRef(String vnpTxnRef);
    
    List<VipPayment> findByCustomer_CustomerIdOrderByCreatedDateDesc(Integer customerId);
    
    List<VipPayment> findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus paymentStatus);
    
    @Query("SELECT p FROM VipPayment p WHERE p.customer.customerId = :customerId AND p.course.courseId = :courseId")
    Optional<VipPayment> findByCustomerAndCourse(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    @Query("SELECT COUNT(p) FROM VipPayment p WHERE p.paymentStatus = 'SUCCESS' AND p.course.courseId = :courseId")
    Long countSuccessfulPaymentsByCourse(@Param("courseId") Integer courseId);
} 