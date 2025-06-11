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
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/course")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseController {

    @Autowired
    private ICourseService courseService;

    @GetMapping("")
    public ResponseEntity<List<Course>> getAllCourse() {
        try {
            List<Course> courseList = this.courseService.findAll();
            
            if (courseList.isEmpty()) {
                return new ResponseEntity<>(courseList, HttpStatus.OK);
            }
            
            return new ResponseEntity<>(courseList, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
            Boolean newStatus = courseService.updateCourseStatus(courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("courseId", courseId);
            response.put("newStatus", newStatus);
            response.put("message", newStatus ? "Course is now visible" : "Course is now hidden");
            response.put("action", newStatus ? "shown" : "hidden");
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating course status: " + e.getMessage());
        }
    }
}
