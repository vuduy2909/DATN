package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.service.*;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/post-course-measurement")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class PostCourseMeasurementController {

    @Autowired
    private IUserDataAiService userDataAiService;
    
    @Autowired
    private ITrackDataAiService trackDataAiService;
    
    @Autowired
    private ICustomerRepository customerRepository;
    
    @Autowired
    private CustomerDayCompletionService completionService;

    /**
     * Lấy thông tin measurements ban đầu của user cho course đã hoàn thành
     */
    @GetMapping("/initial-data/{courseId}")
    public ResponseEntity<?> getInitialMeasurements(@PathVariable Integer courseId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // Kiểm tra xem course đã hoàn thành chưa
            boolean isCourseCompleted = completionService.isCourseFullyCompleted(customer.getCustomerId(), courseId);
            if (!isCourseCompleted) {
                return ResponseEntity.badRequest().body("Course not yet completed");
            }
            
            // Lấy user_data_ai record cho course này - tìm trong danh sách của customer
            List<UserDataAi> userDataList = userDataAiService.findByCustomerId(customer.getCustomerId());
            Optional<UserDataAi> userDataOpt = userDataList.stream()
                    .filter(data -> data.getCourse() != null && data.getCourse().getCourseId().equals(courseId))
                    .findFirst();
            
            if (!userDataOpt.isPresent()) {
                return ResponseEntity.badRequest().body("No initial measurements found for this course");
            }
            
            UserDataAi userData = userDataOpt.get();
            
            Map<String, Object> initialMeasurements = new HashMap<>();
            initialMeasurements.put("weight", userData.getWeight());
            initialMeasurements.put("waistCircumference", userData.getWaist_circumference());
            initialMeasurements.put("chestCircumference", userData.getChest_circumference());
            initialMeasurements.put("height", userData.getHeight());
            initialMeasurements.put("bmi", userData.getBmi());
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customer.getCustomerId());
            response.put("customerName", customer.getCustomerName());
            response.put("courseId", courseId);
            response.put("initialMeasurements", initialMeasurements);
            response.put("hasPostMeasurements", userData.getWeight_after() != null);
            response.put("courseCompleted", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting initial measurements: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving initial measurements: " + e.getMessage());
        }
    }

    /**
     * Cập nhật measurements sau khi hoàn thành course
     */
    @PostMapping("/submit")
    public ResponseEntity<?> submitPostCourseMeasurements(
            @RequestBody PostMeasurementRequest request, 
            Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // Kiểm tra xem course đã hoàn thành chưa
            boolean isCourseCompleted = completionService.isCourseFullyCompleted(customer.getCustomerId(), request.getCourseId());
            if (!isCourseCompleted) {
                return ResponseEntity.badRequest().body("Course not yet completed");
            }
            
            // Lấy user_data_ai record hiện tại
            List<UserDataAi> userDataList = userDataAiService.findByCustomerId(customer.getCustomerId());
            Optional<UserDataAi> userDataOpt = userDataList.stream()
                    .filter(data -> data.getCourse() != null && data.getCourse().getCourseId().equals(request.getCourseId()))
                    .findFirst();
            
            if (!userDataOpt.isPresent()) {
                return ResponseEntity.badRequest().body("No initial measurements found for this course");
            }
            
            UserDataAi userData = userDataOpt.get();
            
            // Kiểm tra xem đã submit post measurements chưa
            if (userData.getWeight_after() != null) {
                return ResponseEntity.badRequest().body("Post-course measurements already submitted");
            }
            
            // Tạo TrackDataAi record mới
            TrackDataAi trackData = new TrackDataAi();
            trackData.setAge(userData.getAge());
            trackData.setGender(userData.getGender());
            trackData.setHeight(userData.getHeight());
            trackData.setWeight(request.getWeightAfter());
            trackData.setWaist_circumference(request.getWaistCircumferenceAfter());
            trackData.setChest_circumference(request.getChestCircumferenceAfter());
            trackData.setBmi(calculateBMI(request.getWeightAfter(), userData.getHeight()));
            trackData.setCourse(userData.getCourse());
            
            // Copy other fields from UserDataAi
            trackData.setOccupation(userData.getOccupation());
            trackData.setWork_environment(userData.getWork_environment());
            trackData.setTraining_goals(userData.getTraining_goals() != null ? userData.getTraining_goals().toString() : null);
            trackData.setTraining_history(userData.getTraining_history());
            
            // Đánh giá hiệu quả
            boolean isEffective = evaluateEffectiveness(userData, request);
            trackData.setEffective(isEffective);
            
            // Lưu TrackDataAi record
            TrackDataAi savedTrackData = trackDataAiService.saveTrackDataAi(trackData);
            
            // Cập nhật UserDataAi với post measurements và track_data_ai_id
            userData.setWeight_after(request.getWeightAfter());
            userData.setWaist_circumference_after(request.getWaistCircumferenceAfter());
            userData.setChest_circumference_after(request.getChestCircumferenceAfter());
            userData.setTrack_data_ai_id(savedTrackData.getTrackDataAiId());
            userData.setEffective(isEffective);
            userData.setUpdated_date(LocalDateTime.now());
            
            userDataAiService.saveUserDataAi(userData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Post-course measurements submitted successfully");
            response.put("effective", isEffective);
            response.put("trackDataAiId", savedTrackData.getTrackDataAiId());
            response.put("improvementSummary", generateImprovementSummary(userData, request));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error submitting post-course measurements: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error submitting measurements: " + e.getMessage());
        }
    }

    /**
     * Đánh giá hiệu quả của course
     */
    private boolean evaluateEffectiveness(UserDataAi initial, PostMeasurementRequest after) {
        // Course được coi là hiệu quả nếu ít nhất 2/3 chỉ số được cải thiện (giảm)
        int improvedCount = 0;
        
        if (after.getWeightAfter() < initial.getWeight()) {
            improvedCount++;
        }
        
        if (after.getWaistCircumferenceAfter() < initial.getWaist_circumference()) {
            improvedCount++;
        }
        
        if (after.getChestCircumferenceAfter() < initial.getChest_circumference()) {
            improvedCount++;
        }
        
        return improvedCount >= 2; // Ít nhất 2/3 chỉ số cải thiện
    }
    
    /**
     * Tính BMI
     */
    private Double calculateBMI(Double weight, Double height) {
        if (weight == null || height == null || height <= 0) {
            return null;
        }
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }
    
    /**
     * Tạo summary về sự cải thiện
     */
    private Map<String, Object> generateImprovementSummary(UserDataAi initial, PostMeasurementRequest after) {
        Map<String, Object> summary = new HashMap<>();
        
        double weightChange = after.getWeightAfter() - initial.getWeight();
        double waistChange = after.getWaistCircumferenceAfter() - initial.getWaist_circumference();
        double chestChange = after.getChestCircumferenceAfter() - initial.getChest_circumference();
        
        summary.put("weightChange", weightChange);
        summary.put("waistChange", waistChange);
        summary.put("chestChange", chestChange);
        
        summary.put("weightImproved", weightChange < 0);
        summary.put("waistImproved", waistChange < 0);
        summary.put("chestImproved", chestChange < 0);
        
        return summary;
    }

    /**
     * DTO for post-course measurement request
     */
    public static class PostMeasurementRequest {
        private Integer courseId;
        private Double weightAfter;
        private Double waistCircumferenceAfter;
        private Double chestCircumferenceAfter;
        
        // Getters and setters
        public Integer getCourseId() { return courseId; }
        public void setCourseId(Integer courseId) { this.courseId = courseId; }
        
        public Double getWeightAfter() { return weightAfter; }
        public void setWeightAfter(Double weightAfter) { this.weightAfter = weightAfter; }
        
        public Double getWaistCircumferenceAfter() { return waistCircumferenceAfter; }
        public void setWaistCircumferenceAfter(Double waistCircumferenceAfter) { this.waistCircumferenceAfter = waistCircumferenceAfter; }
        
        public Double getChestCircumferenceAfter() { return chestCircumferenceAfter; }
        public void setChestCircumferenceAfter(Double chestCircumferenceAfter) { this.chestCircumferenceAfter = chestCircumferenceAfter; }
    }
} 