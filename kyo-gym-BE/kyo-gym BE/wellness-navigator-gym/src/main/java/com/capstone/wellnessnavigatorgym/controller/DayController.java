package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Day;
import com.capstone.wellnessnavigatorgym.service.IDayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/day")
@CrossOrigin(origins = "http://localhost:3000")
public class DayController {

    @Autowired
    private IDayService dayService;

    @GetMapping("")
    public ResponseEntity<List<Day>> getAllDay() {
        List<Day> dayList = this.dayService.findAll();
        if (dayList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(dayList, HttpStatus.OK);
    }
}