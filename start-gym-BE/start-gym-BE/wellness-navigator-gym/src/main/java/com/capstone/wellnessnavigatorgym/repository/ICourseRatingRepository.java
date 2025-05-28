package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CourseRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ICourseRatingRepository extends JpaRepository<CourseRating, Integer> {
    
    // Tìm rating của customer cho course cụ thể
    @Query("SELECT cr FROM CourseRating cr WHERE cr.customer.customerId = :customerId AND cr.course.courseId = :courseId")
    Optional<CourseRating> findByCustomerAndCourse(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    // Lấy tất cả ratings của 1 course
    @Query("SELECT cr FROM CourseRating cr WHERE cr.course.courseId = :courseId ORDER BY cr.createdDate DESC")
    List<CourseRating> findByCourseOrderByCreatedDateDesc(@Param("courseId") Integer courseId);
    
    // Lấy tất cả ratings của 1 customer
    @Query("SELECT cr FROM CourseRating cr WHERE cr.customer.customerId = :customerId ORDER BY cr.createdDate DESC")
    List<CourseRating> findByCustomerOrderByCreatedDateDesc(@Param("customerId") Integer customerId);
    
    // Đếm số lượng rating theo từng mức (1-5 sao) cho course
    @Query("SELECT COUNT(cr) FROM CourseRating cr WHERE cr.course.courseId = :courseId AND cr.rating = :rating")
    Long countByCourseAndRating(@Param("courseId") Integer courseId, @Param("rating") Integer rating);
    
    // Tính average rating cho course
    @Query("SELECT AVG(cr.rating) FROM CourseRating cr WHERE cr.course.courseId = :courseId")
    Double getAverageRatingByCourse(@Param("courseId") Integer courseId);
    
    // Đếm tổng số ratings cho course
    @Query("SELECT COUNT(cr) FROM CourseRating cr WHERE cr.course.courseId = :courseId")
    Long getTotalRatingsByCourse(@Param("courseId") Integer courseId);
    
    // Lấy ratings của course với thông tin customer để hiển thị
    @Query("SELECT cr FROM CourseRating cr " +
           "JOIN FETCH cr.customer c " +
           "WHERE cr.course.courseId = :courseId " +
           "ORDER BY cr.createdDate DESC")
    List<CourseRating> findByCourseWithCustomerInfo(@Param("courseId") Integer courseId);
    
    // Kiểm tra customer đã hoàn thành course và được phép rate chưa
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END " +
           "FROM CourseRating cr WHERE cr.customer.customerId = :customerId " +
           "AND cr.course.courseId = :courseId AND cr.isCourseCompleted = true")
    Boolean hasCustomerCompletedAndRatedCourse(@Param("customerId") Integer customerId, @Param("courseId") Integer courseId);
    
    // Lấy top rated courses
    @Query("SELECT cr.course.courseId, AVG(cr.rating) as avgRating, COUNT(cr) as totalRatings " +
           "FROM CourseRating cr " +
           "GROUP BY cr.course.courseId " +
           "HAVING COUNT(cr) >= :minRatings " +
           "ORDER BY avgRating DESC, totalRatings DESC")
    List<Object[]> findTopRatedCourses(@Param("minRatings") Long minRatings);
} 