package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ICustomerTypeRepository extends JpaRepository<CustomerType, Integer> {
    
    /**
     * Lấy danh sách customer types theo thứ tự price tăng dần
     * Để xác định threshold cho việc phân loại customer
     */
    @Query("SELECT ct FROM CustomerType ct ORDER BY ct.price ASC")
    List<CustomerType> findAllOrderByPriceAsc();
    
    /**
     * Lấy customer type theo tên
     */
    @Query("SELECT ct FROM CustomerType ct WHERE ct.customerTypeName = :typeName")
    CustomerType findByCustomerTypeName(String typeName);
}
