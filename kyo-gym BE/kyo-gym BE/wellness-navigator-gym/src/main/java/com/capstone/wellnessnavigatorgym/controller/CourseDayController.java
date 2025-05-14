package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDayDetailDto;
import com.capstone.wellnessnavigatorgym.entity.CourseDays;
import com.capstone.wellnessnavigatorgym.service.ICourseDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course-day")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseDayController {

    @Autowired
    private ICourseDayService courseDayService;

    @GetMapping("")
    public ResponseEntity<List<CourseDays>> getAllCourseDays() {
        List<CourseDays> courseDaysList = this.courseDayService.findAll();
        if (courseDaysList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(courseDaysList, HttpStatus.OK);
    }

    @GetMapping("/courses-with-days")
    public ResponseEntity<List<CourseDayDetailDto>> getAllCoursesWithDays() {
        List<CourseDayDetailDto> coursesWithDays = courseDayService.findAllCoursesWithDays();

        if (coursesWithDays.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(coursesWithDays, HttpStatus.OK);
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDayDetailDto> getCourseWithDaysById(@PathVariable Integer courseId) {
        CourseDayDetailDto courseWithDays = courseDayService.findCourseById(courseId);

        if (courseWithDays == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(courseWithDays, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/days/{dayId}")
    public ResponseEntity<CourseDays> getCourseDayDetails(@PathVariable Integer courseId, @PathVariable Integer dayId) {
        CourseDays courseDayDetails = courseDayService.getCourseDayDetails(courseId, dayId);
        return ResponseEntity.ok(courseDayDetails);
    }

    @PostMapping("/{courseId}/advance")
    public ResponseEntity<CourseDays> advanceCourseDay(@PathVariable Integer courseId) {
        CourseDays nextDay = courseDayService.advanceCourseDay(courseId);
        return ResponseEntity.ok(nextDay);
    }

    @PostMapping("/{courseId}/reset")
    public ResponseEntity<CourseDays> resetToFirstDay(@PathVariable Integer courseId) {
        CourseDays firstDay = courseDayService.resetToFirstDay(courseId);
        return ResponseEntity.ok(firstDay);
    }
}
