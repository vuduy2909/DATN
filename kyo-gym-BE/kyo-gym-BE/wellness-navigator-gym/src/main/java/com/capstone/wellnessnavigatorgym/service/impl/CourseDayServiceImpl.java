package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDayDetailDto;
import com.capstone.wellnessnavigatorgym.dto.course.CourseDetailsOfExerciseDto;
import com.capstone.wellnessnavigatorgym.dto.course.CourseTypeDto;
import com.capstone.wellnessnavigatorgym.dto.day.DayDto;
import com.capstone.wellnessnavigatorgym.dto.day.DayDetailDto;
import com.capstone.wellnessnavigatorgym.dto.exercise.ExerciseDto;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.repository.ICourseDayRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.service.ICourseDayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.Tuple;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CourseDayServiceImpl implements ICourseDayService {
    private static final Logger logger = LoggerFactory.getLogger(CourseDayServiceImpl.class);

    @Autowired
    private ICourseDayRepository courseDayRepository;

    @Autowired
    private ICourseRepository courseRepository;

    @Override
    public List<CourseDays> findAll() {
        return courseDayRepository.findAll();
    }

    @Override
    public List<CourseDayDetailDto> findAllCoursesWithDays() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(course -> {
                    List<CourseDays> courseDays = courseDayRepository.findByCourseIdOrderByDayId(course.getCourseId());
                    return convertCourseToDTO(course, courseDays);
                })
                .collect(Collectors.toList());
    }

    private CourseDayDetailDto convertCourseToDTO(Course course, List<CourseDays> courseDays) {
        CourseDayDetailDto dto = new CourseDayDetailDto();
        dto.setCourseId(course.getCourseId());
        dto.setCourseName(course.getCourseName());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setImage(course.getImage());
        dto.setStatus(course.getStatus());
        dto.setCourseType(convertCourseTypeToDTO(course.getCourseType()));
        
        List<DayDto> sortedDays = courseDays.stream()
                .sorted(Comparator.comparing(CourseDays::getDayId))
                .map(this::convertDayToDTO)
                .collect(Collectors.toList());
        
        logger.debug("Converting course {} with {} days", course.getCourseId(), sortedDays.size());
        sortedDays.forEach(day -> 
            logger.debug("Day in course {}: id={}, name={}", 
                course.getCourseId(), day.getDayId(), day.getDayName())
        );
        
        dto.setDays(sortedDays);
        return dto;
    }

    private CourseTypeDto convertCourseTypeToDTO(CourseType courseType) {
        if (courseType == null) {
            return null;
        }
        CourseTypeDto dto = new CourseTypeDto();
        dto.setCourseTypeId(courseType.getCourseTypeId());
        dto.setCourseTypeName(courseType.getCourseTypeName());
        return dto;
    }

    private DayDto convertDayToDTO(CourseDays courseDay) {
        DayDto dto = new DayDto();
        Day day = courseDay.getDay();
        if (day != null) {
            dto.setDayId(day.getDayId());
            dto.setDayName(day.getDayName());
            dto.setStatus(courseDay.getStatus());
            logger.debug("Converting day: id={}, name={}, status={}", 
                day.getDayId(), day.getDayName(), courseDay.getStatus());
        } else {
            logger.warn("Day entity is null for courseDay with dayId={}", courseDay.getDayId());
            dto.setDayId(courseDay.getDayId());
            dto.setDayName("Day " + courseDay.getDayId());
            dto.setStatus(courseDay.getStatus());
        }
        return dto;
    }

    @Override
    public CourseDayDetailDto findCourseById(Integer courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        
        List<CourseDays> courseDays = courseDayRepository.findByCourseIdOrderByDayId(courseId);
        if (courseDays.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No days found for this course");
        }
        
        logger.debug("Found {} days for course {}", courseDays.size(), courseId);
        courseDays.forEach(cd -> 
            logger.debug("Day: id={}, name={}, status={}", 
                cd.getDayId(), 
                cd.getDay() != null ? cd.getDay().getDayName() : "null",
                cd.getStatus())
        );
        
        return convertCourseToDTO(course, courseDays);
    }

    @Override
    public CourseDays getCourseDayDetails(Integer courseId, Integer dayId) {
        return courseDayRepository.findByCourseIdAndDayId(courseId, dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    String.format("Day %d not found in course %d", dayId, courseId)));
    }

    @Override
    public CourseDays advanceCourseDay(Integer courseId) {
        List<CourseDays> courseDays = courseDayRepository.findByCourseIdOrderByDayId(courseId);
        return courseDays.stream()
                .filter(cd -> !cd.getStatus())
                .min(Comparator.comparing(CourseDays::getDayId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "No more days to advance in this course"));
    }

    @Override
    public CourseDays resetToFirstDay(Integer courseId) {
        List<CourseDays> courseDays = courseDayRepository.findByCourseIdOrderByDayId(courseId);
        return courseDays.stream()
                .min(Comparator.comparing(CourseDays::getDayId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "This course does not have any days"));
    }

    @Override
    public DayDetailDto getDayDetails(Integer courseId, Integer dayId) {
        CourseDays courseDay = courseDayRepository.findByCourseIdAndDayId(courseId, dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Day not found for this course"));

        DayDetailDto dayDetailDto = new DayDetailDto();
        dayDetailDto.setDayId(courseDay.getDayId());
        dayDetailDto.setDayName("Day " + courseDay.getDayId());
        dayDetailDto.setStatus(courseDay.getStatus());
        
        // Lấy danh sách bài tập cho ngày cụ thể
        List<Tuple> exerciseTuples = courseRepository.getCourseDetailsByDayAndCourseOfExercise(courseId, dayId);
        
        if (exerciseTuples != null && !exerciseTuples.isEmpty()) {
            List<ExerciseDto> exerciseDtos = exerciseTuples.stream()
                .map(tuple -> {
                    ExerciseDto dto = new ExerciseDto();
                    dto.setExerciseId(tuple.get("exercise_id", Integer.class));
                    dto.setExerciseName(tuple.get("exercise_name", String.class));
                    dto.setDescription(tuple.get("exercise_description", String.class));
                    dto.setVideoUrl(tuple.get("video_url", String.class));
                    dto.setStatus(true); // Default to true since these are active exercises
                    return dto;
                })
                .collect(Collectors.toList());
            
            dayDetailDto.setExercises(exerciseDtos);
            logger.debug("Found {} exercises for day {} in course {}", 
                exerciseDtos.size(), dayId, courseId);
        } else {
            logger.warn("No exercises found for day {} in course {}", dayId, courseId);
            dayDetailDto.setExercises(Collections.emptyList());
        }
        
        return dayDetailDto;
    }
}
