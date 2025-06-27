package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.service.OptimizedQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý tối ưu hóa và monitoring hệ thống
 * CHỈ dành cho ADMIN
 */
@RestController
@RequestMapping("/api/v1/admin/optimization")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class OptimizationController {

    private final OptimizedQueryService optimizedQueryService;

    /**
     * Lấy thống kê hiệu suất database
     */
    @GetMapping("/database-stats")
    public ResponseEntity<?> getDatabaseStats() {
        try {
            Map<String, Object> stats = optimizedQueryService.getDatabaseStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting database stats", e);
            return ResponseEntity.internalServerError()
                .body("Error getting database stats: " + e.getMessage());
        }
    }

    /**
     * Clear tất cả cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearAllCaches() {
        try {
            optimizedQueryService.clearAllCaches();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All caches cleared successfully");
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing caches", e);
            return ResponseEntity.internalServerError()
                .body("Error clearing caches: " + e.getMessage());
        }
    }

    /**
     * Clear cache cho specific customer
     */
    @PostMapping("/clear-cache/customer/{customerId}")
    public ResponseEntity<?> clearCustomerCache(@PathVariable Integer customerId) {
        try {
            optimizedQueryService.clearCustomerCache(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer cache cleared successfully");
            response.put("customerId", customerId);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error clearing customer cache", e);
            return ResponseEntity.internalServerError()
                .body("Error clearing customer cache: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê tối ưu cho tháng hiện tại
     */
    @GetMapping("/monthly-stats")
    public ResponseEntity<?> getCurrentMonthStats() {
        try {
            LocalDate now = LocalDate.now();
            Map<String, Object> stats = optimizedQueryService.getMonthlyStatsOptimized(
                now.getYear(), now.getMonthValue());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting monthly stats", e);
            return ResponseEntity.internalServerError()
                .body("Error getting monthly stats: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê cho tháng cụ thể
     */
    @GetMapping("/monthly-stats/{year}/{month}")
    public ResponseEntity<?> getMonthStats(@PathVariable int year, @PathVariable int month) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().body("Invalid month: " + month);
            }
            
            Map<String, Object> stats = optimizedQueryService.getMonthlyStatsOptimized(year, month);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting stats for {}/{}", year, month, e);
            return ResponseEntity.internalServerError()
                .body("Error getting monthly stats: " + e.getMessage());
        }
    }

    /**
     * Lấy top performers của tháng
     */
    @GetMapping("/top-performers")
    public ResponseEntity<?> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            // Nếu không có year/month, dùng tháng hiện tại
            if (year == null || month == null) {
                LocalDate now = LocalDate.now();
                year = now.getYear();
                month = now.getMonthValue();
            }
            
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().body("Invalid month: " + month);
            }
            
            List<Object[]> topPerformers = optimizedQueryService.getTopPerformersOfMonth(year, month, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("topPerformers", topPerformers);
            response.put("year", year);
            response.put("month", month);
            response.put("limit", limit);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting top performers", e);
            return ResponseEntity.internalServerError()
                .body("Error getting top performers: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê course popularity
     */
    @GetMapping("/course-stats")
    public ResponseEntity<?> getCourseStats() {
        try {
            List<Object[]> courseStats = optimizedQueryService.getCoursePopularityStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("courseStats", courseStats);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting course stats", e);
            return ResponseEntity.internalServerError()
                .body("Error getting course stats: " + e.getMessage());
        }
    }

    /**
     * Cleanup dữ liệu cũ
     */
    @PostMapping("/cleanup")
    public ResponseEntity<?> cleanupOldData(@RequestParam(defaultValue = "30") int daysOld) {
        try {
            if (daysOld < 7) {
                return ResponseEntity.badRequest()
                    .body("Cannot cleanup data less than 7 days old for safety");
            }
            
            int deletedCount = optimizedQueryService.cleanupOldNullCompletions(daysOld);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleanup completed successfully");
            response.put("deletedRecords", deletedCount);
            response.put("daysOld", daysOld);
            response.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during cleanup", e);
            return ResponseEntity.internalServerError()
                .body("Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Test connection và hiệu suất
     */
    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Test một query đơn giản
            Map<String, Object> dbStats = optimizedQueryService.getDatabaseStats();
            
            long endTime = System.currentTimeMillis();
            long queryTime = endTime - startTime;
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("queryTime", queryTime + "ms");
            health.put("timestamp", java.time.LocalDateTime.now());
            health.put("databaseConnected", true);
            health.put("tableCount", ((List<?>) dbStats.get("tableSizes")).size());
            
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Health check failed", e);
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            health.put("timestamp", java.time.LocalDateTime.now());
            health.put("databaseConnected", false);
            
            return ResponseEntity.status(500).body(health);
        }
    }

    /**
     * Benchmark query performance
     */
    @GetMapping("/benchmark")
    public ResponseEntity<?> benchmark() {
        try {
            Map<String, Object> results = new HashMap<>();
            
            // Test 1: Customer progress query
            long start1 = System.currentTimeMillis();
            optimizedQueryService.getCustomerCourseProgressOptimized(1, 1);
            long time1 = System.currentTimeMillis() - start1;
            results.put("customerProgressQuery", time1 + "ms");
            
            // Test 2: Monthly stats
            long start2 = System.currentTimeMillis();
            LocalDate now = LocalDate.now();
            optimizedQueryService.getMonthlyStatsOptimized(now.getYear(), now.getMonthValue());
            long time2 = System.currentTimeMillis() - start2;
            results.put("monthlyStatsQuery", time2 + "ms");
            
            // Test 3: Top performers
            long start3 = System.currentTimeMillis();
            optimizedQueryService.getTopPerformersOfMonth(now.getYear(), now.getMonthValue(), 10);
            long time3 = System.currentTimeMillis() - start3;
            results.put("topPerformersQuery", time3 + "ms");
            
            results.put("totalTime", (time1 + time2 + time3) + "ms");
            results.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Benchmark failed", e);
            return ResponseEntity.internalServerError()
                .body("Benchmark failed: " + e.getMessage());
        }
    }
} 