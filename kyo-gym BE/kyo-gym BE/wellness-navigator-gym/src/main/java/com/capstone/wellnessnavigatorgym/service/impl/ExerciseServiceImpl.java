package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.exercise.ExerciseInfo;
import com.capstone.wellnessnavigatorgym.entity.Exercise;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.error.ResourceNotFoundException;
import com.capstone.wellnessnavigatorgym.repository.IExerciseRepository;
import com.capstone.wellnessnavigatorgym.service.IExerciseService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExerciseServiceImpl implements IExerciseService {

    @Autowired
    private IExerciseRepository exerciseRepository;

    @Override
    public List<Exercise> findAll() {
        return exerciseRepository.findAll();
    }

    @SneakyThrows
    @Override
    public Exercise findExerciseById(Integer id) {
        Optional<Exercise> exercise = exerciseRepository.findExerciseById(id);
        if (exercise.isPresent()) {
            return exercise.get();
        }
        throw new NotFoundById("Could not find any exercises with code: " + id);
    }

    @Override
    public Exercise save(Exercise exercise) {
        return exerciseRepository.save(exercise);
    }

    @Override
    public void update(ExerciseInfo exerciseInfo, Integer id) {
        exerciseRepository.updateExercise(id, exerciseInfo.getExerciseName(), exerciseInfo.getBodyPart(),
                exerciseInfo.getEquipment(), exerciseInfo.getVideoUrl(), exerciseInfo.getTarget(),
                exerciseInfo.getExerciseDescription(), exerciseInfo.getInstructions(), true);
    }

    @Override
    public void deleteById(Integer id) {
        exerciseRepository.deleteExerciseId(id);
    }

/*    @Override
    public void updateVideoWatchStatus(Integer exerciseId, Boolean isVideoFinished) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
        exercise.setIsVideoFinished(isVideoFinished);
        if (isVideoFinished) {
            exercise.setViews(exercise.getViews() + 1);
        }
        exerciseRepository.save(exercise);
    }*/
}
