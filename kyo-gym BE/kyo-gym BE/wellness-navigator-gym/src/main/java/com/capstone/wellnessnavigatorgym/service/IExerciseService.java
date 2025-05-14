package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.exercise.ExerciseInfo;
import com.capstone.wellnessnavigatorgym.entity.Exercise;

import java.util.List;

public interface IExerciseService {
    List<Exercise> findAll();

    Exercise findExerciseById(Integer id);

    Exercise save(Exercise exercise);

    void update(ExerciseInfo exerciseInfo, Integer id);

    void deleteById(Integer id);

//    void updateVideoWatchStatus(Integer exerciseId, Boolean isVideoFinished);
}
