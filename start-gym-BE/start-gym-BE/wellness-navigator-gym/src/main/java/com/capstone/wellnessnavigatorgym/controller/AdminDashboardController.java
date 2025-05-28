package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.DashboardStatsDTO;
import com.capstone.wellnessnavigatorgym.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class AdminDashboardController {

    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardController.class);
    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        try {
            logger.info("=== DASHBOARD ENDPOINT CALLED ===");
            DashboardStatsDTO dashboardStats = adminDashboardService.getDashboardStats();
            logger.info("Dashboard stats retrieved successfully");
            logger.info("Total users: {}", dashboardStats.getUserStats().getTotal());
            logger.info("Active courses: {}", dashboardStats.getCourseStats().getActive());
            logger.info("Total revenue: {}", dashboardStats.getRevenueStats().getTotal());
            logger.info("=== RETURNING DASHBOARD DATA ===");
            return ResponseEntity.ok(dashboardStats);
        } catch (Exception e) {
            logger.error("Error getting dashboard stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/test")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DashboardStatsDTO> getTestDashboardStats() {
        try {
            logger.info("=== TEST DASHBOARD ENDPOINT CALLED ===");
            DashboardStatsDTO dashboardStats = adminDashboardService.getTestDashboardStats();
            logger.info("Test dashboard stats retrieved successfully");
            logger.info("Test total users: {}", dashboardStats.getUserStats().getTotal());
            logger.info("=== RETURNING TEST DASHBOARD DATA ===");
            return ResponseEntity.ok(dashboardStats);
        } catch (Exception e) {
            logger.error("Error getting test dashboard stats", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/debug")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getDebugData() {
        try {
            logger.info("=== DEBUG ENDPOINT CALLED ===");
            
            // Test basic queries
            Long totalCustomers = adminDashboardService.getDebugTotalCustomers();
            Long activeCourses = adminDashboardService.getDebugActiveCourses();
            Double totalRevenue = adminDashboardService.getDebugTotalRevenue();
            
            logger.info("Debug - Total customers: {}", totalCustomers);
            logger.info("Debug - Active courses: {}", activeCourses);
            logger.info("Debug - Total revenue: {}", totalRevenue);
            
            java.util.Map<String, Object> debugData = new java.util.HashMap<>();
            debugData.put("totalCustomers", totalCustomers);
            debugData.put("activeCourses", activeCourses);
            debugData.put("totalRevenue", totalRevenue);
            debugData.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(debugData);
        } catch (Exception e) {
            logger.error("Error getting debug data", e);
            return ResponseEntity.internalServerError().body("Debug error: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/raw-counts")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getRawCounts() {
        try {
            logger.info("=== RAW COUNTS ENDPOINT CALLED ===");
            
            // Get raw counts without any filters
            java.util.Map<String, Object> rawData = adminDashboardService.getRawCounts();
            rawData.put("timestamp", java.time.LocalDateTime.now());
            rawData.put("message", "Raw database counts");
            
            return ResponseEntity.ok(rawData);
        } catch (Exception e) {
            logger.error("Error getting raw counts", e);
            return ResponseEntity.internalServerError().body("Raw counts error: " + e.getMessage());
        }
    }

    @GetMapping("/dashboard/user-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserStats() {
        try {
            return ResponseEntity.ok(adminDashboardService.getUserStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/course-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getCourseStats() {
        try {
            return ResponseEntity.ok(adminDashboardService.getCourseStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/revenue-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getRevenueStats() {
        try {
            return ResponseEntity.ok(adminDashboardService.getRevenueStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/dashboard/order-stats")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getOrderStats() {
        try {
            return ResponseEntity.ok(adminDashboardService.getOrderStats());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
} 