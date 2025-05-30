package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.UserDataAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDataAiRepository extends JpaRepository<UserDataAi, Integer> {

    // Tìm dữ liệu AI mới nhất cho một người dùng
    Optional<UserDataAi> findTopByCustomer_CustomerIdOrderByCreatedDateDesc(Integer customerId);
    
    // Kiểm tra xem người dùng đã có dữ liệu AI chưa
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM UserDataAi u WHERE u.customer.customerId = :customerId")
    Boolean existsByCustomerId(@Param("customerId") Integer customerId);
    
    // Tìm dữ liệu AI hiệu quả (effective = true) mới nhất cho một người dùng
    @Query("SELECT u FROM UserDataAi u WHERE u.customer.customerId = :customerId AND u.effective = true ORDER BY u.createdDate DESC")
    Optional<UserDataAi> findLatestEffectiveDataByCustomerId(@Param("customerId") Integer customerId);
} 