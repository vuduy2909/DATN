package com.capstone.wellnessnavigatorgym.dto.course;

import com.capstone.wellnessnavigatorgym.dto.day.DayCompletionDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseWithCompletionDto {
    private Integer courseId;
    private String courseName;
    private String description;
    private String duration;
    private String image;
    private Boolean status;
    private CourseTypeDto courseType;
    private List<DayCompletionDto> days;
    private Double completionPercentage; // Phần trăm hoàn thành
    private Integer completedDays; // Số ngày đã hoàn thành
    private Integer totalDays; // Tổng số ngày
} 