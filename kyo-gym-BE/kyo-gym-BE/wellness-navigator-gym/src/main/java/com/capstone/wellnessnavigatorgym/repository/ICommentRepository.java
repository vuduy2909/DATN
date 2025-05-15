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
import java.util.Optional;

@Repository
@Transactional
public interface ICommentRepository extends JpaRepository<Comment, Integer> {
    @Query(value = "SELECT * FROM `comments` where comment_id = :id", nativeQuery = true)
    Optional<Comment> findCommentById(@Param("id") Integer id);

    @Modifying
    @Query(value = "INSERT INTO `comments` (comment_text, rating, customer_id, exercise_id) " +
            "VALUES (:comment_text, :rating, :customer_id, :exercise_id)", nativeQuery = true)
    void insertComment(@Param("comment_text") String comment_text,
                        @Param("rating") Integer rating,
                        @Param("customer_id") String customer_id,
                        @Param("exercise_id") String exercise_id);
}
