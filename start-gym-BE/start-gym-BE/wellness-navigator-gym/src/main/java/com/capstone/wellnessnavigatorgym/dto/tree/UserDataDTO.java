package com.capstone.wellnessnavigatorgym.dto.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDataDTO {
    private String activity_level;
    private Integer age;
    private String gender;
    private Double height;     // Chiều cao (cm)
    private Double weight;     // Cân nặng (kg)
    private Double weight_after;
    private Double bmi;
    private Double waist_circumference;
    private Double chest_circumference;
    private Double waist_circumference_after;
    private Double chest_circumference_after;
    private String bodyType;   // Loại cơ thể
    private String training_goals;
    private String training_history;
    private String trainingFrequency;
    private Boolean heartDisease;
    private Boolean respiratoryDisease;
    private Boolean recentInjury;
    private String occupation; // Nghề nghiệp
    private String workEnvironment; // Môi trường làm việc
    private String healthIssues; // Các vấn đề sức khỏe (có thể lưu dạng chuỗi phân cách bằng dấu phẩy)
    private Integer customerId;

    @Override
    public String toString() {
        return "UserDataDTO{" +
                "activity_level='" + activity_level + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", weight_after=" + weight_after +
                ", bmi=" + bmi +
                ", waist_circumference=" + waist_circumference +
                ", chest_circumference=" + chest_circumference +
                ", waist_circumference_after=" + waist_circumference_after +
                ", chest_circumference_after=" + chest_circumference_after +
                ", bodyType='" + bodyType + '\'' +
                ", training_goals='" + training_goals + '\'' +
                ", training_history='" + training_history + '\'' +
                ", trainingFrequency='" + trainingFrequency + '\'' +
                ", heartDisease=" + heartDisease +
                ", respiratoryDisease=" + respiratoryDisease +
                ", recentInjury=" + recentInjury +
                ", occupation='" + occupation + '\'' +
                ", workEnvironment='" + workEnvironment + '\'' +
                ", healthIssues='" + healthIssues + '\'' +
                ", customerId=" + customerId +
                '}';
    }
}
