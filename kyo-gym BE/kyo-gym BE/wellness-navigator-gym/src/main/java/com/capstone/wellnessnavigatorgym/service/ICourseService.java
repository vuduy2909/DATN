package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfCommentDto;
import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfExerciseDto;
import com.capstone.wellnessnavigatorgym.entity.Course;

import java.util.List;

public interface ICourseService {
    List<Course> findAll();

    Course findCourseById(Integer id);

    List<CourseDetailsOfExerciseDto> getCourseDetailsByDayAndCourseOfExercise(Integer courseId, Integer dayId);

    List<CourseDetailsOfCommentDto> getCourseDetailsByDayAndCourseOfComment(Integer courseId, Integer dayId);

    void save(Course course);

    void updateCourseStatus(Integer courseId);
}
