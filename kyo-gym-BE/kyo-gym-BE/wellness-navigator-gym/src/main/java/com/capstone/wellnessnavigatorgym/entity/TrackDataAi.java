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
    private Integer trackDataAiId;
    private String activity_level;
    private Integer age;
    private String gender;
    private Double bmi;
    private String training_goals;
    private String training_history;
    private Boolean effective;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_id")
    private Course course;

    @Override
    public String toString() {
        return "TrackDataAi{" +
                "trackDataAiId=" + trackDataAiId +
                ", activityLevel='" + activity_level + '\'' +
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
            case "activity_level":
                return getActivity_level();
            case "age":
                return getAge();
            case "gender":
                return getGender();
            case "bmi":
                return getBmi();
            case "training_goals":
                return getTraining_goals();
            case "training_history":
                return getTraining_history();
            case "course_id":
                return this.course != null ? this.course.getCourseId() : null; // Trả về course_id nếu course không null
            case "effective":
                return getEffective();
            default:
                System.out.println("Invalid attribute: " + attributeName);
                return null; // Hoặc bạn có thể ném một ngoại lệ
        }
    }
}
