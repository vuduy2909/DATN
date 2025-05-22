package com.capstone.wellnessnavigatorgym.dto.tree;

import com.capstone.wellnessnavigatorgym.entity.Course;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RecommendationDTO {
    private Course recommendedCourses;
    private String message;
    private Integer userDataId;

    public RecommendationDTO() {
    }
    
    public RecommendationDTO(Course recommendedCourses, String message) {
        this.recommendedCourses = recommendedCourses;
        this.message = message;
    }
    
    public RecommendationDTO(Course recommendedCourses, String message, Integer userDataId) {
        this.recommendedCourses = recommendedCourses;
        this.message = message;
        this.userDataId = userDataId;
    }
}
