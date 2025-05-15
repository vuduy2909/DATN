package com.capstone.wellnessnavigatorgym.dto.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserFormDataDTO {
    // Step 1: Thông tin tập luyện hiện tại
    private String trainingFrequency;  // "Không tập", "2 buổi/tuần", "3 buổi/tuần", "4 buổi/tuần", "5 buổi/tuần trở lên"
    
    // Step 2: Thông tin cơ bản
    private String gender;          // "Nam", "Nữ"
    private Integer age;            // Tuổi
    private Double height;          // Chiều cao (cm)
    private Double weight;          // Cân nặng (kg)
    private String bodyType;        // "Thin", "Skinny Fat", "Overweight", "Normal", "Fit"
    
    // Step 3: Mục tiêu tập luyện
    private String trainingGoal;    // "Giảm cân tổng thể", "Giảm mỡ, giữ cơ", "Tăng cơ bắp", ...
    
    // Step 4: Tần suất tập luyện mong muốn
    private String desiredFrequency; // "2 buổi/tuần", "3 buổi/tuần", "4 buổi/tuần", "5 buổi/tuần"
    
    // Step 5: Thông tin nghề nghiệp và sức khỏe
    private String occupation;      // "Sinh viên", "Nhân viên văn phòng", ...
    private String workEnvironment; // "Văn phòng", "Đứng nhiều", ...
    private List<String> healthIssues; // ["heart", "respiratory", "injury"]
    
    // Step 6: Số đo cơ thể
    private Double chestSize;       // Vòng ngực (cm)
    private Double waistSize;       // Vòng eo (cm)
    
    // Ảnh người dùng (url hoặc base64 string)
    private String bodyImage;
    
    // Thông tin người dùng
    private Integer customerId;

    @Override
    public String toString() {
        return "UserFormDataDTO{" +
                "trainingFrequency='" + trainingFrequency + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", height=" + height +
                ", weight=" + weight +
                ", bodyType='" + bodyType + '\'' +
                ", trainingGoal='" + trainingGoal + '\'' +
                ", desiredFrequency='" + desiredFrequency + '\'' +
                ", occupation='" + occupation + '\'' +
                ", workEnvironment='" + workEnvironment + '\'' +
                ", healthIssues=" + healthIssues +
                ", chestSize=" + chestSize +
                ", waistSize=" + waistSize +
                ", customerId=" + customerId +
                '}';
    }
}
