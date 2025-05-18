package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * Phiên bản nâng cao của TrackDataAi với các trường bổ sung
 * để hỗ trợ các thuộc tính mới từ UserDataAi
 */
@Setter
@Getter
@RequiredArgsConstructor
@Entity
public class EnhancedTrackDataAi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer trackDataAiId;
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
    private String healthIssues; // Các vấn đề sức khỏe
    private Double chestSize;  // Vòng ngực (cm)
    private Double waistSize;  // Vòng eo (cm)
    private Boolean effective;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_id")
    private Course course;

    @Override
    public String toString() {
        return "EnhancedTrackDataAi{" +
                "trackDataAiId=" + trackDataAiId +
                ", activityLevel='" + activity_level + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                ", bmi=" + bmi +
                ", trainingGoals='" + training_goals + '\'' +
                ", trainingHistory='" + training_history + '\'' +
                ", bodyType='" + bodyType + '\'' +
                ", occupation='" + occupation + '\'' +
                ", workEnvironment='" + workEnvironment + '\'' +
                ", healthIssues='" + healthIssues + '\'' +
                ", chestSize=" + chestSize +
                ", waistSize=" + waistSize +
                ", effective=" + effective +
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
            case "height":
                return getHeight();
            case "weight":
                return getWeight();
            case "bmi":
                return getBmi();
            case "training_goals":
                return getTraining_goals();
            case "training_history":
                return getTraining_history();
            case "bodyType":
                return getBodyType();
            case "occupation":
                return getOccupation();
            case "workEnvironment":
                return getWorkEnvironment();
            case "healthIssues":
                return getHealthIssues();
            case "chestSize":
                return getChestSize();
            case "waistSize":
                return getWaistSize();
            case "course_id":
                return this.course != null ? this.course.getCourseId() : null;
            case "effective":
                return getEffective();
            default:
                System.out.println("Invalid attribute: " + attributeName);
                return null;
        }
    }
    
    /**
     * Chuyển đổi từ TrackDataAi
     */
    public static EnhancedTrackDataAi fromTrackDataAi(TrackDataAi trackDataAi) {
        EnhancedTrackDataAi enhanced = new EnhancedTrackDataAi();
        enhanced.setTrackDataAiId(trackDataAi.getTrackDataAiId());
        enhanced.setActivity_level(trackDataAi.getActivity_level());
        enhanced.setAge(trackDataAi.getAge());
        enhanced.setGender(trackDataAi.getGender());
        enhanced.setBmi(trackDataAi.getBmi());
        enhanced.setTraining_goals(trackDataAi.getTraining_goals());
        enhanced.setTraining_history(trackDataAi.getTraining_history());
        enhanced.setEffective(trackDataAi.getEffective());
        enhanced.setCourse(trackDataAi.getCourse());
        
        // Thiết lập các giá trị mặc định cho các trường mới
        enhanced.setHeight(170.0); // Chiều cao mặc định
        enhanced.setWeight(70.0);  // Cân nặng mặc định
        enhanced.setBodyType("Normal"); // Loại cơ thể mặc định
        enhanced.setOccupation("Office Worker"); // Nghề nghiệp mặc định
        enhanced.setWorkEnvironment("Indoor"); // Môi trường làm việc mặc định
        enhanced.setHealthIssues("None"); // Không có vấn đề sức khỏe
        enhanced.setChestSize(90.0); // Vòng ngực mặc định
        enhanced.setWaistSize(80.0); // Vòng eo mặc định
        
        return enhanced;
    }
}
