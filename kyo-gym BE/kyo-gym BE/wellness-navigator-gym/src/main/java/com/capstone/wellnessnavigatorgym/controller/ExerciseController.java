package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.exercise.ExerciseInfo;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.Exercise;
import com.capstone.wellnessnavigatorgym.service.IExerciseDayService;
import com.capstone.wellnessnavigatorgym.service.IExerciseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/exercise")
@CrossOrigin(origins = "http://localhost:3000")
public class ExerciseController {

    @Autowired
    private IExerciseService exerciseService;

    @Autowired
    private IExerciseDayService exerciseDayService;

    @GetMapping("")
    public ResponseEntity<List<Exercise>> getAllExercise() {
        List<Exercise> exerciseList = this.exerciseService.findAll();
        if (exerciseList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(exerciseList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exercise> getExerciseById(@PathVariable Integer id) {
        return new ResponseEntity<>(exerciseService.findExerciseById(id), HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExercise(@Valid @RequestBody ExerciseInfo exerciseInfo, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                String fieldName = error.getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
            return ResponseEntity.badRequest().body(errors);
        }

        Exercise exercise = new Exercise();
        exercise.setExerciseName(exerciseInfo.getExerciseName());
        exercise.setBodyPart(exerciseInfo.getBodyPart());
        exercise.setEquipment(exerciseInfo.getEquipment());
        exercise.setVideoUrl(exerciseInfo.getVideoUrl());
        exercise.setTarget(exerciseInfo.getTarget());
        exercise.setExerciseDescription(exerciseInfo.getExerciseDescription());
        exercise.setInstructions(exerciseInfo.getInstructions());
        exercise.setIsEnable(true);
        exercise.setDuration(exerciseInfo.getDuration());
        exercise.setUploadDate(LocalDateTime.now());
        exercise.setViews(0);

        exerciseService.save(exercise);

        if (exerciseInfo.getDays() != null) {
            exerciseDayService.addExerciseToDay(exerciseInfo.getDays(), exercise.getExerciseId());
        }

        return new ResponseEntity<>(new MessageResponse("New exercise successfully created!"), HttpStatus.CREATED);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateExercise(@Valid @PathVariable Integer id,
                                            @RequestBody ExerciseInfo exerciseInfo,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                    });
            return ResponseEntity.badRequest().body(errors);
        } else {
            exerciseService.update(exerciseInfo, id);
        }
        return new ResponseEntity<>(new MessageResponse("The exercise has successfully edited!!"), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExercise(@PathVariable Integer id) {
        exerciseService.deleteById(id);
        return new ResponseEntity<>(new MessageResponse("Exercise has successfully deleted!"), HttpStatus.OK);
    }

/*    @PutMapping("/watchStatus/{id}")
    public ResponseEntity<?> updateWatchStatus(@PathVariable Integer id,
                                               @RequestBody WatchStatusDto watchStatusDto) {
        exerciseService.updateVideoWatchStatus(id, watchStatusDto.getIsVideoFinished());
        return ResponseEntity.ok(new MessageResponse("Watch status updated successfully"));
    }*/
}
