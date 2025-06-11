package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
public class TrackDataAi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "track_data_ai_id")
    private Integer trackDataAiId;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "gender")
    private String gender;
    
    @Column(name = "height")
    private Double height;
    
    @Column(name = "weight")
    private Double weight;
    
    @Column(name = "waist_circumference")
    private Double waist_circumference;
    
    @Column(name = "chest_circumference")
    private Double chest_circumference;
    
    @Column(name = "bmi")
    private Double bmi;
    
    @Column(name = "occupation")
    private String occupation;
    
    @Column(name = "work_environment")
    private String work_environment;
    
    @Column(name = "training_goals")
    private String training_goals;
    
    @Column(name = "training_history")
    private String training_history;
    
    @Column(name = "effective")
    private Boolean effective;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_id")
    private Course course;

    @Override
    public String toString() {
        return "TrackDataAi{" +
                "trackDataAiId=" + trackDataAiId +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", bmi=" + bmi +
                ", trainingGoals='" + training_goals + '\'' +
                ", trainingHistory='" + training_history +
                '}';
    }

    public Object getAttributeValue(String attributeName) {
        switch (attributeName) {
            case "track_data_ai_id":
                return getTrackDataAiId();
            case "age":
                return getAge();
            case "gender":
                return getGender();
            case "height":
                return getHeight();
            case "weight":
                return getWeight();
            case "waist_circumference":
                return getWaist_circumference();
            case "chest_circumference":
                return getChest_circumference();
            case "bmi":
                return getBmi();
            case "occupation":
                return getOccupation();
            case "work_environment":
                return getWork_environment();
            case "training_goals":
                return getTraining_goals();
            case "training_history":
                return getTraining_history();
            case "course_id":
                return this.course != null ? this.course.getCourseId() : null;
            case "effective":
                return getEffective();
            case "body_type":
                return null; // Not stored in TrackDataAi but needed for compatibility
            default:
                System.out.println("Invalid attribute: " + attributeName);
                return null;
        }
    }

    public static boolean isValidAttribute(String attributeName) {
        switch (attributeName) {
            case "track_data_ai_id":
            case "age":
            case "gender":
            case "height":
            case "weight":
            case "waist_circumference":
            case "chest_circumference":
            case "bmi":
            case "occupation":
            case "work_environment":
            case "training_goals":
            case "training_history":
            case "course_id":
            case "effective":
            case "body_type":  // Support for compatibility
                return true;
            default:
                return false;
        }
    }
}
