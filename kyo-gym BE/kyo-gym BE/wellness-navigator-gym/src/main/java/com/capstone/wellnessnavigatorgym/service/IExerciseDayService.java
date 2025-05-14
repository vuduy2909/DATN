package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.ExerciseDay;

import java.util.List;

public interface IExerciseDayService {
    void addExerciseToDay(Integer dayId, Integer exerciseId);

    List<ExerciseDay> findExercisesByDay(Integer dayId);

    ExerciseDay updateStatusForDay(Integer dayId, Integer exerciseId, Boolean status);
}
