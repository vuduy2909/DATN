package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.CourseDays;
import com.capstone.wellnessnavigatorgym.entity.Day;
import com.capstone.wellnessnavigatorgym.entity.Exercise;
import com.capstone.wellnessnavigatorgym.entity.ExerciseDay;
import com.capstone.wellnessnavigatorgym.repository.ICourseDayRepository;
import com.capstone.wellnessnavigatorgym.repository.IDayRepository;
import com.capstone.wellnessnavigatorgym.repository.IExerciseDayRepository;
import com.capstone.wellnessnavigatorgym.repository.IExerciseRepository;
import com.capstone.wellnessnavigatorgym.service.IExerciseDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ExerciseDayServiceImpl implements IExerciseDayService {

    @Autowired
    private IExerciseRepository exerciseRepository;

    @Autowired
    private IDayRepository dayRepository;

    @Autowired
    private IExerciseDayRepository exerciseDayRepository;

    @Autowired
    private ICourseDayRepository courseDayRepository;

    @Override
    public void addExerciseToDay(Integer dayId, Integer exerciseId) {
        if (dayRepository.existsById(dayId) && exerciseRepository.existsById(exerciseId)) {
            Day day = dayRepository.getDayById(dayId);
            Exercise exercise = exerciseRepository.getExerciseById(exerciseId);
            exerciseDayRepository.addExerciseToDay(day.getDayId(), exercise.getExerciseId());
        }
    }

    @Override
    public List<ExerciseDay> findExercisesByDay(Integer dayId) {
        return exerciseDayRepository.findByDayId(dayId);
    }

    @Override
    public ExerciseDay updateStatusForDay(Integer dayId, Integer exerciseId, Boolean status) {
        List<ExerciseDay> exerciseDays = exerciseDayRepository.findByDayId(dayId);

        if (exerciseDays.isEmpty()) {
            throw new EntityNotFoundException("No exercises found for day: " + dayId);
        }

        ExerciseDay currentExerciseDay = exerciseDays.stream()
                .filter(exerciseDay -> exerciseDay.getExercise().getExerciseId().equals(exerciseId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("ExerciseDay not found for exerciseId: " + exerciseId));

        currentExerciseDay.setStatus(status);
        currentExerciseDay.setIsCompleted(status);
        exerciseDayRepository.save(currentExerciseDay);

        if (status) {
            // Check if this is the last exercise of the day
            boolean isLastExercise = exerciseDays.indexOf(currentExerciseDay) == (exerciseDays.size() - 1);

            if (!isLastExercise) {
                // Not the last exercise, so set the next exercise's status to true
                ExerciseDay nextExerciseDay = exerciseDays.get(exerciseDays.indexOf(currentExerciseDay) + 1);
                nextExerciseDay.setStatus(true);
                nextExerciseDay.setIsCompleted(false); // Next exercise is not completed yet
                exerciseDayRepository.save(nextExerciseDay);
            } else {
                // Last exercise of the day, check if all exercises for the current day are completed
                boolean allExercisesCompletedToday = exerciseDays.stream()
                        .allMatch(ExerciseDay::getIsCompleted);

                if (allExercisesCompletedToday) {
                    // Find the first exercise of the next day and start it
                    Integer nextDayId = dayId + 1;
                    List<ExerciseDay> nextDayExercises = exerciseDayRepository.findByDayId(nextDayId);
                    if (!nextDayExercises.isEmpty()) {
                        ExerciseDay firstExerciseNextDay = nextDayExercises.get(0);
                        firstExerciseNextDay.setStatus(true);
                        firstExerciseNextDay.setIsCompleted(false); // Since this is the new exercise being started
                        exerciseDayRepository.save(firstExerciseNextDay);

                        CourseDays nextDay = courseDayRepository.findById(nextDayId)
                                .orElseThrow(() -> new EntityNotFoundException("Day not found for dayId: " + nextDayId));
                        nextDay.setStatus(true);
                        courseDayRepository.save(nextDay);
                    }
                }
            }
        }
        return currentExerciseDay;
    }
}
