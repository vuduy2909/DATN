package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public interface IPaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findPaymentByTnxRef(String tnxRef);

    @Modifying
    @Query(value = "DELETE FROM `payment_cart_details` WHERE payment_id = :id", nativeQuery = true)
    void deleteDetailsByPaymentId(@Param("id") Integer id);
}
