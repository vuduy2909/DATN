package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDayDetailDto;
import com.capstone.wellnessnavigatorgym.dto.course.CourseTypeDto;
import com.capstone.wellnessnavigatorgym.dto.day.DayDto;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CourseDays;
import com.capstone.wellnessnavigatorgym.entity.CourseType;
import com.capstone.wellnessnavigatorgym.repository.ICourseDayRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.service.ICourseDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseDayServiceImpl implements ICourseDayService {

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
        List<CourseDays> courseDaysList = courseDayRepository.findAll();
        return courseDaysList.stream()
                .collect(Collectors.groupingBy(CourseDays::getCourse))
                .entrySet().stream()
                .map(entry -> convertCourseToDTO(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private CourseDayDetailDto convertCourseToDTO(Course course, List<CourseDays> courseDays) {
        CourseDayDetailDto courseDayDetailDto = new CourseDayDetailDto();
        courseDayDetailDto.setCourseId(course.getCourseId());
        courseDayDetailDto.setCourseName(course.getCourseName());
        courseDayDetailDto.setDescription(course.getDescription());
        courseDayDetailDto.setDuration(course.getDuration());
        courseDayDetailDto.setImage(course.getImage());
        courseDayDetailDto.setStatus(course.getStatus());
        courseDayDetailDto.setCourseType(convertCourseTypeToDTO(course.getCourseType()));
        courseDayDetailDto.setDays(courseDays.stream()
                .map(this::convertDayToDTO)
                .collect(Collectors.toList()));
        return courseDayDetailDto;
    }

    private CourseTypeDto convertCourseTypeToDTO(CourseType courseType) {
        CourseTypeDto courseTypeDto = new CourseTypeDto();
        courseTypeDto.setCourseTypeId(courseType.getCourseTypeId());
        courseTypeDto.setCourseTypeName(courseType.getCourseTypeName());
        return courseTypeDto;
    }

    private DayDto convertDayToDTO(CourseDays courseDay) {
        DayDto dayDto = new DayDto();
        dayDto.setDayId(courseDay.getDay().getDayId());
        dayDto.setDayName(courseDay.getDay().getDayName());
        return dayDto;
    }

    @Override
    public CourseDayDetailDto findCourseById(Integer courseId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);

        if (!courseOptional.isPresent()) {
            return null;
        }

        Course course = courseOptional.get();
        List<CourseDays> courseDays = courseDayRepository.findByCourse(course);

        return convertCourseToDTO(course, courseDays);
    }

    @Override
    public CourseDays getCourseDayDetails(Integer courseId, Integer dayId) {
        return courseDayRepository.findByCourseIdAndDayId(courseId, dayId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Day not found for this course"));
    }

    @Override
    public CourseDays advanceCourseDay(Integer courseId) {
        List<CourseDays> courseDaysList = courseDayRepository.findByCourseCourseId(courseId);
        return courseDaysList.stream()
                .filter(cd -> !cd.getStatus())
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No more days to advance in this course"));
    }

    @Override
    public CourseDays resetToFirstDay(Integer courseId) {
        List<CourseDays> courseDaysList = courseDayRepository.findByCourseCourseId(courseId);
        return courseDaysList.stream()
                .min(Comparator.comparingInt(CourseDays::getCourseDayId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "This course does not have any days"));
    }
}
