package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.UserDashboardStatsDTO;
import com.capstone.wellnessnavigatorgym.service.UserDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user-dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserDashboardController {

    private final UserDashboardService userDashboardService;

    /**
     * Lấy toàn bộ thống kê dashboard cho người dùng
     */
    @GetMapping("/stats")
    public ResponseEntity<UserDashboardStatsDTO> getUserDashboardStats(Authentication authentication) {
        Integer customerId = Integer.parseInt(authentication.getName());
        log.info("Getting dashboard stats for user ID: {}", customerId);
        
        UserDashboardStatsDTO stats = userDashboardService.getUserDashboardStats(customerId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Lấy % hoạt động theo tuần
     */
    @GetMapping("/weekly-activity")
    public ResponseEntity<Map<String, Object>> getWeeklyActivity(Authentication authentication) {
        Integer customerId = Integer.parseInt(authentication.getName());
        
        Map<String, Object> response = new HashMap<>();
        Double weeklyActivity = userDashboardService.getWeeklyActivityPercentage(customerId);
        response.put("weeklyActivity", weeklyActivity);
        
        return ResponseEntity.ok(response);
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
     * Lấy dữ liệu cho biểu đồ tiến độ
     */
    @GetMapping("/progress-chart")
    public ResponseEntity<Map<String, Object>> getProgressChartData(Authentication authentication) {
        Integer customerId = Integer.parseInt(authentication.getName());
        
        Map<String, Object> chartData = userDashboardService.getProgressChartData(customerId);
        return ResponseEntity.ok(chartData);
    }
    
    /**
     * Lấy dữ liệu cho biểu đồ hoạt động
     */
    @GetMapping("/activity-chart")
    public ResponseEntity<Map<String, Object>> getActivityChartData(Authentication authentication) {
        Integer customerId = Integer.parseInt(authentication.getName());
        
        Map<String, Object> chartData = userDashboardService.getActivityChartData(customerId);
        return ResponseEntity.ok(chartData);
    }
} 