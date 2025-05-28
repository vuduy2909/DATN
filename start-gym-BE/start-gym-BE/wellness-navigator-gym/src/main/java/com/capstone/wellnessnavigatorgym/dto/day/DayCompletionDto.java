package com.capstone.wellnessnavigatorgym.dto.day;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayCompletionDto {
    private Integer dayId;
    private String dayName;
    private Boolean status; // Status của day trong course
    private Boolean isCompleted; // Trạng thái completion của user
    private String completionDate; // Ngày hoàn thành (nếu có)
} 