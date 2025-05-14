package com.capstone.wellnessnavigatorgym.dto.course;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetail {
    Integer userDataId;
    String activityLevel;
    Integer age;
    Double bmi;
    String gender;
    String trainingGoals;
    String trainingHistory;
    Boolean recommendedStatus;
    Integer courseId;
    String courseName;
    String description;
    String duration;
    String image;
    Boolean status;
    String courseTypeName;
}
