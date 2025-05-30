package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.UserDashboardStatsDTO;
import java.util.Map;

public interface UserDashboardService {
    
    /**
     * Lấy toàn bộ thống kê dashboard cho người dùng
     */
    UserDashboardStatsDTO getUserDashboardStats(Integer customerId);
    
    /**
     * Lấy % hoạt động trong tuần
     */
    Double getWeeklyActivityPercentage(Integer customerId);
    
    /**
     * Lấy thời gian tập luyện trong tuần (format: HH:MM:SS)
     */
    String getWorkedTimeThisWeek(Integer customerId);
    
    /**
     * Lấy dữ liệu cho biểu đồ tiến độ
     */
    Map<String, Object> getProgressChartData(Integer customerId);
    
    /**
     * Lấy dữ liệu cho biểu đồ hoạt động
     */
    Map<String, Object> getActivityChartData(Integer customerId);
} 