package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.UserDataAi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface IUserDataAiRepository extends JpaRepository<UserDataAi, Integer> {
    List<UserDataAi> findByCustomer_CustomerId(Integer customerId);
}
