package com.capstone.wellnessnavigatorgym.dto.tree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDataDTO {
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
    private String body_type;   // Loại cơ thể
    private String training_goals;
    private String training_history;
    private String training_frequency;
    private Boolean heart_disease;
    private Boolean respiratory_disease;
    private Boolean recent_injury;
    private String occupation; // Nghề nghiệp
    private String work_environment; // Môi trường làm việc
    private Integer customerId;

    @Override
    public String toString() {
        return "UserDataDTO{" +
                "age=" + age +
                ", gender='" + gender + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", weight_after=" + weight_after +
                ", bmi=" + bmi +
                ", waist_circumference=" + waist_circumference +
                ", chest_circumference=" + chest_circumference +
                ", waist_circumference_after=" + waist_circumference_after +
                ", chest_circumference_after=" + chest_circumference_after +
                ", body_type='" + body_type + '\'' +
                ", training_goals='" + training_goals + '\'' +
                ", training_history='" + training_history + '\'' +
                ", training_frequency='" + training_frequency + '\'' +
                ", heart_disease=" + heart_disease +
                ", respiratory_disease=" + respiratory_disease +
                ", recent_injury=" + recent_injury +
                ", occupation='" + occupation + '\'' +
                ", work_environment='" + work_environment + '\'' +
                ", customerId=" + customerId +
                '}';
    }
}
