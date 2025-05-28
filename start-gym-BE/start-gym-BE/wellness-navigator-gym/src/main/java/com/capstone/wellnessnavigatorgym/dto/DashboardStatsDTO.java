package com.capstone.wellnessnavigatorgym.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    private UserStatsDTO userStats;
    private CourseStatsDTO courseStats;
    private RevenueStatsDTO revenueStats;
    private OrderStatsDTO orderStats;
    private RegistrationStatsDTO registrationStats;
    private List<RecentActivityDTO> recentActivities;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStatsDTO {
        private Long total;
        private Long regular;
        private Long premium;
        private Long vip;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStatsDTO {
        private Long total;
        private Long active;
        private Long hidden;
        private Long newThisWeek;
        private Long newThisMonth;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueStatsDTO {
        private Double total;
        private Double vip;
        private Double premium;
        private Double thisMonth;
        private Double thisWeek;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderStatsDTO {
        private Long total;
        private Long pending;
        private Long success;
        private Long failed;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistrationStatsDTO {
        private Long today;
        private Long thisWeek;
        private Long thisMonth;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivityDTO {
        private LocalDateTime createdAt;
        private String type;
        private String userName;
        private String status;
        private Double amount;
        private String description;
    }
} 