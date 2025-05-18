package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.CourseType;

import java.util.List;

public interface ICourseTypeService {
    List<CourseType> findAll();
}
