package com.capstone.wellnessnavigatorgym.dto.course;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDetail {
    // UserDataId
    private Integer userDataId;
    
    // UserData fields
    private Integer age;
    private Double bmi;
    private String gender;
    private String training_goals;
    private String trainingHistory;
    
    // Recommendation status
    private Boolean recommendedStatus;
    
    // Course fields
    private Integer courseId;
    private String courseName;
    private String description;
    private String duration;
    private String image;
    private Boolean status;
    private String courseTypeName;
}
