package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.CustomerDayCompletion;
import com.capstone.wellnessnavigatorgym.exception.AttendanceException;
import com.capstone.wellnessnavigatorgym.service.CustomerDayCompletionService;
import com.capstone.wellnessnavigatorgym.service.CustomerDayCompletionService.CourseProgressDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/v1/customer-progress")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerDayCompletionController {
    
    private final CustomerDayCompletionService completionService;
    
    /**
     * Đánh dấu một buổi học là đã hoàn thành
     * POST /api/customer-progress/complete
     */
    @PostMapping("/complete")
    public ResponseEntity<CustomerDayCompletion> markDayAsCompleted(
            @RequestParam Integer customerId,
            @RequestParam Integer courseId,
            @RequestParam Integer dayId) {
        
        log.info("API: Marking day {} as completed for customer {} in course {}", 
                dayId, customerId, courseId);
        
        CustomerDayCompletion completion = completionService.markDayAsCompleted(customerId, courseId, dayId);
        return ResponseEntity.ok(completion);
    }
    
    /**
     * Đánh dấu một buổi học là chưa hoàn thành
     * POST /api/customer-progress/incomplete
     */
    @PostMapping("/incomplete")
    public ResponseEntity<CustomerDayCompletion> markDayAsIncomplete(
            @RequestParam Integer customerId,
            @RequestParam Integer courseId,
            @RequestParam Integer dayId) {
        
        log.info("API: Marking day {} as incomplete for customer {} in course {}", 
                dayId, customerId, courseId);
        
        CustomerDayCompletion completion = completionService.markDayAsIncomplete(customerId, courseId, dayId);
        return ResponseEntity.ok(completion);
    }
    
    /**
     * Kiểm tra trạng thái hoàn thành của một buổi học
     * GET /api/customer-progress/status
     */
    @GetMapping("/status")
    public ResponseEntity<Boolean> isDayCompleted(
            @RequestParam Integer customerId,
            @RequestParam Integer courseId,
            @RequestParam Integer dayId) {
        
        boolean isCompleted = completionService.isDayCompleted(customerId, courseId, dayId);
        return ResponseEntity.ok(isCompleted);
    }
    
    /**
     * Lấy tiến độ hoàn thành của customer trong một course
     * GET /api/customer-progress/course/{courseId}/customer/{customerId}
     */
    @GetMapping("/course/{courseId}/customer/{customerId}")
    public ResponseEntity<CourseProgressDTO> getCourseProgress(
            @PathVariable Integer customerId,
            @PathVariable Integer courseId) {
        
        log.info("API: Getting course progress for customer {} in course {}", customerId, courseId);
        
        CourseProgressDTO progress = completionService.getCourseProgress(customerId, courseId);
        return ResponseEntity.ok(progress);
    }
    
    /**
     * Lấy danh sách tất cả completion records của customer trong course
     * GET /api/customer-progress/course/{courseId}/customer/{customerId}/details
     */
    @GetMapping("/course/{courseId}/customer/{customerId}/details")
    public ResponseEntity<List<CustomerDayCompletion>> getCustomerCourseCompletions(
            @PathVariable Integer customerId,
            @PathVariable Integer courseId) {
        
        log.info("API: Getting completion details for customer {} in course {}", customerId, courseId);
        
        List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(customerId, courseId);
        return ResponseEntity.ok(completions);
    }
    
    /**
     * Khởi tạo completion records cho customer khi bắt đầu course
     * POST /api/customer-progress/initialize
     */
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeCompletionRecords(
            @RequestParam Integer customerId,
            @RequestParam Integer courseId) {
        
        log.info("API: Initializing completion records for customer {} in course {}", customerId, courseId);
        
        completionService.initializeCompletionRecords(customerId, courseId);
        return ResponseEntity.ok("Completion records initialized successfully");
    }
    
    /**
     * Xóa tất cả completion records của customer trong course
     * DELETE /api/customer-progress/course/{courseId}/customer/{customerId}
     */
    @DeleteMapping("/course/{courseId}/customer/{customerId}")
    public ResponseEntity<String> deleteCustomerCourseCompletions(
            @PathVariable Integer customerId,
            @PathVariable Integer courseId) {
        
        log.info("API: Deleting completion records for customer {} in course {}", customerId, courseId);
        
        completionService.deleteCustomerCourseCompletions(customerId, courseId);
        return ResponseEntity.ok("Completion records deleted successfully");
    }
    
    /**
     * Toggle completion status của một buổi học
     * PUT /api/customer-progress/toggle
     */
    @PutMapping("/toggle")
    public ResponseEntity<?> toggleDayCompletion(
            @RequestParam Integer customerId,
            @RequestParam Integer courseId,
            @RequestParam Integer dayId) {
        
        log.info("API: Toggling completion status for day {} of customer {} in course {}", 
                dayId, customerId, courseId);
        
        try {
            // Kiểm tra trạng thái hiện tại
            boolean currentStatus = completionService.isDayCompleted(customerId, courseId, dayId);
            
            CustomerDayCompletion completion;
            if (currentStatus) {
                completion = completionService.markDayAsIncomplete(customerId, courseId, dayId);
            } else {
                completion = completionService.markDayAsCompleted(customerId, courseId, dayId);
            }
            
            // Tạo response đơn giản để tránh lỗi serialization
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dayId", dayId);
            response.put("isCompleted", completion.getIsCompleted());
            response.put("completionDate", completion.getCompletionDate());
            response.put("message", "Điểm danh thành công");
            
            return ResponseEntity.ok(response);
            
        } catch (AttendanceException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Lấy tất cả completion records của customer (tất cả courses)
     * GET /api/customer-progress/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<CustomerDayCompletion>> getAllCustomerCompletions(
            @PathVariable Integer customerId) {
        
        log.info("API: Getting all completion records for customer {}", customerId);
        
        // Tạo method mới trong service nếu cần
        // List<CustomerDayCompletion> completions = completionService.getAllCustomerCompletions(customerId);
        // return ResponseEntity.ok(completions);
        
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/debug/customer/{customerId}")
    public ResponseEntity<?> debugCustomerCompletions(@PathVariable Integer customerId) {
        try {
            List<CustomerDayCompletion> completions = completionService.getCustomerCourseCompletions(customerId, 1);
            
            List<Map<String, Object>> debugInfo = new ArrayList<>();
            for (CustomerDayCompletion completion : completions) {
                Map<String, Object> info = new HashMap<>();
                info.put("dayId", completion.getDay().getDayId());
                info.put("isCompleted", completion.getIsCompleted());
                info.put("completionDate", completion.getCompletionDate());
                info.put("createdAt", completion.getCreatedAt());
                info.put("updatedAt", completion.getUpdatedAt());
                debugInfo.add(info);
            }
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
} 