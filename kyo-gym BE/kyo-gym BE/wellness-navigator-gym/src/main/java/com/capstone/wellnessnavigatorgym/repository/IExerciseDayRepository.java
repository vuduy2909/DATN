package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.ExerciseDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface IExerciseDayRepository extends JpaRepository<ExerciseDay, Integer> {

    @Modifying
    @Query(value = "INSERT INTO exercise_day (day_id, exercise_id) VALUES (:dayId, :exerciseId)", nativeQuery = true)
    void addExerciseToDay(@Param("dayId") Integer dayId,
                          @Param("exerciseId") Integer exerciseId);


    @Query(value = "SELECT * FROM exercise_day WHERE day_id = :dayId", nativeQuery = true)
    List<ExerciseDay> findByDayId(@Param("dayId") Integer dayId);
}
