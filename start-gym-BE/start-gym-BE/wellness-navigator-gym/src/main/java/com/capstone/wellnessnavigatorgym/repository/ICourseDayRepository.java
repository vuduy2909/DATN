package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CourseDays;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ICourseDayRepository extends JpaRepository<CourseDays, Integer> {

    List<CourseDays> findByCourse(Course course);

    @Query("SELECT cd FROM CourseDays cd " +
           "LEFT JOIN FETCH cd.course c " +
           "LEFT JOIN FETCH cd.day d " +
           "WHERE cd.courseId = :courseId AND cd.status = true " +
           "ORDER BY cd.dayId")
    List<CourseDays> findByCourseIdOrderByDayId(@Param("courseId") Integer courseId);

    @Query("SELECT cd FROM CourseDays cd " +
           "LEFT JOIN FETCH cd.course c " +
           "LEFT JOIN FETCH cd.day d " +
           "WHERE cd.courseId = :courseId AND cd.dayId = :dayId")
    Optional<CourseDays> findByCourseIdAndDayId(@Param("courseId") Integer courseId, @Param("dayId") Integer dayId);

    List<CourseDays> findByCourseCourseId(Integer courseId);
}
