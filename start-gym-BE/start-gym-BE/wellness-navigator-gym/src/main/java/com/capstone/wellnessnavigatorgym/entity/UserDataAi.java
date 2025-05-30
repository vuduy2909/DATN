package com.capstone.wellnessnavigatorgym.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

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
    
    @Column(name = "track_data_ai_id", nullable = false)
    private Integer track_data_ai_id;
    
    private Integer age;
    private String gender;
    private Double height;     // Chiều cao (cm)
    private Double weight;     // Cân nặng (kg)
    private Double weight_after; // Cân nặng sau khi tập
    private Double waist_circumference;
    private Double chest_circumference;
    private Double waist_circumference_after; // Vòng eo sau khi tập
    private Double chest_circumference_after; // Vòng ngực sau khi tập
    private Double bmi;
    
    // Thêm trường để track thời gian
    @Column(name = "created_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated_date;
    
    // These are VARCHAR fields in user_data_ai table, not enums
    @Column(name = "occupation")
    private String occupation; // VARCHAR in database
    
    @Column(name = "work_environment") 
    private String work_environment; // VARCHAR in database
    
    @Column(name = "training_goals")
    @Convert(converter = TrainingGoalsConverter.class)
    private TrainingGoals training_goals;
    
    // Use standard TrainingFrequency enum (now synchronized with database)
    @Enumerated(EnumType.STRING)
    @Column(name = "training_frequency")
    private TrainingFrequency training_frequency;
    
    // Body type uses enum with converter
    @Column(name = "body_type")
    @Convert(converter = BodyTypeConverter.class)
    private BodyType body_type;   // Use BodyType enum
    
    private Boolean effective;
    
    // This is VARCHAR in user_data_ai table, not enum
    @Column(name = "training_history")
    private String training_history; // VARCHAR in database

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "course_id")
    private Course course;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    // Automatically set timestamps
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updated_date = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated_date = LocalDateTime.now();
    }
}
