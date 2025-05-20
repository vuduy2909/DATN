package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
@Table(name = "user_data_ai")
public class UserDataAi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_data_id", nullable = false)
    private Integer user_data_id;
    
    @Column(name = "track_data_ai_id", nullable = true)
    private Integer track_data_ai_id;
    
    private Integer age;
    private String gender;
    private Double height;     // Chiều cao (cm)
    private Double weight;     // Cân nặng (kg)
    private Double waist_circumference;
    private Double chest_circumference;
    private Double bmi;
    private String occupation; // Nghề nghiệp
    private String work_environment; // Môi trường làm việc
    private String training_goals;
    private Boolean heart_disease;
    private Boolean respiratory_disease;
    private Boolean recent_injury;
    private String training_frequency;
    private String body_type;   // Loại cơ thể
    private Boolean effective;
    private String training_history;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
