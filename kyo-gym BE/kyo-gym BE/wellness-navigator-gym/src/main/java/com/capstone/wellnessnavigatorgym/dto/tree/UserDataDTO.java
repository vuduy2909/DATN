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
    private Double bmi;
    private String training_goals;
    private String training_history;
    private Integer customerId;

    @Override
    public String toString() {
        return "UserDataDTO{" +
                "activity_level='" + activity_level + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", bmi=" + bmi +
                ", training_goals='" + training_goals + '\'' +
                ", training_history='" + training_history + '\'' +
                '}';
    }
}

