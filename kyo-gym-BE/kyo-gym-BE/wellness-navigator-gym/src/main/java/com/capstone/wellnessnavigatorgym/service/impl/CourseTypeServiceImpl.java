package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.CourseType;
import com.capstone.wellnessnavigatorgym.repository.ICourseTypeRepository;
import com.capstone.wellnessnavigatorgym.service.ICourseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseTypeServiceImpl implements ICourseTypeService {

    @Autowired
    private ICourseTypeRepository courseTypeRepository;

    @Override
    public List<CourseType> findAll() {
        return courseTypeRepository.findAll();
    }
}
