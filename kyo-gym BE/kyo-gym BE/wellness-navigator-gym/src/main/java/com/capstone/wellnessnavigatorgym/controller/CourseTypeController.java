package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.CourseType;
import com.capstone.wellnessnavigatorgym.service.ICourseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/course-type")
@CrossOrigin(origins = "http://localhost:3000")
public class CourseTypeController {

    @Autowired
    private ICourseTypeService courseTypeService;

    @GetMapping("")
    public ResponseEntity<List<CourseType>> getAllCourseType() {
        List<CourseType> courseTypeList = this.courseTypeService.findAll();
        if (courseTypeList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(courseTypeList, HttpStatus.OK);
    }
}
