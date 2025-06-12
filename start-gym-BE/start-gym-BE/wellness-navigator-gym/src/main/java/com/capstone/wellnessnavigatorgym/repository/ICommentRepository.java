package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.Comment;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ICommentRepository extends JpaRepository<Comment, Integer> {
    @Query(value = "SELECT * FROM `comments` where comment_id = :id", nativeQuery = true)
    Optional<Comment> findCommentById(@Param("id") Integer id);

    @Modifying
    @Query(value = "INSERT INTO `comments` (comment_text, customer_id, exercise_id, course_id, day_id, created_date, updated_date) " +
            "VALUES (:comment_text, :customer_id, :exercise_id, :course_id, :day_id, NOW(), NOW())", nativeQuery = true)
    void insertComment(@Param("comment_text") String comment_text,
                        @Param("customer_id") String customer_id,
                        @Param("exercise_id") String exercise_id,
                        @Param("course_id") String course_id,  // Can be null
                        @Param("day_id") String day_id);       // Can be null
    
    // Additional queries for new Comment structure
    @Query("SELECT c FROM Comment c ORDER BY c.createdDate DESC")
    List<Comment> findAllOrderByCreatedDateDesc();
    
    @Query("SELECT c FROM Comment c WHERE c.isReported = true OR c.reportCount > 0 ORDER BY c.reportCount DESC, c.createdDate DESC")
    List<Comment> findReportedComments();
    
    @Query("SELECT c FROM Comment c WHERE c.course.courseId = :courseId ORDER BY c.createdDate DESC")
    List<Comment> findByCourseOrderByCreatedDateDesc(@Param("courseId") Integer courseId);
    
    @Query("SELECT c FROM Comment c WHERE c.day.dayId = :dayId ORDER BY c.createdDate DESC")
    List<Comment> findByDayOrderByCreatedDateDesc(@Param("dayId") Integer dayId);
    
    @Query("SELECT c FROM Comment c WHERE c.exercise.exerciseId = :exerciseId ORDER BY c.createdDate DESC")
    List<Comment> findByExerciseOrderByCreatedDateDesc(@Param("exerciseId") Integer exerciseId);
}
