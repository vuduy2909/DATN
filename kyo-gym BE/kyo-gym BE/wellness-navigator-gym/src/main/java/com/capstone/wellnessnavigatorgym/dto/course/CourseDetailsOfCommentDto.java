package com.capstone.wellnessnavigatorgym.dto.course;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetailsOfCommentDto {
    private Integer courseId;
    private String courseName;
    private Integer dayId;
    private String dayName;
    private Integer exerciseId;
    private String exerciseName;
    private Integer commentId;
    private String commentText;
    private Integer rating;
    private Integer customerId;
    private String customerName;
    private String customerImg;
}
