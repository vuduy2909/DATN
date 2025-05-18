package com.capstone.wellnessnavigatorgym.dto.course;

import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleCourseDto {
    private Integer courseId;
    private String courseName;
    private String description;
    private String duration;
    private String image;
    private String courseTypeName;
    private Boolean recommendedStatus;
    
    public static SimpleCourseDto fromCustomerCourse(CustomerCourse customerCourse) {
        if (customerCourse == null || customerCourse.getCourse() == null) {
            return null;
        }
        
        Course course = customerCourse.getCourse();
        String courseTypeName = course.getCourseType() != null ? 
                course.getCourseType().getCourseTypeName() : "General";
        
        return new SimpleCourseDto(
                course.getCourseId(),
                course.getCourseName(),
                course.getDescription(),
                course.getDuration(),
                course.getImage(),
                courseTypeName,
                customerCourse.getRecommendedStatus()
        );
    }
} 