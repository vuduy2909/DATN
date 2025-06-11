package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Lấy danh sách customer types theo thứ tự price giảm dần
     */
    @Query("SELECT ct FROM CustomerType ct ORDER BY ct.price DESC")
    List<CustomerType> findAllOrderByPriceDesc();
    
    /**
     * Lấy customer type theo tên
     */
    @Query("SELECT ct FROM CustomerType ct WHERE ct.customerTypeName = :typeName")
    CustomerType findByCustomerTypeName(@Param("typeName") String typeName);
    
    /**
     * Lấy ID cao nhất hiện tại để tạo sequential ID
     */
    @Query("SELECT MAX(ct.customerTypeId) FROM CustomerType ct")
    Integer findMaxCustomerTypeId();
    
    /**
     * Lấy danh sách customer types có ID lớn hơn ID bị xóa để cập nhật sequential
     */
    @Query("SELECT ct FROM CustomerType ct WHERE ct.customerTypeId > :deletedId ORDER BY ct.customerTypeId ASC")
    List<CustomerType> findAllWithIdGreaterThan(@Param("deletedId") Integer deletedId);
    
    /**
     * Cập nhật ID của customer type
     */
    @Modifying
    @Query("UPDATE CustomerType ct SET ct.customerTypeId = :newId WHERE ct.customerTypeId = :oldId")
    void updateCustomerTypeId(@Param("oldId") Integer oldId, @Param("newId") Integer newId);
}
