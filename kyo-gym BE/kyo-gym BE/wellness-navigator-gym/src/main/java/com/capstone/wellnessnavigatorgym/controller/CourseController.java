package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfCommentDto;
import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfExerciseDto;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.service.ICourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    @GetMapping("")
    public ResponseEntity<List<Course>> getAllCourse() {
        List<Course> courseList = this.courseService.findAll();
        if (courseList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(courseList, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Integer id) {
        return new ResponseEntity<>(courseService.findCourseById(id), HttpStatus.OK);
    }

    @GetMapping("/{courseId}/day/{dayId}/exercise")
    public ResponseEntity<List<CourseDetailsOfExerciseDto>> getCourseDetailsByDayAndCourseOfExercise(@PathVariable Integer courseId, @PathVariable Integer dayId) {
        List<CourseDetailsOfExerciseDto> courseDetailsOfExerciseDtoList = this.courseService.getCourseDetailsByDayAndCourseOfExercise(courseId, dayId);
        if (courseDetailsOfExerciseDtoList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(courseDetailsOfExerciseDtoList, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/day/{dayId}/comment")
    public ResponseEntity<List<CourseDetailsOfCommentDto>> getCourseDetailsByDayAndCourseOfComment(@PathVariable Integer courseId, @PathVariable Integer dayId) {
        List<CourseDetailsOfCommentDto> courseDetailsOfCommentDtoList = this.courseService.getCourseDetailsByDayAndCourseOfComment(courseId, dayId);
        if (courseDetailsOfCommentDtoList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(courseDetailsOfCommentDtoList, HttpStatus.OK);
    }

    @PatchMapping("/{courseId}/status")
    public ResponseEntity<?> updateCourseStatus(@PathVariable Integer courseId) {
        try {
            courseService.updateCourseStatus(courseId);
            return new ResponseEntity<>(new MessageResponse("Edit status successful!"), HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating course status: " + e.getMessage());
        }
    }
}
