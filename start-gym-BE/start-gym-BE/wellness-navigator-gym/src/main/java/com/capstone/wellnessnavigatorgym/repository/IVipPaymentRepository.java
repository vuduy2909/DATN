package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IVipPaymentRepository extends JpaRepository<VipPayment, Long> {
    
    Optional<VipPayment> findByVnpTxnRef(String vnpTxnRef);
    
    List<VipPayment> findByCustomer_CustomerIdOrderByCreatedDateDesc(Integer customerId);
    
    List<VipPayment> findByCustomer_CustomerIdAndPaymentStatusOrderByCreatedDateDesc(Integer customerId, VipPayment.PaymentStatus paymentStatus);
    
    List<VipPayment> findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus paymentStatus);
    
    @Query("SELECT p FROM VipPayment p WHERE p.customer.customerId = :customerId AND p.course.courseId = :courseId")
    Optional<VipPayment> findByCustomerAndCourse(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    @Query("SELECT COUNT(p) FROM VipPayment p WHERE p.paymentStatus = 'SUCCESS' AND p.course.courseId = :courseId")
    Long countSuccessfulPaymentsByCourse(@Param("courseId") Integer courseId);

    // Dashboard-specific queries
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM vip_payment " +
            "WHERE payment_status = 'SUCCESS' " +
            "AND YEAR(created_date) = YEAR(CURDATE()) " +
            "AND MONTH(created_date) = MONTH(CURDATE())", nativeQuery = true)
    Double getTotalRevenueThisMonth();

    @Query(value = "SELECT COUNT(*) FROM vip_payment " +
            "WHERE payment_status = 'SUCCESS' " +
            "AND YEAR(created_date) = YEAR(CURDATE()) " +
            "AND MONTH(created_date) = MONTH(CURDATE())", nativeQuery = true)
    Long countSuccessfulOrdersThisMonth();

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM vip_payment " +
            "WHERE payment_status = 'SUCCESS'", nativeQuery = true)
    Double getTotalRevenue();

    // New queries for payment status management
    
    @Query("SELECT p FROM VipPayment p WHERE p.paymentStatus = 'PENDING' AND p.createdDate < :expiryTime ORDER BY p.createdDate ASC")
    List<VipPayment> findExpiredPendingPayments(@Param("expiryTime") LocalDateTime expiryTime);
    
    @Query("SELECT COUNT(p) FROM VipPayment p WHERE p.paymentStatus = 'PENDING' AND p.createdDate < :expiryTime")
    Long countExpiredPendingPayments(@Param("expiryTime") LocalDateTime expiryTime);
    
    @Query("SELECT p FROM VipPayment p WHERE p.customer.customerId = :customerId AND p.paymentStatus = 'SUCCESS' ORDER BY p.createdDate DESC")
    List<VipPayment> findSuccessfulPaymentsByCustomer(@Param("customerId") Integer customerId);
    
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM VipPayment p WHERE p.customer.customerId = :customerId AND p.paymentStatus = 'SUCCESS'")
    BigDecimal getTotalPaidAmountByCustomer(@Param("customerId") Integer customerId);
} 