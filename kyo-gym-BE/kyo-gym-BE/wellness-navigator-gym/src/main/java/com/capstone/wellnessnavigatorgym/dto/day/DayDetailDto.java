package com.capstone.wellnessnavigatorgym.dto.day;

import com.capstone.wellnessnavigatorgym.dto.exercise.ExerciseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayDetailDto {
    private Integer dayId;
    private String dayName;
    private Boolean status;
    private List<ExerciseDto> exercises;  // Danh sách bài tập của ngày
} 