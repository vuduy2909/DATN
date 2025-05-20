package com.capstone.wellnessnavigatorgym.dto.tree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserFormDataDTO {
    // Step 1: Thông tin tập luyện hiện tại
    private String training_history;  // "Không tập", "2 buổi/tuần", "3 buổi/tuần", "4 buổi/tuần", "5 buổi/tuần trở lên"
    
    // Step 2: Thông tin cơ bản
    private String gender;          // "Nam", "Nữ"
    private Integer age;            // Tuổi
    private Double height;          // Chiều cao (cm)
    private Double weight;          // Cân nặng (kg)
    private String body_type;        // "Thin", "Skinny Fat", "Overweight", "Normal", "Fit"
    
    // Step 3: Mục tiêu tập luyện
    private String training_goals;    // "Giảm cân tổng thể", "Giảm mỡ, giữ cơ", "Tăng cơ bắp", ...
    
    // Step 4: Tần suất tập luyện mong muốn
    private String training_frequency; // "2 buổi/tuần", "3 buổi/tuần", "4 buổi/tuần", "5 buổi/tuần"
    
    // Step 5: Thông tin nghề nghiệp và sức khỏe
    private String occupation;      // "Sinh viên", "Nhân viên văn phòng", ...
    private String work_environment; // "Văn phòng", "Đứng nhiều", ...
    
    // Các trường sức khỏe - dùng tên trường giống cơ sở dữ liệu
    private Boolean heart_disease;      // Mắc bệnh tim
    private Boolean respiratory_disease; // Mắc bệnh hô hấp
    private Boolean recent_injury;      // Có chấn thương gần đây
    
    // Step 6: Số đo cơ thể
    private Double chest_circumference;  // Vòng ngực (cm)
    private Double waist_circumference;  // Vòng eo (cm)
    
    // Ảnh người dùng (url hoặc base64 string)
    private String bodyImage;
    
    // Thông tin người dùng
    private Integer customerId;

    @Override
    public String toString() {
        return "UserFormDataDTO{" +
                "training_history='" + training_history + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + age +
                ", height=" + height +
                ", weight=" + weight +
                ", body_type='" + body_type + '\'' +
                ", training_goals='" + training_goals + '\'' +
                ", training_frequency='" + training_frequency + '\'' +
                ", occupation='" + occupation + '\'' +
                ", work_environment='" + work_environment + '\'' +
                ", heart_disease=" + heart_disease +
                ", respiratory_disease=" + respiratory_disease +
                ", recent_injury=" + recent_injury +
                ", chest_circumference=" + chest_circumference +
                ", waist_circumference=" + waist_circumference +
                ", customerId=" + customerId +
                '}';
    }
}

