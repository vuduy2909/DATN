package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Day;

import java.util.List;

public interface IDayService {
    List<Day> findAll();

    Day findDayById(Integer id);
}
