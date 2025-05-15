package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
public class UserDataAi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userDataId;
    private String activityLevel;
    private Integer age;
    private String gender;
    private Double height;     // Chiều cao (cm)
    private Double weight;     // Cân nặng (kg)
    private Double bmi;
    private String trainingGoals;
    private String trainingHistory;
    private String bodyType;   // Loại cơ thể
    private String occupation; // Nghề nghiệp
    private String workEnvironment; // Môi trường làm việc
    private String healthIssues; // Các vấn đề sức khỏe (có thể lưu dạng chuỗi phân cách bằng dấu phẩy)
    private Double chestSize;  // Vòng ngực (cm)
    private Double waistSize;  // Vòng eo (cm)
    private Boolean effective;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
