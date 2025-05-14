package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Day;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.IDayRepository;
import com.capstone.wellnessnavigatorgym.service.IDayService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DayServiceImpl implements IDayService {

    @Autowired
    private IDayRepository dayRepository;

    @Override
    public List<Day> findAll() {
        return dayRepository.findAll();
    }

    @SneakyThrows
    @Override
    public Day findDayById(Integer id) {
        Optional<Day> day = dayRepository.findById(id);
        if (day.isPresent()) {
            return day.get();
        }
        throw new NotFoundById("Could not find any days with code: " + id);
    }
}
