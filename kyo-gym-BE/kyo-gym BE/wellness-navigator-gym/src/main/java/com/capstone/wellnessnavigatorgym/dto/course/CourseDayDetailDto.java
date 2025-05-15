package com.capstone.wellnessnavigatorgym.dto.course;

import com.capstone.wellnessnavigatorgym.dto.day.DayDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDayDetailDto {
    private Integer courseId;
    private String courseName;
    private String description;
    private String duration;
    private String image;
    private Boolean status;
    private CourseTypeDto courseType;
    private List<DayDto> days;
}
