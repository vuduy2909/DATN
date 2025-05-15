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
    private Double bmi;
    private String training_goals;
    private String training_history;
    private String bodyType;   // Loại cơ thể
    private String occupation; // Nghề nghiệp
    private String workEnvironment; // Môi trường làm việc
    private String healthIssues; // Các vấn đề sức khỏe (có thể lưu dạng chuỗi phân cách bằng dấu phẩy)
    private Double chestSize;  // Vòng ngực (cm)
    private Double waistSize;  // Vòng eo (cm)
    private Integer customerId;

    @Override
    public String toString() {
        return "UserDataDTO{" +
                "activity_level='" + activity_level + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", bmi=" + bmi +
                ", training_goals='" + training_goals + '\'' +
                ", training_history='" + training_history + '\'' +
                ", bodyType='" + bodyType + '\'' +
                ", occupation='" + occupation + '\'' +
                ", workEnvironment='" + workEnvironment + '\'' +
                ", healthIssues='" + healthIssues + '\'' +
                ", chestSize=" + chestSize +
                ", waistSize=" + waistSize +
                ", customerId=" + customerId +
                '}';
    }
}
