package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CustomerDayCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ICustomerDayCompletionRepository extends JpaRepository<CustomerDayCompletion, Integer> {
    
    /**
     * Tìm completion record của customer cho một day cụ thể trong course
     */
    @Query("SELECT cdc FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId AND cdc.day.dayId = :dayId")
    Optional<CustomerDayCompletion> findByCustomerAndCourseAndDay(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId,
            @Param("dayId") Integer dayId);
    
    /**
     * Lấy tất cả completion records của customer cho một course
     */
    @Query("SELECT cdc FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId ORDER BY cdc.day.dayId")
    List<CustomerDayCompletion> findByCustomerAndCourse(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId);
    
    /**
     * Lấy tất cả completion records của customer
     */
    @Query("SELECT cdc FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "ORDER BY cdc.course.courseId, cdc.day.dayId")
    List<CustomerDayCompletion> findByCustomer(@Param("customerId") Integer customerId);
    
    /**
     * Đếm số buổi đã hoàn thành của customer trong một course
     */
    @Query("SELECT COUNT(cdc) FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId AND cdc.isCompleted = true")
    Long countCompletedDaysByCustomerAndCourse(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId);
    
    /**
     * Đếm tổng số buổi của customer trong một course
     */
    @Query("SELECT COUNT(cdc) FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId")
    Long countTotalDaysByCustomerAndCourse(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId);
    
    /**
     * Kiểm tra customer đã hoàn thành course chưa (tất cả days đều completed)
     */
    @Query("SELECT CASE WHEN COUNT(cdc) = COUNT(CASE WHEN cdc.isCompleted = true THEN 1 END) " +
           "THEN true ELSE false END " +
           "FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId")
    Boolean isCourseCompletedByCustomer(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId);
    
    /**
     * Lấy danh sách days đã hoàn thành của customer trong course
     */
    @Query("SELECT cdc FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId AND cdc.isCompleted = true " +
           "ORDER BY cdc.completionDate DESC")
    List<CustomerDayCompletion> findCompletedDaysByCustomerAndCourse(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId);
    
    /**
     * Cập nhật trạng thái completion cho một day
     */
    @Modifying
    @Query("UPDATE CustomerDayCompletion cdc SET cdc.isCompleted = :isCompleted, " +
           "cdc.completionDate = CASE WHEN :isCompleted = true THEN CURRENT_TIMESTAMP ELSE NULL END, " +
           "cdc.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE cdc.customer.customerId = :customerId AND cdc.course.courseId = :courseId " +
           "AND cdc.day.dayId = :dayId")
    int updateCompletionStatus(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId,
            @Param("dayId") Integer dayId,
            @Param("isCompleted") Boolean isCompleted);
    
    /**
     * Xóa tất cả completion records của customer cho một course
     */
    @Modifying
    @Query("DELETE FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId")
    int deleteByCustomerAndCourse(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId);
    
    /**
     * Kiểm tra xem customer đã có completion record cho day chưa
     */
    @Query("SELECT CASE WHEN COUNT(cdc) > 0 THEN true ELSE false END " +
           "FROM CustomerDayCompletion cdc WHERE cdc.customer.customerId = :customerId " +
           "AND cdc.course.courseId = :courseId AND cdc.day.dayId = :dayId")
    Boolean existsByCustomerAndCourseAndDay(
            @Param("customerId") Integer customerId,
            @Param("courseId") Integer courseId,
            @Param("dayId") Integer dayId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CustomerDayCompletion c " +
           "WHERE c.customer.customerId = :customerId " +
           "AND CAST(c.completionDate AS date) = CAST(:today AS date) " +
           "AND c.isCompleted = true")
    boolean hasAttendanceForDate(@Param("customerId") Integer customerId, 
                                 @Param("today") LocalDateTime today);

    @Query("SELECT MAX(c.day.dayId) FROM CustomerDayCompletion c " +
           "WHERE c.customer.customerId = :customerId " +
           "AND c.course.courseId = :courseId " +
           "AND c.isCompleted = true")
    Integer findLastCompletedDayNumber(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);

    @Query("SELECT c FROM CustomerDayCompletion c " +
           "JOIN FETCH c.day " +
           "WHERE c.customer.customerId = :customerId " +
           "AND c.completionDate >= :startDate " +
           "AND c.completionDate < :endDate " +
           "AND c.isCompleted = true " +
           "ORDER BY c.completionDate ASC")
    List<CustomerDayCompletion> findCompletedDaysByCustomerAndDateRange(
            @Param("customerId") Integer customerId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số buổi tập hoàn thành trong tuần hiện tại (từ thứ 2 đến chủ nhật)
     */
    @Query("SELECT COUNT(cdc) FROM CustomerDayCompletion cdc " +
           "WHERE cdc.customer.account.userName = :username " +
           "AND cdc.isCompleted = true " +
           "AND cdc.completionDate >= :weekStart " +
           "AND cdc.completionDate < :weekEnd")
    Integer countCompletedSessionsInCurrentWeek(
            @Param("username") String username,
            @Param("weekStart") LocalDateTime weekStart, 
            @Param("weekEnd") LocalDateTime weekEnd);
            
    /**
     * Đếm số buổi tập mục tiêu trong tuần (dựa trên tổng số khóa học đang tham gia)
     */
    @Query("SELECT COUNT(DISTINCT cd.course.courseId) FROM CustomerDayCompletion cd " +
           "WHERE cd.customer.account.userName = :username")
    Integer countActiveCoursesForCustomer(@Param("username") String username);
} 