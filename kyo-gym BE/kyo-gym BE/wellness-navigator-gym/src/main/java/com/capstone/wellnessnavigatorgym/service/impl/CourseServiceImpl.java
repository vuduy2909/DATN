package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfCommentDto;
import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfExerciseDto;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.service.ICourseService;
import com.capstone.wellnessnavigatorgym.utils.ConvertToInteger;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl implements ICourseService {

    @Autowired
    private ICourseRepository courseRepository;

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @SneakyThrows
    @Override
    public Course findCourseById(Integer id) {
        Optional<Course> course = courseRepository.findCourseById(id);
        if (course.isPresent()) {
            return course.get();
        }
        throw new NotFoundById("Could not find any courses with code: " + id);
    }

    @Override
    public List<CourseDetailsOfExerciseDto> getCourseDetailsByDayAndCourseOfExercise(Integer courseId, Integer dayId) {
        List<Tuple> tupleList = courseRepository.getCourseDetailsByDayAndCourseOfExercise(courseId, dayId);
        return tupleList.stream()
                .map(this::tupleToCourseDetailsOfExerciseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseDetailsOfCommentDto> getCourseDetailsByDayAndCourseOfComment(Integer courseId, Integer dayId) {
        List<Tuple> tupleList = courseRepository.getCourseDetailsByDayAndCourseOfComment(courseId, dayId);
        return tupleList.stream()
                .map(this::tupleToCourseDetailsOfCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Course course) {
        courseRepository.save(course);
    }

    @Override
    public void updateCourseStatus(Integer courseId) {
        courseRepository.updateStatus(courseId);
    }

    public CourseDetailsOfExerciseDto tupleToCourseDetailsOfExerciseDto(Tuple tuple) {
        return new CourseDetailsOfExerciseDto(
                ConvertToInteger.convertToInteger(tuple.get("course_id")),
                tuple.get("course_name", String.class),
                tuple.get("description", String.class),
                tuple.get("duration", String.class),
                tuple.get("image", String.class),
                ConvertToInteger.convertToInteger(tuple.get("day_id")),
                tuple.get("day_name", String.class),
                ConvertToInteger.convertToInteger(tuple.get("exercise_id")),
                tuple.get("body_part", String.class),
                tuple.get("equipment", String.class),
                tuple.get("exercise_description", String.class),
                tuple.get("exercise_name", String.class),
                tuple.get("instructions", String.class),
                tuple.get("target", String.class),
                tuple.get("video_url", String.class)
        );
    }

    public CourseDetailsOfCommentDto tupleToCourseDetailsOfCommentDto(Tuple tuple) {
        return new CourseDetailsOfCommentDto(
                ConvertToInteger.convertToInteger(tuple.get("course_id")),
                tuple.get("course_name", String.class),
                ConvertToInteger.convertToInteger(tuple.get("day_id")),
                tuple.get("day_name", String.class),
                ConvertToInteger.convertToInteger(tuple.get("exercise_id")),
                tuple.get("exercise_name", String.class),
                ConvertToInteger.convertToInteger(tuple.get("comment_id")),
                tuple.get("comment_text", String.class),
                ConvertToInteger.convertToInteger(tuple.get("rating")),
                ConvertToInteger.convertToInteger(tuple.get("customer_id")),
                tuple.get("customer_name", String.class),
                tuple.get("customer_img", String.class)
        );
    }
}
