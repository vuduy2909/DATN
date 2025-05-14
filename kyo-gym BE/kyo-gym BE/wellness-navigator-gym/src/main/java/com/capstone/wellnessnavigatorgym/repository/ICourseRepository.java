package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ICourseRepository extends JpaRepository<Course, Integer> {
    @Query(value = "SELECT * FROM course WHERE course_id = :id", nativeQuery = true)
    Optional<Course> findCourseById(@Param("id") Integer id);

    @Query(value = "select c.course_id, c.course_name, c.description, c.duration, c.image, d.day_id, d.day_name, " +
            "e.exercise_id, e.body_part, e.equipment, e.exercise_description, e.exercise_name, e.instructions, e.target, " +
            "e.video_url " +
            "from course c " +
            "join course_days cd on c.course_id = cd.course_id " +
            "join days d on cd.day_id = d.day_id " +
            "join exercise_day ed on d.day_id = ed.day_id " +
            "join exercise e on ed.exercise_id = e.exercise_id " +
            "where c.course_id = :courseId and d.day_id = :dayId and ed.status = true and cd.status = true", nativeQuery = true)
    List<Tuple> getCourseDetailsByDayAndCourseOfExercise(@Param("courseId") Integer courseId, @Param("dayId") Integer dayId);

    @Query(value = "SELECT c.course_id, c.course_name, d.day_id, d.day_name, e.exercise_id, e.exercise_name, " +
            "cm.comment_id, cm.comment_text, cm.rating, cu.customer_id, cu.customer_name, cu.customer_img " +
            "FROM course c " +
            "JOIN course_days cd ON c.course_id = cd.course_id " +
            "JOIN days d ON cd.day_id = d.day_id " +
            "JOIN exercise_day ed ON d.day_id = ed.day_id " +
            "JOIN exercise e ON ed.exercise_id = e.exercise_id " +
            "JOIN comments cm ON e.exercise_id = cm.exercise_id " +
            "JOIN customer cu ON cm.customer_id = cu.customer_id " +
            "WHERE c.course_id = :courseId AND d.day_id = :dayId", nativeQuery = true)
    List<Tuple> getCourseDetailsByDayAndCourseOfComment(@Param("courseId") Integer courseId, @Param("dayId") Integer dayId);

    @Modifying
    @Query(value = "UPDATE course SET status = true WHERE course_id = :courseId", nativeQuery = true)
    void updateStatus(@Param("courseId") Integer courseId);

}
