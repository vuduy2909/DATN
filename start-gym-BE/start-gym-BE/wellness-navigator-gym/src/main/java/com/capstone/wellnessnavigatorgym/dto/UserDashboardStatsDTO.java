package com.capstone.wellnessnavigatorgym.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardStatsDTO {
    
    private Double weeklyActivity; // % hoạt động trong tuần
    private String workedTimeThisWeek; // Thời gian luyện tập trong tuần
    private Integer coursesInProgress; // Số khóa học đang theo
    private Double bmi; // Chỉ số BMI
    
    private Map<String, Object> activityChartData; // Dữ liệu cho biểu đồ hoạt động
    private Map<String, Object> progressChartData; // Dữ liệu cho biểu đồ tiến độ
    
    // Class con cho biểu đồ tiến độ theo ngày
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayProgressDTO {
        private String label; // Label (Day 1-10, etc.)
        private Double value; // Giá trị hoàn thành (%)
    }
    
    // Class con cho biểu đồ hoạt động theo ngày
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayActivityDTO {
        private String day; // Ngày (Day 1, Day 10, etc.)
        private Double activity; // Mức độ hoạt động
    }
} 