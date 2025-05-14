package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDayDetailDto;
import com.capstone.wellnessnavigatorgym.entity.CourseDays;

import java.util.List;

public interface ICourseDayService {

    List<CourseDays> findAll();

    List<CourseDayDetailDto> findAllCoursesWithDays();

    CourseDayDetailDto findCourseById(Integer courseId);

    CourseDays getCourseDayDetails(Integer courseId, Integer dayId);

    CourseDays advanceCourseDay(Integer courseId);

    CourseDays resetToFirstDay(Integer courseId);
}
