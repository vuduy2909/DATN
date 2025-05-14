package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.ExerciseDay;
import com.capstone.wellnessnavigatorgym.service.IDayService;
import com.capstone.wellnessnavigatorgym.service.IExerciseDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exercise-day")
@CrossOrigin(origins = "http://localhost:3000")
public class ExerciseDayController {

    @Autowired
    private IExerciseDayService exerciseDayService;

    @Autowired
    private IDayService dayService;

    @GetMapping("/day/{dayId}")
    public ResponseEntity<List<ExerciseDay>> getExercisesByDay(@PathVariable Integer dayId) {
        List<ExerciseDay> exercises = exerciseDayService.findExercisesByDay(dayId);
        return ResponseEntity.ok(exercises);
    }

    @PutMapping("/updateStatusForDay/day/{dayId}/exercise/{exerciseId}")
    public ResponseEntity<ExerciseDay> updateStatusForDay(@PathVariable Integer dayId, @PathVariable Integer exerciseId, @RequestParam Boolean status) {
        ExerciseDay updatedExercises = exerciseDayService.updateStatusForDay(dayId, exerciseId, status);
        return ResponseEntity.ok(updatedExercises);
    }
}
