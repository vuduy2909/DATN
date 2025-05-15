package com.capstone.wellnessnavigatorgym.dto.exercise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseDto {
    private Integer exerciseId;
    private String exerciseName;
    private String description;
    private String videoUrl;
    private String image;
    private Boolean status;
} 