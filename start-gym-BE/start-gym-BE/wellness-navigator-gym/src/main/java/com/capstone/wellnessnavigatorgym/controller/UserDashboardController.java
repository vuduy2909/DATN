package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.UserDashboardStatsDTO;
import com.capstone.wellnessnavigatorgym.service.UserDashboardService;
import com.capstone.wellnessnavigatorgym.service.CustomerDayCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/user-dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserDashboardController {

    private final UserDashboardService userDashboardService;
    private final CustomerDayCompletionService completionService;

    /**
     * Lấy toàn bộ thống kê dashboard cho người dùng
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserDashboardStats() {
        // Lấy username từ context bảo mật
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        log.info("Getting dashboard stats for user: {}", username);
        
        // Tính toán Weekly Activity
        Map<String, Object> weeklyActivityData = completionService.calculateWeeklyActivity(username);
        
        // Tạo dữ liệu Activity Chart (biểu đồ hoạt động theo tuần)
        Map<String, Object> activityChartData = generateActivityChartData(username);
        
        // Tạo response object
        Map<String, Object> response = new HashMap<>();
        
        // Thêm Weekly Activity vào response
        response.put("weeklyActivity", weeklyActivityData.get("weeklyActivity"));
        response.put("completedSessions", weeklyActivityData.get("completedSessions"));
        response.put("targetSessions", weeklyActivityData.get("targetSessions"));
        
        // Thêm Activity Chart data
        response.put("activityChartData", activityChartData);
        
        // Thêm Progress Chart data (dữ liệu mẫu)
        Map<String, Object> progressChartData = new HashMap<>();
        progressChartData.put("percentage", 75);
        progressChartData.put("completed", 15);
        progressChartData.put("total", 20);
        response.put("progressChartData", progressChartData);
        
        // Thêm các thông tin khác vào response nếu cần
        response.put("coursesInProgress", 2); // Placeholder - cần implement
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo dữ liệu Activity Chart theo tuần
     */
    private Map<String, Object> generateActivityChartData(String username) {
        List<Map<String, Object>> chartData = new ArrayList<>();
        
        // Lấy ngày hiện tại
        LocalDateTime now = LocalDateTime.now();
        
        // Tính 4 tuần gần nhất (bao gồm tuần hiện tại)
        for (int weekOffset = 3; weekOffset >= 0; weekOffset--) {
            LocalDateTime weekStart = now.minusWeeks(weekOffset)
                .toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
            LocalDateTime weekEnd = weekStart.plusDays(7);
            
            // Đếm số buổi hoàn thành trong tuần này
            Integer completedSessions = completionService.getCompletionRepository()
                .countCompletedSessionsInCurrentWeek(username, weekStart, weekEnd);
            
            // Đếm số khóa học đang tham gia (mỗi khóa = 3 buổi/tuần)
            Integer activeCourses = completionService.getCompletionRepository()
                .countActiveCoursesForCustomer(username);
            
            // Tính target sessions (tối thiểu 3 buổi/tuần)
            Integer targetSessions = Math.max(activeCourses * 3, 3);
            
            // Tính tỷ lệ hoàn thành
            double activityPercentage = targetSessions > 0 ? 
                Math.min((double) completedSessions / targetSessions * 100, 100) : 0;
            
            Map<String, Object> weekData = new HashMap<>();
            weekData.put("day", "Week " + (4 - weekOffset)); // Week 1, Week 2, Week 3, Week 4
            weekData.put("activity", Math.round(activityPercentage));
            weekData.put("completedSessions", completedSessions);
            weekData.put("targetSessions", targetSessions);
            weekData.put("tooltip", String.format("Week %d: %d%% (%d/%d buổi)", 
                (4 - weekOffset), Math.round(activityPercentage), completedSessions, targetSessions));
            
            chartData.add(weekData);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("data", chartData);
        result.put("title", "Weekly Activity Progress");
        result.put("description", "Tỷ lệ hoàn thành mục tiêu tập luyện theo tuần");
        
        return result;
    }
    
    /**
     * Lấy % hoạt động theo tuần
     */
    @GetMapping("/weekly-activity")
    public ResponseEntity<?> getWeeklyActivity() {
        // Lấy username từ context bảo mật
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Tính toán Weekly Activity
        Map<String, Object> weeklyActivityData = completionService.calculateWeeklyActivity(username);
        
        return ResponseEntity.ok(weeklyActivityData);
    }
    
    /**
     * Lấy dữ liệu Activity Chart
     */
    @GetMapping("/activity-chart")
    public ResponseEntity<?> getActivityChartData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Map<String, Object> chartData = generateActivityChartData(username);
        return ResponseEntity.ok(chartData);
    }
    
    /**
     * Lấy thời gian tập luyện trong tuần
     */
    @GetMapping("/worked-time")
    public ResponseEntity<Map<String, Object>> getWorkedTime(Authentication authentication) {
        Integer customerId = Integer.parseInt(authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        String workedTime = userDashboardService.getWorkedTimeThisWeek(customerId);
        response.put("workedTime", workedTime);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy dữ liệu cho biểu đồ tiến độ theo day cải thiện
     */
    @GetMapping("/progress-chart")
    public ResponseEntity<Map<String, Object>> getProgressChartData(Authentication authentication) {
        try {
            // Lấy customer ID từ customer service 
            String username = authentication.getName();
            
            // Tìm customer ID thông qua username - cần implement logic này
            // Tạm thời sử dụng customer ID mặc định hoặc lấy từ context khác
            Integer customerId = getCustomerIdFromUsername(username);
        
        Map<String, Object> chartData = userDashboardService.getProgressChartData(customerId);
            
            // Đảm bảo format phù hợp với frontend ChartCircle component
            Map<String, Object> response = new HashMap<>();
            response.put("data", chartData.get("data"));
            response.put("totalDays", chartData.get("totalDays"));
            response.put("completedDays", chartData.get("completedDays"));
            response.put("percentage", chartData.get("overallProgress"));
            response.put("completed", chartData.get("completedDays"));
            response.put("total", chartData.get("totalDays"));
            response.put("chartType", "day-by-day");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting progress chart data: {}", e.getMessage());
            // Return fallback data
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("percentage", 0);
            fallback.put("completed", 0);
            fallback.put("total", 0);
            fallback.put("data", new ArrayList<>());
            return ResponseEntity.ok(fallback);
        }
    }
    
    /**
     * Helper method để lấy customer ID từ username
     * TODO: Implement proper logic hoặc inject CustomerService
     */
    private Integer getCustomerIdFromUsername(String username) {
        // Tạm thời return customer ID mặc định
        // Trong production, cần implement logic thực tế
        try {
            return Integer.parseInt(username);
        } catch (NumberFormatException e) {
            log.warn("Username is not numeric, using default customer ID: {}", username);
            return 5; // Default customer ID cho testing
        }
    }
} 