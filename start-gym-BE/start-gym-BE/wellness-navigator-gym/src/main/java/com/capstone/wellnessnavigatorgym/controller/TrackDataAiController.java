package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.*;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/v1/track-data-ai")
@CrossOrigin(origins = "http://localhost:3000")
public class TrackDataAiController {

    private static final Logger logger = LoggerFactory.getLogger(TrackDataAiController.class);
    private static final int MAX_RETRIES = 5;  // Increased from 3 to 5
    private static final int INITIAL_BACKOFF_MS = 2000;  // Increased initial backoff to 2 seconds
    private static final double BACKOFF_MULTIPLIER = 2.0;
    private static final int MAX_BACKOFF_MS = 32000;  // 32 seconds maximum backoff
    private static final Object claudeApiLock = new Object();
    private static long lastCallTimestamp = 0;
    private static final long MIN_CALL_INTERVAL_MS = 3000;  // Increased to 3 seconds minimum between calls

    @Autowired
    private ITrackDataAiService trackDataAiService;

    @Autowired
    private ICustomerCourseService customerCourseService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private IUserDataAiService userDataAiService;

    @Autowired
    private ICourseService courseService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${claude.api.key:sk-ant-api03-G47aLO10DvYoxHuaEnCRL4y_gGTevFEQPLvkHU81DYujJ81lKUMuX28cyJIiD5_z1KpuDpiLzg1QLppX17F9Q-TMiqkgAA}")
    private String claudeApiKey;

    @Value("${claude.api.url:https://api.anthropic.com/v1/messages}")
    private String claudeApiUrl;

    @Value("${claude.api.version:2023-06-01}")
    private String claudeApiVersion;

    @Value("${claude.api.model:claude-3-5-haiku-20240307}")
    private String claudeModel;

    // Đường dẫn file XML - Sửa lại đường dẫn nhất quán
    private static final String XML_FILE_PATH = "D:/DATN/CODE/start-gym-BE/start-gym-BE/wellness-navigator-gym/src/main/resources/static/DataTraining.xml";

    // Simple DTO classes to replace missing ones
    public static class RecommendationDTO {
        private Course recommendedCourses;
        private String message;
        private Integer userDataId;
        private UserDataDTO formData; // Thêm field để chứa form data
        
        public RecommendationDTO(Course recommendedCourses, String message) {
            this.recommendedCourses = recommendedCourses;
            this.message = message;
        }
        
        public RecommendationDTO(Course recommendedCourses, String message, Integer userDataId) {
            this.recommendedCourses = recommendedCourses;
            this.message = message;
            this.userDataId = userDataId;
        }
        
        public Course getRecommendedCourses() { return recommendedCourses; }
        public String getMessage() { return message; }
        public Integer getUserDataId() { return userDataId; }
        public UserDataDTO getFormData() { return formData; }
        public void setFormData(UserDataDTO formData) { this.formData = formData; }
    }
    
    public static class UserFormDataDTO {
        private String training_history;
        private String training_frequency;
        private Integer age;
        private String gender;
        private Double height;
        private Double weight;
        private Double bmi;  // Add BMI field
        private Double chest_circumference;
        private Double waist_circumference;
        private String training_goals;
        private String body_type;
        private String occupation;
        private String work_environment;
        private Integer customerId;
        
        // Getters
        public String getTraining_history() { return training_history; }
        public String getTraining_frequency() { return training_frequency; }
        public Integer getAge() { return age; }
        public String getGender() { return gender; }
        public Double getHeight() { return height; }
        public Double getWeight() { return weight; }
        public Double getBmi() { return bmi; }  // Add BMI getter
        public Double getChest_circumference() { return chest_circumference; }
        public Double getWaist_circumference() { return waist_circumference; }
        public String getTraining_goals() { return training_goals; }
        public String getBody_type() { return body_type; }
        public String getOccupation() { return occupation; }
        public String getWork_environment() { return work_environment; }
        public Integer getCustomerId() { return customerId; }
        
        // Setters
        public void setTraining_history(String training_history) { this.training_history = training_history; }
        public void setTraining_frequency(String training_frequency) { this.training_frequency = training_frequency; }
        public void setAge(Integer age) { this.age = age; }
        public void setGender(String gender) { this.gender = gender; }
        public void setHeight(Double height) { this.height = height; }
        public void setWeight(Double weight) { this.weight = weight; }
        public void setBmi(Double bmi) { this.bmi = bmi; }
        public void setChest_circumference(Double chest_circumference) { this.chest_circumference = chest_circumference; }
        public void setWaist_circumference(Double waist_circumference) { this.waist_circumference = waist_circumference; }
        public void setTraining_goals(String training_goals) { this.training_goals = training_goals; }
        public void setBody_type(String body_type) { this.body_type = body_type; }
        public void setOccupation(String occupation) { this.occupation = occupation; }
        public void setWork_environment(String work_environment) { this.work_environment = work_environment; }
        public void setCustomerId(Integer customerId) { this.customerId = customerId; }
    }
    
    public static class UserDataDTO {
        private String training_history;
        private String training_frequency;
        private Integer age;
        private String gender;
        private Double height;
        private Double weight;
        private Double bmi;
        private Double chest_circumference;
        private Double waist_circumference;
        private String training_goals;
        private String body_type;
        private String occupation;
        private String work_environment;
        private Integer customerId;
        private Integer recommendedCourseId;
        
        // Getters and setters
        public String getTraining_history() { return training_history; }
        public void setTraining_history(String training_history) { this.training_history = training_history; }
        public String getTraining_frequency() { return training_frequency; }
        public void setTraining_frequency(String training_frequency) { this.training_frequency = training_frequency; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public Double getHeight() { return height; }
        public void setHeight(Double height) { this.height = height; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public Double getBmi() { return bmi; }
        public void setBmi(Double bmi) { this.bmi = bmi; }
        public Double getChest_circumference() { return chest_circumference; }
        public void setChest_circumference(Double chest_circumference) { this.chest_circumference = chest_circumference; }
        public Double getWaist_circumference() { return waist_circumference; }
        public void setWaist_circumference(Double waist_circumference) { this.waist_circumference = waist_circumference; }
        public String getTraining_goals() { return training_goals; }
        public void setTraining_goals(String training_goals) { this.training_goals = training_goals; }
        public String getBody_type() { return body_type; }
        public void setBody_type(String body_type) { this.body_type = body_type; }
        public String getOccupation() { return occupation; }
        public void setOccupation(String occupation) { this.occupation = occupation; }
        public String getWork_environment() { return work_environment; }
        public void setWork_environment(String work_environment) { this.work_environment = work_environment; }
        public Integer getCustomerId() { return customerId; }
        public void setCustomerId(Integer customerId) { this.customerId = customerId; }
        public Integer getRecommendedCourseId() { return recommendedCourseId; }
        public void setRecommendedCourseId(Integer recommendedCourseId) { this.recommendedCourseId = recommendedCourseId; }
    }
    
    public static class ImageAnalysisResponseDTO {
        private String bodyType;
        private String analysis;
        private String recommendations;
        private Integer userDataId;
        
        public ImageAnalysisResponseDTO() {}
        
        public ImageAnalysisResponseDTO(String bodyType, String analysis, String recommendations) {
            this.bodyType = bodyType;
            this.analysis = analysis;
            this.recommendations = recommendations;
        }
        
        public String getBodyType() { return bodyType; }
        public void setBodyType(String bodyType) { this.bodyType = bodyType; }
        public String getAnalysis() { return analysis; }
        public void setAnalysis(String analysis) { this.analysis = analysis; }
        public String getRecommendations() { return recommendations; }
        public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
        public Integer getUserDataId() { return userDataId; }
        public void setUserDataId(Integer userDataId) { this.userDataId = userDataId; }
    }
    
    public static class EffectivenessUpdateDTO {
        private Boolean effective;
        
        public Boolean getEffective() { return effective; }
        public void setEffective(Boolean effective) { this.effective = effective; }
    }

    public static class PostCourseUpdateDTO {
        private Double weight_after;           // Cân nặng sau khi tập
        private Double chest_circumference_after;  // Vòng ngực sau khi tập
        private Double waist_circumference_after;  // Vòng eo sau khi tập
        
        public Double getWeight_after() { return weight_after; }
        public void setWeight_after(Double weight_after) { this.weight_after = weight_after; }
        public Double getChest_circumference_after() { return chest_circumference_after; }
        public void setChest_circumference_after(Double chest_circumference_after) { this.chest_circumference_after = chest_circumference_after; }
        public Double getWaist_circumference_after() { return waist_circumference_after; }
        public void setWaist_circumference_after(Double waist_circumference_after) { this.waist_circumference_after = waist_circumference_after; }
    }

    // DTO cho việc lưu tạm dữ liệu form wizard
    public static class FormWizardDraftDTO {
        private Integer customerId;
        private Integer currentStep;
        
        // Step 1: Thông tin cơ bản
        private Integer age;
        private String gender;
        private Double height;
        private Double weight;
        private Double bmi;
        
        // Step 2: Đo lường cơ thể
        private Double chest_circumference;
        private Double waist_circumference;
        
        // Step 3: Mục tiêu tập luyện
        private String training_goals;
        
        // Step 4: Lịch sử tập luyện và tần suất
        private String training_history;
        private String training_frequency;
        
        // Step 5: Thông tin nghề nghiệp
        private String occupation;
        private String work_environment;
        
        // Step 6: Phân tích hình ảnh
        private String body_type;
        private String imageAnalysis;
        private String imageRecommendations;
        private String imageBase64; // Lưu ảnh dưới dạng base64 để hiển thị lại
        
        // Step 7: Khóa học được đề xuất
        private Integer recommendedCourseId;
        private String recommendedCourseName;
        
        // Metadata
        private String createdAt;
        private String updatedAt;
        
        // Constructors
        public FormWizardDraftDTO() {}
        
        // Getters and Setters
        public Integer getCustomerId() { return customerId; }
        public void setCustomerId(Integer customerId) { this.customerId = customerId; }
        public Integer getCurrentStep() { return currentStep; }
        public void setCurrentStep(Integer currentStep) { this.currentStep = currentStep; }
        
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public Double getHeight() { return height; }
        public void setHeight(Double height) { this.height = height; }
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        public Double getBmi() { return bmi; }
        public void setBmi(Double bmi) { this.bmi = bmi; }
        
        public Double getChest_circumference() { return chest_circumference; }
        public void setChest_circumference(Double chest_circumference) { this.chest_circumference = chest_circumference; }
        public Double getWaist_circumference() { return waist_circumference; }
        public void setWaist_circumference(Double waist_circumference) { this.waist_circumference = waist_circumference; }
        
        public String getTraining_goals() { return training_goals; }
        public void setTraining_goals(String training_goals) { this.training_goals = training_goals; }
        
        public String getTraining_history() { return training_history; }
        public void setTraining_history(String training_history) { this.training_history = training_history; }
        public String getTraining_frequency() { return training_frequency; }
        public void setTraining_frequency(String training_frequency) { this.training_frequency = training_frequency; }
        
        public String getOccupation() { return occupation; }
        public void setOccupation(String occupation) { this.occupation = occupation; }
        public String getWork_environment() { return work_environment; }
        public void setWork_environment(String work_environment) { this.work_environment = work_environment; }
        
        public String getBody_type() { return body_type; }
        public void setBody_type(String body_type) { this.body_type = body_type; }
        public String getImageAnalysis() { return imageAnalysis; }
        public void setImageAnalysis(String imageAnalysis) { this.imageAnalysis = imageAnalysis; }
        public String getImageRecommendations() { return imageRecommendations; }
        public void setImageRecommendations(String imageRecommendations) { this.imageRecommendations = imageRecommendations; }
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
        
        public Integer getRecommendedCourseId() { return recommendedCourseId; }
        public void setRecommendedCourseId(Integer recommendedCourseId) { this.recommendedCourseId = recommendedCourseId; }
        public String getRecommendedCourseName() { return recommendedCourseName; }
        public void setRecommendedCourseName(String recommendedCourseName) { this.recommendedCourseName = recommendedCourseName; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    // In-memory storage cho draft data (có thể thay bằng database sau)
    private static final Map<Integer, FormWizardDraftDTO> draftStorage = new HashMap<>();

    @GetMapping("/{id}")
    public ResponseEntity<String> getTrackDataAiById(@PathVariable Integer id) {
        return new ResponseEntity<>("Track data retrieved", HttpStatus.OK);
    }

    @PostMapping("/process-form-data")
    public ResponseEntity<RecommendationDTO> processFormData(@RequestBody UserFormDataDTO formData) {
        try {
            logger.info("Received form data: training_frequency='{}', training_history='{}'", 
                formData.getTraining_frequency(), formData.getTraining_history());
            
            // Validate enum values before processing
            validateEnumValues(formData);
            
            // Chuyển đổi dữ liệu từ form sang UserDataDTO để xử lý với hệ thống hiện tại
            UserDataDTO userDataDTO = convertToUserDataDTO(formData);

            // Tính BMI nếu chưa có
            if (userDataDTO.getBmi() == null && userDataDTO.getHeight() != null && userDataDTO.getWeight() != null) {
                double heightInMeters = userDataDTO.getHeight() / 100.0;
                double bmi = userDataDTO.getWeight() / (heightInMeters * heightInMeters);
                userDataDTO.setBmi(Math.round(bmi * 100.0) / 100.0); // Làm tròn đến 2 chữ số thập phân
            }

            Customer customer = customerService.findById(userDataDTO.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "Customer not found"));
            }

            // For now, assign a default course until decision tree is available
            List<Course> defaultCourses = courseService.findAll();
            Course recommendations = null;
            if (!defaultCourses.isEmpty()) {
                recommendations = defaultCourses.get(0);
                logger.info("Using first available course as default: {}", recommendations.getCourseId());
            } else {
                return ResponseEntity.ok(new RecommendationDTO(null, "No courses available in system"));
            }

            // Set the recommended course_id on the UserDataDTO
            userDataDTO.setRecommendedCourseId(recommendations.getCourseId());
            
            // *** KHÔNG LƯU VÀO DATABASE Ở ĐÂY ***
            // Chỉ tạo một temporary userDataId để frontend có thể track
            // Sẽ lưu thật sự khi user xác nhận ở Step 7
            
            logger.info("Generated recommendation for course " + recommendations.getCourseId() + " for user " + userDataDTO.getCustomerId() + " (not saved to DB yet)");

            // Tạo một RecommendationDTO đặc biệt với formData để Step7 có thể hiển thị đầy đủ thông tin
            RecommendationDTO response = new RecommendationDTO(recommendations, "Course recommendations generated successfully");
            
            // Thêm formData vào response để Step7 có thể hiển thị
            response.setFormData(userDataDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing form data: " + e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationDTO(null, "Error processing form data: " + e.getMessage()));
        }
    }
    
    private void validateEnumValues(UserFormDataDTO formData) {
        // Validate training frequency
        String trainingFrequency = formData.getTraining_frequency();
        if (trainingFrequency != null) {
            trainingFrequency = trainingFrequency.trim();
            logger.info("Validating training_frequency: '{}'", trainingFrequency);
            if (!isValidTrainingFrequency(trainingFrequency)) {
                logger.warn("Invalid training_frequency value: '{}', will use default", trainingFrequency);
            }
        }
        
        // Validate training history
        String trainingHistory = formData.getTraining_history();
        if (trainingHistory != null) {
            trainingHistory = trainingHistory.trim();
            logger.info("Validating training_history: '{}'", trainingHistory);
            if (!isValidTrainingHistory(trainingHistory)) {
                logger.warn("Invalid training_history value: '{}', will use default", trainingHistory);
            }
        }
        
        // Log all enum values for debugging
        logger.info("Available TrainingFrequency values: {}", Arrays.toString(TrainingFrequency.values()));
        logger.info("Available TrainingHistory values: {}", Arrays.toString(TrainingHistory.values()));
    }
    
    private boolean isValidTrainingFrequency(String value) {
        try {
            TrainingFrequency.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    private boolean isValidTrainingHistory(String value) {
        try {
            TrainingHistory.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private UserDataAi createUserDataAiFromDTO(UserDataDTO dto, Customer customer) {
        UserDataAi userDataAi = new UserDataAi();
        userDataAi.setTrack_data_ai_id(1001);
        userDataAi.setAge(dto.getAge());
        userDataAi.setGender(dto.getGender());
        userDataAi.setBmi(dto.getBmi());
        
        // Convert and set training_goals enum
        final String trainingGoals = dto.getTraining_goals() != null && !dto.getTraining_goals().trim().isEmpty() 
            ? dto.getTraining_goals() : "7, Weight Loss";
        
        try {
            // Use the new fromDatabaseValue method for proper mapping
            TrainingGoals goalEnum = TrainingGoals.fromDatabaseValue(trainingGoals.trim());
            userDataAi.setTraining_goals(goalEnum);
            logger.info("Set training goal enum: {} (database value: {})", goalEnum.name(), goalEnum.getDatabaseValue());
        } catch (Exception e) {
            logger.warn("Failed to map training goal '{}', using default", trainingGoals);
            userDataAi.setTraining_goals(TrainingGoals.WEIGHT_LOSS);
        }
        
        // Convert and set training_history as String (not enum in user_data_ai table)
        final String trainingHistory = dto.getTraining_history() != null && !dto.getTraining_history().trim().isEmpty() 
            ? dto.getTraining_history().trim() : "Never";
        
        logger.info("Processing training_history: '{}'", trainingHistory);
        userDataAi.setTraining_history(trainingHistory); // Direct string assignment
        logger.info("Set training history string: '{}'", trainingHistory);
        
        // Convert and set training_frequency using standard TrainingFrequency (now synchronized)
        final String trainingFrequency = dto.getTraining_frequency() != null && !dto.getTraining_frequency().trim().isEmpty() 
            ? dto.getTraining_frequency().trim() : "Moderate";
        
        logger.info("Processing training_frequency: '{}'", trainingFrequency);
        try {
            // Use standard TrainingFrequency enum (database now synchronized)
            TrainingFrequency frequencyEnum = TrainingFrequency.valueOf(trainingFrequency);
            userDataAi.setTraining_frequency(frequencyEnum);
            logger.info("Successfully set training frequency enum: {} (name: {})", frequencyEnum.name(), frequencyEnum);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid training frequency '{}', using default Moderate. Error: {}", trainingFrequency, e.getMessage());
            userDataAi.setTraining_frequency(TrainingFrequency.Moderate);
        }

        // Set physical measurements
        userDataAi.setHeight(dto.getHeight());
        userDataAi.setWeight(dto.getWeight());
        userDataAi.setChest_circumference(dto.getChest_circumference());
        userDataAi.setWaist_circumference(dto.getWaist_circumference());
        
        // Convert and set body_type using new BodyType enum
        String bodyType = dto.getBody_type();
        if (bodyType == null || bodyType.trim().isEmpty()) {
            bodyType = "Normal"; // Default body type
            logger.info("Using default body type: Normal");
        }
        
        logger.info("=== BODY TYPE ENUM CONVERSION ===");
        logger.info("Body type from DTO: '{}'", bodyType);
        
        try {
            // Sử dụng fromDatabaseValue để convert
            BodyType bodyTypeEnum = BodyType.fromDatabaseValue(bodyType.trim());
            userDataAi.setBody_type(bodyTypeEnum);
            logger.info("✅ Successfully converted body type '{}' to enum: {} (database value: {})", 
                bodyType, bodyTypeEnum.name(), bodyTypeEnum.getDatabaseValue());
        } catch (Exception e) {
            logger.warn("⚠️ Failed to map body type '{}', using default Normal. Error: {}", bodyType, e.getMessage());
            userDataAi.setBody_type(BodyType.NORMAL);
        }
        
        logger.info("=== END BODY TYPE ENUM CONVERSION ===");
        
        // Set occupation as String (not enum in user_data_ai table)
        final String occupation = dto.getOccupation() != null && !dto.getOccupation().trim().isEmpty() 
            ? dto.getOccupation() : "Student";
        userDataAi.setOccupation(occupation);
        logger.info("Set occupation string: '{}'", occupation);
        
        // Set work_environment as String (not enum in user_data_ai table)
        final String workEnvironment = dto.getWork_environment() != null && !dto.getWork_environment().trim().isEmpty() 
            ? dto.getWork_environment() : "Office";
        userDataAi.setWork_environment(workEnvironment);
        logger.info("Set work environment string: '{}'", workEnvironment);
        
        userDataAi.setCustomer(customer);
        
        // Save the recommended course if available
        if (dto.getRecommendedCourseId() != null) {
            Course course = new Course(dto.getRecommendedCourseId());
            userDataAi.setCourse(course);
            logger.info("Set recommended course_id: {} for user_data_ai", dto.getRecommendedCourseId());
        }

        return userDataAi;
    }

    private UserDataDTO convertToUserDataDTO(UserFormDataDTO formData) {
        UserDataDTO dto = new UserDataDTO();

        // Mapping các trường dữ liệu - trường đã trùng tên nên chỉ cần copy
        dto.setTraining_history(formData.getTraining_history()); 
        dto.setTraining_frequency(formData.getTraining_frequency());
        dto.setAge(formData.getAge());
        dto.setGender(formData.getGender());
        dto.setHeight(formData.getHeight());
        dto.setWeight(formData.getWeight());

        // Use BMI from form data if available, otherwise calculate it
        if (formData.getBmi() != null) {
            dto.setBmi(formData.getBmi());
        } else if (formData.getHeight() != null && formData.getWeight() != null) {
            // Tính BMI nếu không có trong form data
            double heightInMeters = formData.getHeight() / 100.0;
            double bmi = formData.getWeight() / (heightInMeters * heightInMeters);
            dto.setBmi(Math.round(bmi * 100.0) / 100.0); // Làm tròn đến 2 chữ số thập phân
        }

        // Số đo cơ thể
        dto.setChest_circumference(formData.getChest_circumference());
        dto.setWaist_circumference(formData.getWaist_circumference());

        dto.setTraining_goals(formData.getTraining_goals());
        
        // *** XỬ LÝ BODY TYPE: ƯU TIÊN CLAUDE API ***
        String bodyTypeFromInput = formData.getBody_type();
        
        logger.info("=== BODY TYPE PROCESSING ===");
        logger.info("Body type received from form: '{}'", bodyTypeFromInput);
        
        // Kiểm tra xem có phải là kết quả từ Claude API không
        // Claude API thường trả về: "Thin", "Skinny Fat", "Overweight", "Normal", "Fit"
        String[] claudeBodyTypes = {"Thin", "Skinny Fat", "Overweight", "Normal", "Fit"};
        boolean isFromClaude = false;
        
        if (bodyTypeFromInput != null && !bodyTypeFromInput.trim().isEmpty()) {
            String cleanBodyType = bodyTypeFromInput.trim();
            
            // Kiểm tra xem có phải từ Claude API không
            for (String claudeType : claudeBodyTypes) {
                if (claudeType.equalsIgnoreCase(cleanBodyType)) {
                    isFromClaude = true;
                    bodyTypeFromInput = claudeType; // Chuẩn hóa case
                    break;
                }
            }
        }
        
        if (isFromClaude) {
            logger.info("✅ Body type '{}' is from Claude API analysis - using this value", bodyTypeFromInput);
            dto.setBody_type(bodyTypeFromInput);
        } else if (bodyTypeFromInput != null && !bodyTypeFromInput.trim().isEmpty()) {
            logger.info("⚠️ Body type '{}' appears to be manual input - using this value", bodyTypeFromInput);
            dto.setBody_type(bodyTypeFromInput);
        } else {
            logger.info("❌ No body type provided - using default 'Normal'");
            dto.setBody_type("Normal");
        }
        
        logger.info("Final body type set: '{}'", dto.getBody_type());
        logger.info("=== END BODY TYPE PROCESSING ===");
        
        dto.setOccupation(formData.getOccupation());
        dto.setWork_environment(formData.getWork_environment());

        dto.setCustomerId(formData.getCustomerId());

        return dto;
    }

    @PostMapping("/recommend-course")
    public ResponseEntity<RecommendationDTO> recommendCourse(@RequestBody UserDataDTO userDataDTO) {
        try {
            logger.info("Received recommendation request for user ID: " + userDataDTO.getCustomerId());
            
            Customer customer = customerService.findById(userDataDTO.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "Customer not found"));
            }

            // For now, assign a default course until decision tree is available
            List<Course> defaultCourses = courseService.findAll();
            Course recommendations = null;
            if (!defaultCourses.isEmpty()) {
                recommendations = defaultCourses.get(0);
                logger.info("Using first available course as default: {}", recommendations.getCourseId());
            } else {
                return ResponseEntity.ok(new RecommendationDTO(null, "No courses available in system"));
            }

            // Set the recommended course_id on the UserDataDTO before saving
            userDataDTO.setRecommendedCourseId(recommendations.getCourseId());
            
            // Lưu dữ liệu người dùng với effective=false (chưa được xác nhận)
            UserDataAi userDataAi = createUserDataAiFromDTO(userDataDTO, customer);
            userDataAi.setEffective(false); // Chưa được xác nhận
            userDataAi = userDataAiService.saveUserDataAi(userDataAi);

            // Không lưu khóa học đề xuất vào CustomerCourse ngay lập tức, 
            // mà sẽ đợi người dùng xác nhận ở bước tiếp theo
            logger.info("Pending recommended course " + recommendations.getCourseId() + " for user " + userDataDTO.getCustomerId());

            return ResponseEntity.ok(new RecommendationDTO(
                recommendations, 
                "Course recommendations generated successfully",
                userDataAi.getUser_data_id()
            ));
        } catch (Exception e) {
            logger.error("Error in recommendation process: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RecommendationDTO(null, "Error in recommendation process: " + e.getMessage()));
        }
    }

    private Map<String, Object> extractUserDataFromTrackDataAi(UserDataDTO userDataDTO) {
        Map<String, Object> userData = new HashMap<>();
        // Các trường cơ bản
        userData.put("age", userDataDTO.getAge());
        userData.put("gender", userDataDTO.getGender());
        userData.put("bmi", userDataDTO.getBmi());
        userData.put("training_goals", userDataDTO.getTraining_goals());
        userData.put("training_history", userDataDTO.getTraining_history());
        
        // Các trường mở rộng - đảm bảo tên trường nhất quán với BuildDecisionTree
        if (userDataDTO.getHeight() != null) userData.put("height", userDataDTO.getHeight());
        if (userDataDTO.getWeight() != null) userData.put("weight", userDataDTO.getWeight());
        if (userDataDTO.getWaist_circumference() != null) userData.put("waist_circumference", userDataDTO.getWaist_circumference());
        if (userDataDTO.getChest_circumference() != null) userData.put("chest_circumference", userDataDTO.getChest_circumference());
        if (userDataDTO.getBody_type() != null) userData.put("body_type", userDataDTO.getBody_type());
        if (userDataDTO.getOccupation() != null) userData.put("occupation", userDataDTO.getOccupation());
        if (userDataDTO.getWork_environment() != null) userData.put("work_environment", userDataDTO.getWork_environment());
        
        return userData;
    }

    private List<String> getAttributeNames() {
        // Lấy tất cả các thuộc tính, sau đó lọc chỉ giữ lại thuộc tính hợp lệ
        return Arrays.asList(
            "age",
            "gender", 
            "bmi",
            "training_goals",
            "training_history",
            "height",
            "weight",
            "waist_circumference",
            "chest_circumference",
            "body_type",
            "occupation",
            "work_environment"
        );
    }

    @PutMapping("/update-effectiveness/{userDataAiId}")
    public ResponseEntity<?> updateUserDataAiEffectiveness(@PathVariable Integer userDataAiId, @RequestBody EffectivenessUpdateDTO dto) {
        UserDataAi userDataAi = userDataAiService.findUserDataAiById(userDataAiId);
        userDataAi.setEffective(dto.getEffective());
        userDataAiService.saveUserDataAi(userDataAi);
        return ResponseEntity.ok("Effectiveness updated successfully for UserDataAi ID " + userDataAiId);
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<?> uploadCSV(@RequestParam("file") MultipartFile file,
                                       @RequestParam("customerId") Integer customerId) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please select a CSV file to upload");
        }

        try {
            // Lấy thông tin khách hàng
            Customer customer = customerService.findById(customerId);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found with ID: " + customerId);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            List<UserDataAi> savedEntities = new ArrayList<>();
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                // Bỏ qua dòng header
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 8) { // Đảm bảo có đủ dữ liệu cần thiết
                    continue;
                }

                UserDataAi userDataAi = new UserDataAi();

                // Mapping các trường từ file CSV vào đối tượng UserDataAi
                userDataAi.setAge(Integer.parseInt(data[1].trim()));
                userDataAi.setGender(data[2].trim());
                userDataAi.setHeight(Double.parseDouble(data[3].trim()));
                userDataAi.setWeight(Double.parseDouble(data[4].trim()));

                // Tính BMI
                Double height = userDataAi.getHeight() / 100; // Chuyển từ cm sang m
                Double bmi = userDataAi.getWeight() / (height * height);
                userDataAi.setBmi(bmi);

                // Convert String values to correct types for CSV upload
                try {
                    TrainingGoals goalEnum = TrainingGoals.fromDatabaseValue(data[5].trim());
                    userDataAi.setTraining_goals(goalEnum);
                } catch (Exception e) {
                    userDataAi.setTraining_goals(TrainingGoals.WEIGHT_LOSS);
                }

                // Set training_history as String (not enum in user_data_ai table)
                userDataAi.setTraining_history(data[6].trim());

                // Set body_type using BodyType enum with converter
                try {
                    BodyType bodyTypeEnum = BodyType.fromDatabaseValue(data[7].trim());
                    userDataAi.setBody_type(bodyTypeEnum);
                } catch (Exception e) {
                    userDataAi.setBody_type(BodyType.NORMAL);
                }

                if (data.length > 8) {
                    // Set occupation as String (not enum in user_data_ai table)
                    userDataAi.setOccupation(data[8].trim());
                }
                
                if (data.length > 9) {
                    // Set work_environment as String (not enum in user_data_ai table)
                    userDataAi.setWork_environment(data[9].trim());
                }

                userDataAi.setEffective(true); // Mặc định là hiệu quả
                userDataAi.setCustomer(customer);

                // Lưu vào database
                UserDataAi saved = userDataAiService.saveUserDataAi(userDataAi);
                savedEntities.add(saved);
            }

            return ResponseEntity.ok("Successfully imported " + savedEntities.size() + " records from CSV");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload CSV file: " + e.getMessage());
        }
    }

    @PostMapping("/analyze-image")
    public ResponseEntity<ImageAnalysisResponseDTO> analyzeImage(@RequestParam("image") MultipartFile image,
                                                                 @RequestParam("height") Double height,
                                                                 @RequestParam("weight") Double weight,
                                                                 @RequestParam("gender") String gender,
                                                                 @RequestParam(value = "customerId", required = false) Integer customerId) {
        try {
            logger.info("Starting image analysis - height: {}, weight: {}, gender: {}, image name: {}, size: {}, customerId: {}",
                    height, weight, gender, image.getOriginalFilename(), image.getSize(), customerId);

            // Kiểm tra nếu ảnh không có hoặc rỗng
            if (image == null || image.isEmpty() || image.getSize() == 0) {
                logger.error("No image or empty image provided");
                return ResponseEntity.badRequest()
                        .body(new ImageAnalysisResponseDTO("Unknown",
                                "Không có ảnh hoặc ảnh rỗng",
                                "Vui lòng tải lên ảnh rõ nét để nhận được phân tích chi tiết nhất."));
            }

            // Gọi Claude API với phương thức chính
            String claudeResponse = callClaudeApi(image, height, weight, gender);

            // Nếu không nhận được phản hồi hoặc có lỗi
            if (claudeResponse == null || 
                claudeResponse.contains("Lỗi kết nối") ||
                claudeResponse.contains("Lỗi xác thực") ||
                claudeResponse.contains("Vượt quá giới hạn") ||
                claudeResponse.contains("Overloaded")) {
                
                String errorMessage = "Hệ thống AI đang quá tải. Vui lòng thử lại sau 1-2 phút.";
                logger.warn("Claude API service error: {}", errorMessage);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ImageAnalysisResponseDTO("Unknown",
                                errorMessage,
                                "Để có kết quả phân tích tốt nhất:\n" +
                                "1. Sử dụng ảnh toàn thân hoặc nửa người\n" +
                                "2. Ảnh rõ nét, đủ sáng\n" +
                                "3. Trang phục gọn gàng để thấy rõ hình dáng cơ thể\n" +
                                "4. Tránh ảnh có khuôn mặt để được phân tích nhanh hơn"));
            }

            // Parse kết quả từ Claude
            ImageAnalysisResponseDTO response = parseClaudeResponse(claudeResponse);
            logger.info("Successfully analyzed image. Body type: {}", response.getBodyType());

            // Nếu có customerId, lưu kết quả và đề xuất khóa học
            Integer userDataId = null;
            if (customerId != null) {
                try {
                    // Tạo UserDataDTO từ kết quả phân tích
                    UserDataDTO userDataDTO = new UserDataDTO();
                    userDataDTO.setHeight(height);
                    userDataDTO.setWeight(weight);
                    userDataDTO.setGender(gender);
                    userDataDTO.setCustomerId(customerId);
                    
                    // Tính BMI
                    double heightInMeters = height / 100.0;
                    double bmi = weight / (heightInMeters * heightInMeters);
                    userDataDTO.setBmi(Math.round(bmi * 100.0) / 100.0);
                    
                    // Sử dụng body_type được phát hiện từ Claude
                    userDataDTO.setBody_type(response.getBodyType());
                    
                    // Thiết lập các giá trị mặc định
                    userDataDTO.setAge(30);
                    userDataDTO.setTraining_goals("Weight Management");
                    userDataDTO.setTraining_history("Beginner");
                    
                    // Lưu dữ liệu và đề xuất khóa học
                    logger.info("Attempting to recommend courses based on image analysis for customer ID: {}", customerId);
                    RecommendationDTO recommendation = recommendCourse(userDataDTO).getBody();
                    
                    if (recommendation != null) {
                        if (recommendation.getRecommendedCourses() != null) {
                            logger.info("Successfully recommended course: {}", recommendation.getRecommendedCourses().getCourseName());
                        }
                        userDataId = recommendation.getUserDataId();
                        response.setUserDataId(userDataId);
                        logger.info("Setting userDataId: {} in image analysis response", userDataId);
                    } else {
                        logger.warn("No course was recommended based on image analysis");
                    }
                } catch (Exception e) {
                    logger.error("Error recommending courses after image analysis: {}", e.getMessage(), e);
                }
            }
                
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to analyze image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageAnalysisResponseDTO("Unknown",
                            "Không thể phân tích hình ảnh: " + e.getMessage(),
                            "Để có kết quả phân tích tốt nhất:\n" +
                            "1. Sử dụng ảnh toàn thân hoặc nửa người\n" +
                            "2. Ảnh rõ nét, đủ sáng\n" +
                            "3. Trang phục gọn gàng để thấy rõ hình dáng cơ thể\n" +
                            "4. Tránh ảnh có khuôn mặt để được phân tích nhanh hơn"));
        }
    }

    private String callClaudeApi(MultipartFile image, Double height, Double weight, String gender) throws Exception {
        int retryCount = 0;
        long backoffMs = INITIAL_BACKOFF_MS;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                // Rate limiting check with increased interval
                synchronized (claudeApiLock) {
                    long currentTime = System.currentTimeMillis();
                    long timeSinceLastCall = currentTime - lastCallTimestamp;
                    if (timeSinceLastCall < MIN_CALL_INTERVAL_MS) {
                        Thread.sleep(MIN_CALL_INTERVAL_MS - timeSinceLastCall);
                    }
                    lastCallTimestamp = System.currentTimeMillis();
                }

                logger.info("Attempt {} to call Claude API", retryCount + 1);
                
                // Make the API call using existing code
                ResponseEntity<Map> response = makeClaudeApiCall(image, height, weight, gender);
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    logger.info("Claude API call successful on attempt {}", retryCount + 1);
                    Map responseBody = response.getBody();
                    if (responseBody != null && responseBody.containsKey("content")) {
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseBody.get("content");
                        if (!contentList.isEmpty()) {
                            Map<String, Object> firstContent = contentList.get(0);
                            if (firstContent.containsKey("text")) {
                                return (String) firstContent.get("text");
                            }
                        }
                    }
                    throw new RuntimeException("Invalid response format from Claude API");
                }
            } catch (Exception e) {
                lastException = e;
                logger.error("Error on attempt {}: {}", retryCount + 1, e.getMessage());

                // Check if we should retry based on the error type
                boolean shouldRetry = false;
                if (e instanceof org.springframework.web.client.HttpServerErrorException) {
                    org.springframework.web.client.HttpServerErrorException serverError = 
                        (org.springframework.web.client.HttpServerErrorException) e;
                    
                    // Retry on 5xx errors and specifically 529
                    if (serverError.getStatusCode().is5xxServerError() || 
                        serverError.getRawStatusCode() == 529) {
                        shouldRetry = true;
                        logger.warn("Server error ({}), implementing exponential backoff", 
                            serverError.getRawStatusCode());
                    }
                } else if (e instanceof org.springframework.web.client.ResourceAccessException) {
                    // Retry on network errors
                    shouldRetry = true;
                    logger.warn("Network error, will retry");
                }

                if (!shouldRetry || retryCount >= MAX_RETRIES - 1) {
                    break;
                }

                // Calculate backoff time with jitter
                long jitter = (long) (Math.random() * 1000); // Add up to 1 second of random jitter
                long actualBackoff = Math.min(backoffMs + jitter, MAX_BACKOFF_MS);
                logger.info("Backing off for {} ms before retry attempt {}", actualBackoff, retryCount + 2);
                Thread.sleep(actualBackoff);
                
                backoffMs = Math.min((long) (backoffMs * BACKOFF_MULTIPLIER), MAX_BACKOFF_MS);
                retryCount++;
            }
        }

        // If we've exhausted all retries, return a user-friendly error message
        logger.error("Failed to call Claude API after {} attempts", MAX_RETRIES);
        if (lastException != null) {
            if (lastException instanceof org.springframework.web.client.HttpServerErrorException) {
                org.springframework.web.client.HttpServerErrorException serverError = 
                    (org.springframework.web.client.HttpServerErrorException) lastException;
                
                if (serverError.getRawStatusCode() == 529) {
                    return "{\"bodyType\": \"Unknown\", " +
                           "\"analysis\": \"Xin lỗi, hệ thống AI đang quá tải. Vui lòng thử lại sau 1-2 phút hoặc chọn loại cơ thể thủ công.\", " +
                           "\"recommendations\": \"Vui lòng thử lại sau khi hệ thống ít tải hơn.\"}";
                }
            }
            
            return "{\"bodyType\": \"Unknown\", " +
                   "\"analysis\": \"Có lỗi khi kết nối với hệ thống AI. Chi tiết lỗi: " + lastException.getMessage() + "\", " +
                   "\"recommendations\": \"Vui lòng thử lại sau hoặc chọn loại cơ thể thủ công.\"}";
        }

        return "{\"bodyType\": \"Unknown\", " +
               "\"analysis\": \"Không thể kết nối với hệ thống AI sau nhiều lần thử.\", " +
               "\"recommendations\": \"Vui lòng thử lại sau hoặc chọn loại cơ thể thủ công.\"}";
    }

    private ResponseEntity<Map> makeClaudeApiCall(MultipartFile image, Double height, Double weight, String gender) throws Exception {
        // Thiết lập header
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", claudeApiKey);
        headers.set("anthropic-version", claudeApiVersion);
        headers.setContentType(MediaType.APPLICATION_JSON);

        logger.info("Using Claude API key: {}", claudeApiKey.substring(0, 10) + "...");
        logger.info("Using Claude API URL: {}", claudeApiUrl);
        logger.info("Using Claude model: {}", claudeModel);

        // Encode ảnh thành base64
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Tạo nội dung system message
        String systemPrompt = "Bạn là chuyên gia phân tích thể hình với 15 năm kinh nghiệm, chuyên phân loại hình thể dựa trên hình ảnh. " +
                "Nhiệm vụ của bạn là phân tích MỘT CÁCH CHÍNH XÁC hình thể cơ thể dựa trên tỷ lệ cơ-mỡ và đường nét cơ bắp. " +
                
                "📋 5 LOẠI HÌNH THỂ CHÍNH XÁC: " +
                "1. **THIN**: QUÁ ỐM - Rất gầy, xương nổi rõ, thiếu cân nặng nghiêm trọng " +
                "2. **SKINNY**: ỐM NHƯNG MỠ BỤNG NHIỀU - Gầy nhưng có mỡ tích tụ ở bụng, cơ bắp yếu " +
                "3. **NORMAL**: BÌNH THƯỜNG, KHÔNG CÓ CƠ BẮP - Cân nặng ổn, không gầy không mập, cơ bắp không nổi bật " +
                "4. **OVERWEIGHT**: QUÁ MỠ VÀ MẬP - Thừa cân rõ ràng, nhiều mỡ thừa, bụng to " +
                "5. **FIT**: CƠ BẮP SĂN CHẮC - Có đường cơ rõ nét, sixpack, cơ bắp phát triển tốt " +
                
                "🔍 TIÊU CHÍ PHÂN BIỆT QUAN TRỌNG: " +
                "- THIN vs SKINNY: Thin = hoàn toàn gầy, Skinny = gầy + mỡ bụng " +
                "- SKINNY vs NORMAL: Skinny có mỡ bụng, Normal cân đối không mỡ bụng " +
                "- NORMAL vs FIT: Normal không có cơ bắp nổi, Fit có cơ bắp rõ nét " +
                "- FIT vs OVERWEIGHT: Fit có cơ bắp, Overweight có mỡ thừa " +
                
                "TRẢ LỜI BẰNG TIẾNG VIỆT và PHẢI CHÍNH XÁC 100%.";

        // Tạo nội dung user message với prompt chi tiết hơn
        String prompt = "Hãy phân tích CHÍNH XÁC hình thể trong ảnh này (bỏ qua khuôn mặt). " +
                "Thông số: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "BMI = " + Math.round((weight / Math.pow(height/100, 2)) * 100) / 100.0 + ". " +
                
                "🔍 QUAN SÁT: " +
                "A) Quá ốm/xương nổi? → THIN " +
                "B) Ốm + mỡ bụng? → SKINNY " +
                "C) Cân đối, không cơ bắp? → NORMAL " +
                "D) Cơ bắp rõ nét/sixpack? → FIT " +
                "E) Quá mỡ/mập? → OVERWEIGHT " +
                
                "⚖️ PHÂN LOẠI CHÍNH XÁC (chọn 1 trong 5): " +
                "📏 THIN: Nếu QUÁ ỐM, xương nổi " +
                "⚠️ SKINNY: Nếu ỐM + MỠ BỤNG " +
                "⚖️ NORMAL: Nếu cân đối, KHÔNG cơ bắp " +
                "🔴 OVERWEIGHT: Nếu QUÁ MỠ, mập " +
                "🏋️ FIT: Nếu CƠ BẮP SĂN CHẮC " +
                
                "Chọn 1 trong 5: Thin, Skinny, Normal, Overweight, Fit. " +
                "Mô tả lý do chọn và đề xuất tập luyện. " +
                "JSON: {\"bodyType\": \"...\", \"analysis\": \"...\", \"recommendations\": \"...\"}";

        // Create the messages array with proper structure
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        // Create the content array for the message
        List<Map<String, Object>> contents = new ArrayList<>();
        
        // Add text content
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        contents.add(textContent);

        // Add image content
        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image");
        Map<String, String> source = new HashMap<>();
        source.put("type", "base64");
        source.put("media_type", image.getContentType());
        source.put("data", base64Image);
        imageContent.put("source", source);
        contents.add(imageContent);

        // Add content to the message
        userMessage.put("content", contents);
        messages.add(userMessage);

        // Create the complete request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", claudeModel != null ? claudeModel : "claude-3-haiku-20240307");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 1000);
        requestBody.put("temperature", 0.7);
        requestBody.put("system", systemPrompt);

        // Log the request structure for debugging
        logger.debug("Request body structure: {}", new ObjectMapper().writeValueAsString(requestBody));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        logger.info("Sending request to Claude API with model: {}", claudeModel);

        return restTemplate.exchange(claudeApiUrl, HttpMethod.POST, entity, Map.class);
    }

    private ImageAnalysisResponseDTO parseClaudeResponse(String claudeResponse) {
        logger.info("Parsing Claude response");
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Clean and normalize the response string
            String normalizedResponse = claudeResponse.trim()
                    .replaceAll("\\r\\n|\\r|\\n", " ") // Replace all types of newlines with space
                    .replaceAll("\\s+", " "); // Normalize multiple spaces to single space

            // First try to find JSON in the response using regex
            String jsonPattern = "\\{[^{}]*\\}";
            Pattern pattern = Pattern.compile(jsonPattern);
            Matcher matcher = pattern.matcher(normalizedResponse);

            // Find the largest JSON object in the response
            String largestJson = "";
            while (matcher.find()) {
                String found = matcher.group();
                if (found.length() > largestJson.length()) {
                    largestJson = found;
                }
            }

            // If we found a JSON object, try to parse it
            if (!largestJson.isEmpty()) {
                try {
                    // Configure ObjectMapper to be more lenient
                    objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
                    objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                    
                    // Try to parse directly to ImageAnalysisResponseDTO
                    return objectMapper.readValue(largestJson, ImageAnalysisResponseDTO.class);
                } catch (Exception e) {
                    logger.warn("Could not parse JSON directly to DTO, trying JsonNode approach: {}", e.getMessage());
                    
                    // Try parsing as JsonNode
                    try {
                        JsonNode jsonNode = objectMapper.readTree(largestJson);
                        ImageAnalysisResponseDTO result = new ImageAnalysisResponseDTO();
                        
                        // Extract fields with proper null checks
                        if (jsonNode.has("bodyType")) {
                            result.setBodyType(jsonNode.get("bodyType").asText());
                        } else {
                            result.setBodyType("Normal");
                        }
                        
                        if (jsonNode.has("analysis")) {
                            result.setAnalysis(jsonNode.get("analysis").asText());
                        } else {
                            result.setAnalysis("Không thể phân tích chi tiết. Vui lòng thử lại.");
                        }
                        
                        if (jsonNode.has("recommendations")) {
                            result.setRecommendations(jsonNode.get("recommendations").asText());
                        } else if (jsonNode.has("recom")) { // Check alternative field name
                            result.setRecommendations(jsonNode.get("recom").asText());
                        } else {
                            result.setRecommendations("Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.");
                        }
                        
                        return result;
                    } catch (Exception ex) {
                        logger.error("Error parsing with JsonNode: {}", ex.getMessage());
                        throw ex;
                    }
                }
            }

            // If no valid JSON found, try to extract information using regex
            Pattern bodyTypePattern = Pattern.compile("\"bodyType\"\\s*:\\s*\"([^\"]+)\"");
            Pattern analysisPattern = Pattern.compile("\"analysis\"\\s*:\\s*\"([^\"]+)\"");
            Pattern recommendationsPattern = Pattern.compile("\"recommendations\"\\s*:\\s*\"([^\"]+)\"");

            Matcher bodyTypeMatcher = bodyTypePattern.matcher(normalizedResponse);
            Matcher analysisMatcher = analysisPattern.matcher(normalizedResponse);
            Matcher recommendationsMatcher = recommendationsPattern.matcher(normalizedResponse);

            ImageAnalysisResponseDTO result = new ImageAnalysisResponseDTO();
            
            // Extract values with proper error handling
            try {
                result.setBodyType(bodyTypeMatcher.find() ? bodyTypeMatcher.group(1) : "Normal");
            } catch (Exception e) {
                logger.warn("Error extracting bodyType: {}", e.getMessage());
                result.setBodyType("Normal");
            }

            try {
                result.setAnalysis(analysisMatcher.find() ? analysisMatcher.group(1) : 
                    "Không thể trích xuất phân tích từ phản hồi AI. Vui lòng thử lại.");
            } catch (Exception e) {
                logger.warn("Error extracting analysis: {}", e.getMessage());
                result.setAnalysis("Không thể trích xuất phân tích từ phản hồi AI. Vui lòng thử lại.");
            }

            try {
                result.setRecommendations(recommendationsMatcher.find() ? recommendationsMatcher.group(1) : 
                    "Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.");
            } catch (Exception e) {
                logger.warn("Error extracting recommendations: {}", e.getMessage());
                result.setRecommendations("Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.");
            }

            return result;

        } catch (Exception e) {
            logger.error("Error parsing Claude response: {}", e.getMessage());
            logger.debug("Claude response for debugging: {}", claudeResponse);

            // Return a fallback response with error information
            ImageAnalysisResponseDTO fallback = new ImageAnalysisResponseDTO();
            fallback.setBodyType("Normal");
            fallback.setAnalysis("Không thể phân tích chi tiết. Lỗi xử lý phản hồi: " + e.getMessage());
            fallback.setRecommendations("Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.");
            return fallback;
        }
    }

    /**
     * Phương pháp thay thế để gọi Claude API khi phương pháp chính gặp hạn chế về chính sách
     * Sử dụng cách tiếp cận khác với prompt cải tiến cho 5 loại hình thể:
     * THIN (quá ốm), SKINNY (ốm nhưng mỡ bụng), NORMAL (bình thường không cơ bắp), 
     * OVERWEIGHT (quá mỡ mập), FIT (cơ bắp săn chắc)
     */
    private String callClaudeApiAlternative(MultipartFile image, Double height, Double weight, String gender) throws Exception {
        // Rate limiting check
        synchronized (claudeApiLock) {
            long currentTime = System.currentTimeMillis();
            long timeSinceLastCall = currentTime - lastCallTimestamp;
            if (timeSinceLastCall < MIN_CALL_INTERVAL_MS) {
                Thread.sleep(MIN_CALL_INTERVAL_MS - timeSinceLastCall);
            }
            lastCallTimestamp = System.currentTimeMillis();
        }

        // Thiết lập header
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", claudeApiKey);
        headers.set("anthropic-version", claudeApiVersion);
        headers.setContentType(MediaType.APPLICATION_JSON);

        logger.info("Using alternative Claude API approach with model: {}", claudeModel);

        // Encode ảnh thành base64
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Tạo nội dung yêu cầu
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", claudeModel != null ? claudeModel : "claude-3-haiku-20240307");
        requestBody.put("max_tokens", 2000);

        // Thêm system message như một tham số cấp cao
        String systemPrompt = "Bạn là chuyên gia phân tích thể hình với 15 năm kinh nghiệm, chuyên phân loại hình thể dựa trên hình ảnh. " +
                "Nhiệm vụ của bạn là phân tích MỘT CÁCH CHÍNH XÁC hình thể cơ thể dựa trên tỷ lệ cơ-mỡ và đường nét cơ bắp. " +
                
                "📋 5 LOẠI HÌNH THỂ CHÍNH XÁC: " +
                "1. **THIN**: QUÁ ỐM - Rất gầy, xương nổi rõ, thiếu cân nặng nghiêm trọng " +
                "2. **SKINNY**: ỐM NHƯNG MỠ BỤNG NHIỀU - Gầy nhưng có mỡ tích tụ ở bụng, cơ bắp yếu " +
                "3. **NORMAL**: BÌNH THƯỜNG, KHÔNG CÓ CƠ BẮP - Cân nặng ổn, không gầy không mập, cơ bắp không nổi bật " +
                "4. **OVERWEIGHT**: QUÁ MỠ VÀ MẬP - Thừa cân rõ ràng, nhiều mỡ thừa, bụng to " +
                "5. **FIT**: CƠ BẮP SĂN CHẮC - Có đường cơ rõ nét, sixpack, cơ bắp phát triển tốt " +
                
                "🔍 TIÊU CHÍ PHÂN BIỆT QUAN TRỌNG: " +
                "- THIN vs SKINNY: Thin = hoàn toàn gầy, Skinny = gầy + mỡ bụng " +
                "- SKINNY vs NORMAL: Skinny có mỡ bụng, Normal cân đối không mỡ bụng " +
                "- NORMAL vs FIT: Normal không có cơ bắp nổi, Fit có cơ bắp rõ nét " +
                "- FIT vs OVERWEIGHT: Fit có cơ bắp, Overweight có mỡ thừa " +
                
                "TRẢ LỜI BẰNG TIẾNG VIỆT và PHẢI CHÍNH XÁC 100%.";
        requestBody.put("system", systemPrompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        // Create user message content với tiêu chí chính xác
        String prompt = "Hãy phân tích CHÍNH XÁC hình thể trong ảnh này (bỏ qua khuôn mặt). " +
                "Thông số: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "BMI = " + Math.round((weight / Math.pow(height/100, 2)) * 100) / 100.0 + ". " +
                
                "🔍 QUAN SÁT: " +
                "A) Quá ốm/xương nổi? → THIN " +
                "B) Ốm + mỡ bụng? → SKINNY " +
                "C) Cân đối, không cơ bắp? → NORMAL " +
                "D) Cơ bắp rõ nét/sixpack? → FIT " +
                "E) Quá mỡ/mập? → OVERWEIGHT " +
                
                "⚖️ PHÂN LOẠI CHÍNH XÁC (chọn 1 trong 5): " +
                "📏 THIN: Nếu QUÁ ỐM, xương nổi " +
                "⚠️ SKINNY: Nếu ỐM + MỠ BỤNG " +
                "⚖️ NORMAL: Nếu cân đối, KHÔNG cơ bắp " +
                "🔴 OVERWEIGHT: Nếu QUÁ MỠ, mập " +
                "🏋️ FIT: Nếu CƠ BẮP SĂN CHẮC " +
                
                "Chọn 1 trong 5: Thin, Skinny, Normal, Overweight, Fit. " +
                "Mô tả lý do chọn và đề xuất tập luyện. " +
                "JSON: {\"bodyType\": \"...\", \"analysis\": \"...\", \"recommendations\": \"...\"}";

        List<Map<String, Object>> content = new ArrayList<>();
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        content.add(textContent);

        Map<String, Object> imageContent = new HashMap<>();
        imageContent.put("type", "image");
        Map<String, String> imageData = new HashMap<>();
        imageData.put("type", "base64");
        imageData.put("media_type", image.getContentType());
        imageData.put("data", base64Image);
        imageContent.put("source", imageData);
        content.add(imageContent);

        userMessage.put("content", content);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        int retryCount = 0;
        long backoffMs = INITIAL_BACKOFF_MS;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
                logger.info("Alternative approach: Attempt {} to call Claude API", retryCount + 1);

                ResponseEntity<Map> response = restTemplate.exchange(claudeApiUrl, HttpMethod.POST, entity, Map.class);
                HttpStatus statusCode = response.getStatusCode();

                if (statusCode.is2xxSuccessful()) {
                    logger.info("Alternative Claude API call successful");
                    Map responseBody = response.getBody();

                    if (responseBody != null && responseBody.containsKey("content")) {
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseBody.get("content");
                        if (!contentList.isEmpty()) {
                            Map<String, Object> firstContent = contentList.get(0);
                            if (firstContent.containsKey("text")) {
                                return (String) firstContent.get("text");
                            }
                        }
                    }

                    throw new RuntimeException("Invalid response format from Claude API");
                }
            } catch (Exception e) {
                lastException = e;
                logger.error("Alternative approach: Error on attempt {}: {}", retryCount + 1, e.getMessage());

                // Check if we should retry based on the error type
                boolean shouldRetry = false;
                if (e instanceof org.springframework.web.client.HttpServerErrorException) {
                    org.springframework.web.client.HttpServerErrorException serverError = 
                        (org.springframework.web.client.HttpServerErrorException) e;
                    
                    // Retry on 5xx errors
                    if (serverError.getStatusCode().is5xxServerError()) {
                        shouldRetry = true;
                    }
                    
                    // Special handling for 529 Overloaded error
                    if (serverError.getRawStatusCode() == 529) {
                        logger.warn("Alternative approach: Claude API is overloaded. Implementing exponential backoff.");
                        shouldRetry = true;
                    }
                } else if (e instanceof org.springframework.web.client.ResourceAccessException) {
                    // Retry on network errors
                    shouldRetry = true;
                }

                if (!shouldRetry || retryCount >= MAX_RETRIES - 1) {
                    break;
                }

                // Calculate backoff time with jitter
                long jitter = (long) (Math.random() * 1000); // Add up to 1 second of random jitter
                long actualBackoff = Math.min(backoffMs + jitter, MAX_BACKOFF_MS);
                logger.info("Alternative approach: Backing off for {} ms before retry", actualBackoff);
                Thread.sleep(actualBackoff);
                
                backoffMs = Math.min((long) (backoffMs * BACKOFF_MULTIPLIER), MAX_BACKOFF_MS);
                retryCount++;
            }
        }

        // If we've exhausted all retries, handle the final error
        if (lastException != null) {
            if (lastException instanceof org.springframework.web.client.HttpServerErrorException) {
                org.springframework.web.client.HttpServerErrorException serverError = 
                    (org.springframework.web.client.HttpServerErrorException) lastException;
                
                if (serverError.getRawStatusCode() == 529) {
                    return "{\"bodyType\": \"Normal\", \"analysis\": \"Claude API hiện đang quá tải (phương pháp thay thế). Vui lòng thử lại sau vài phút.\", " +
                           "\"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
                }
            }
            
            return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi kết nối tới Claude API (phương pháp thay thế) sau " + MAX_RETRIES + 
                   " lần thử: " + lastException.getMessage() + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
        }

        return "{\"bodyType\": \"Normal\", \"analysis\": \"Không thể kết nối tới Claude API (phương pháp thay thế) sau nhiều lần thử.\", " +
               "\"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
    }

    @GetMapping("/rebuild-decision-tree")
    public ResponseEntity<?> rebuildDecisionTree() {
        // Method disabled due to missing BuildDecisionTree class
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Decision tree functionality is currently disabled");
    }

    @GetMapping("/generate-enhanced-xml")
    public ResponseEntity<String> generateEnhancedXML() {
        // Method disabled due to missing XMLGenerator class
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("XML generation functionality is currently disabled");
    }

    @GetMapping("/validate-xml")
    public ResponseEntity<String> validateXML() {
        // Method disabled due to missing XMLGenerator class
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("XML validation functionality is currently disabled");
    }

    @GetMapping("/force-rebuild-tree")
    public ResponseEntity<?> forceRebuildDecisionTree() {
        // Method disabled due to missing BuildDecisionTree class
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body("Decision tree functionality is currently disabled");
    }

    @GetMapping("/recommendation-details/{userDataId}")
    public ResponseEntity<?> getRecommendationDetails(@PathVariable Integer userDataId) {
        try {
            // Find the user data AI record
            UserDataAi userDataAi = userDataAiService.findUserDataAiById(userDataId);
            if (userDataAi == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User data AI record not found with ID: " + userDataId);
            }
            
            // Find the recommended course
            Course recommendedCourse = null;
            if (userDataAi.getCourse() != null) {
                try {
                    recommendedCourse = courseService.findCourseById(userDataAi.getCourse().getCourseId());
                } catch (Exception e) {
                    logger.warn("Could not find course: " + e.getMessage());
                }
            }
            
            // Get course type details if available
            Map<String, Object> courseTypeDetails = new HashMap<>();
            if (recommendedCourse != null && recommendedCourse.getCourseType() != null) {
                courseTypeDetails.put("id", recommendedCourse.getCourseType().getCourseTypeId());
                courseTypeDetails.put("name", recommendedCourse.getCourseType().getCourseTypeName());
            }
            
            // Create a map with all the details
            Map<String, Object> recommendationDetails = new HashMap<>();
            recommendationDetails.put("userDataId", userDataAi.getUser_data_id());
            recommendationDetails.put("customerId", userDataAi.getCustomer() != null ? userDataAi.getCustomer().getCustomerId() : null);
            
            // User metrics used for recommendation
            Map<String, Object> userMetrics = new HashMap<>();
            userMetrics.put("age", userDataAi.getAge());
            userMetrics.put("gender", userDataAi.getGender());
            userMetrics.put("height", userDataAi.getHeight());
            userMetrics.put("weight", userDataAi.getWeight());
            userMetrics.put("bmi", userDataAi.getBmi());
            userMetrics.put("body_type", userDataAi.getBody_type());
            userMetrics.put("training_goals", userDataAi.getTraining_goals());
            userMetrics.put("training_history", userDataAi.getTraining_history());
            userMetrics.put("training_frequency", userDataAi.getTraining_frequency());
            userMetrics.put("occupation", userDataAi.getOccupation());
            userMetrics.put("work_environment", userDataAi.getWork_environment());
            
            recommendationDetails.put("userMetrics", userMetrics);
            
            // Course details
            Map<String, Object> courseDetails = new HashMap<>();
            if (recommendedCourse != null) {
                courseDetails.put("id", recommendedCourse.getCourseId());
                courseDetails.put("name", recommendedCourse.getCourseName());
                courseDetails.put("description", recommendedCourse.getDescription());
                courseDetails.put("duration", recommendedCourse.getDuration());
                courseDetails.put("image", recommendedCourse.getImage());
                courseDetails.put("status", recommendedCourse.getStatus());
                courseDetails.put("courseType", courseTypeDetails);
            }
            
            recommendationDetails.put("recommendedCourse", courseDetails);
            recommendationDetails.put("isConfirmed", userDataAi.getEffective());
            
            // Add explanation of how this course matches the user profile
            String explanation = generateRecommendationExplanation(userDataAi, recommendedCourse);
            recommendationDetails.put("explanation", explanation);
            
            return ResponseEntity.ok(recommendationDetails);
        } catch (Exception e) {
            logger.error("Error getting recommendation details: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting recommendation details: " + e.getMessage());
        }
    }

    /**
     * Generate an explanation of why this course was recommended based on user metrics
     */
    private String generateRecommendationExplanation(UserDataAi userDataAi, Course course) {
        StringBuilder explanation = new StringBuilder();
        
        if (course == null || userDataAi == null) {
            return "Không có đủ thông tin để giải thích đề xuất.";
        }
        
        // Add introduction
        explanation.append("Khóa học này được đề xuất dựa trên các thông số của bạn:\n\n");
        
        // Body type explanation
        if (userDataAi.getBody_type() != null) {
            String bodyTypeValue = userDataAi.getBody_type().getDatabaseValue();
            explanation.append("- Với hình thể ").append(bodyTypeValue).append(", ");
            
            if (bodyTypeValue.equalsIgnoreCase("Skinny Fat")) {
                explanation.append("bạn cần tập trung vào tăng cường cơ bắp và giảm lượng mỡ cơ thể.\n");
            } else if (bodyTypeValue.equalsIgnoreCase("Thin")) {
                explanation.append("bạn nên tập trung vào tăng cân và phát triển cơ bắp.\n");
            } else if (bodyTypeValue.equalsIgnoreCase("Overweight")) {
                explanation.append("bạn nên tập trung vào giảm mỡ và cải thiện sức khỏe tim mạch.\n");
            } else if (bodyTypeValue.equalsIgnoreCase("Normal")) {
                explanation.append("bạn có thể tập trung vào duy trì và tăng cường sức khỏe tổng thể.\n");
            } else if (bodyTypeValue.equalsIgnoreCase("Fit")) {
                explanation.append("bạn có thể tập trung vào nâng cao hiệu suất và phát triển cơ bắp đặc biệt.\n");
            }
        }
        
        // Training goals explanation
        if (userDataAi.getTraining_goals() != null) {
            explanation.append("- Mục tiêu tập luyện ").append(getTrainingGoalVietnamese(userDataAi.getTraining_goals().toString()))
                      .append(" của bạn sẽ được đáp ứng thông qua khóa học này.\n");
        }
        
        // BMI explanation
        if (userDataAi.getBmi() != null) {
            explanation.append("- Chỉ số BMI của bạn là ").append(userDataAi.getBmi()).append(". ");
            
            if (userDataAi.getBmi() < 18.5) {
                explanation.append("Bạn đang ở dưới chuẩn và nên tập trung vào tăng khối lượng cơ.\n");
            } else if (userDataAi.getBmi() >= 18.5 && userDataAi.getBmi() < 25) {
                explanation.append("Bạn đang ở mức cân đối và nên tập trung vào duy trì và cải thiện sức khỏe.\n");
            } else if (userDataAi.getBmi() >= 25 && userDataAi.getBmi() < 30) {
                explanation.append("Bạn đang hơi thừa cân và nên tập trung vào giảm mỡ và tăng cơ.\n");
            } else {
                explanation.append("Bạn đang ở mức béo phì và nên tập trung vào giảm cân để cải thiện sức khỏe.\n");
            }
        }
        
        // Training history
        if (userDataAi.getTraining_history() != null) {
            explanation.append("- Với lịch sử tập luyện ").append(userDataAi.getTraining_history())
                      .append(", khóa học này được thiết kế phù hợp với kinh nghiệm của bạn.\n");
        }
        
        // Health conditions
        boolean hasHealthIssues = false;

        if (!hasHealthIssues) {
            explanation.append("- Bạn không có vấn đề sức khỏe đặc biệt, nên khóa học này phù hợp tối ưu cho bạn.\n");
        }
        
        // Course benefit
        explanation.append("\nKhóa học ").append(course.getCourseName())
                  .append(" sẽ giúp bạn đạt được mục tiêu của mình một cách hiệu quả và an toàn.");
        
        return explanation.toString();
    }

    @PostMapping("/confirm-recommendation")
    public ResponseEntity<?> confirmRecommendation(@RequestBody Map<String, Object> requestData) {
        try {
            // Lấy formData và recommendedCourseId từ frontend
            UserDataDTO formData = null;
            final Integer recommendedCourseId; // Make final
            
            if (requestData.containsKey("formData")) {
                // Parse formData từ request
                Map<String, Object> formDataMap = (Map<String, Object>) requestData.get("formData");
                formData = parseFormDataFromMap(formDataMap);
            }
            
            if (requestData.containsKey("recommendedCourseId")) {
                recommendedCourseId = (Integer) requestData.get("recommendedCourseId");
            } else {
                recommendedCourseId = null;
            }
            
            if (formData == null || recommendedCourseId == null) {
                return ResponseEntity.badRequest()
                        .body("Missing required data: formData or recommendedCourseId");
            }
            
            Customer customer = customerService.findById(formData.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // *** KIỂM TRA DUPLICATE: Chỉ tạo bản ghi mới nếu chưa có customer + course_id này ***
            // Tạm thời kiểm tra bằng cách lấy tất cả records của customer và filter
            List<UserDataAi> allUserRecords = userDataAiService.findByCustomerId(formData.getCustomerId());
            List<UserDataAi> existingRecords = allUserRecords.stream()
                .filter(record -> record.getCourse() != null && 
                               record.getCourse().getCourseId().equals(recommendedCourseId))
                .collect(Collectors.toList());
            
            Integer userDataId = null;
            
            if (!existingRecords.isEmpty()) {
                // Đã có bản ghi với customer và course này
                UserDataAi existingRecord = existingRecords.get(0);
                userDataId = existingRecord.getUser_data_id();
                logger.info("Found existing record for customer {} and course {}: userDataId {}", 
                    formData.getCustomerId(), recommendedCourseId, userDataId);
                
                // Cập nhật thông tin mới (vòng eo, vòng ngực, etc.)
                updateExistingUserDataAi(existingRecord, formData);
                userDataAiService.saveUserDataAi(existingRecord);
                
            } else {
                // Chưa có bản ghi, tạo mới
                logger.info("Creating new record for customer {} and course {}", 
                    formData.getCustomerId(), recommendedCourseId);
                
                formData.setRecommendedCourseId(recommendedCourseId);
                UserDataAi userDataAi = createUserDataAiFromDTO(formData, customer);
                userDataAi.setEffective(true); // Đã được xác nhận
                userDataAi = userDataAiService.saveUserDataAi(userDataAi);
                userDataId = userDataAi.getUser_data_id();
            }
            
            // Lưu khóa học đề xuất vào CustomerCourse (nếu chưa có)
            List<CustomerCourse> existingCourses = customerCourseService.findByCustomerId(formData.getCustomerId());
            boolean courseExists = existingCourses.stream()
                    .anyMatch(cc -> cc.getCourse().getCourseId().equals(recommendedCourseId));
            
            if (!courseExists) {
                logger.info("Saving confirmed course {} for user {}", recommendedCourseId, formData.getCustomerId());
                customerCourseService.saveRecommendedCourseById(recommendedCourseId, formData.getCustomerId());
            } else {
                logger.info("Course {} already exists for user {}, skipping save", recommendedCourseId, formData.getCustomerId());
            }
            
            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Recommendation confirmed successfully");
            response.put("redirectUrl", "/collection/recommended-courses");
            response.put("customerId", formData.getCustomerId());
            response.put("userDataId", userDataId);
            
            // *** XÓA DRAFT DATA SAU KHI HOÀN THÀNH ***
            try {
                FormWizardDraftDTO removedDraft = draftStorage.remove(formData.getCustomerId());
                if (removedDraft != null) {
                    logger.info("Cleared draft data for customer {} after successful confirmation", 
                        formData.getCustomerId());
                    response.put("draftCleared", true);
                } else {
                    response.put("draftCleared", false);
                }
            } catch (Exception e) {
                logger.warn("Failed to clear draft data for customer {}: {}", 
                    formData.getCustomerId(), e.getMessage());
                response.put("draftCleared", false);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error confirming recommendation: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error confirming recommendation: " + e.getMessage());
        }
    }
    
    // Helper method để parse formData từ Map
    private UserDataDTO parseFormDataFromMap(Map<String, Object> formDataMap) {
        UserDataDTO dto = new UserDataDTO();
        
        if (formDataMap.get("age") != null) dto.setAge((Integer) formDataMap.get("age"));
        if (formDataMap.get("gender") != null) dto.setGender((String) formDataMap.get("gender"));
        if (formDataMap.get("height") != null) dto.setHeight(((Number) formDataMap.get("height")).doubleValue());
        if (formDataMap.get("weight") != null) dto.setWeight(((Number) formDataMap.get("weight")).doubleValue());
        if (formDataMap.get("bmi") != null) dto.setBmi(((Number) formDataMap.get("bmi")).doubleValue());
        if (formDataMap.get("chest_circumference") != null) dto.setChest_circumference(((Number) formDataMap.get("chest_circumference")).doubleValue());
        if (formDataMap.get("waist_circumference") != null) dto.setWaist_circumference(((Number) formDataMap.get("waist_circumference")).doubleValue());
        if (formDataMap.get("training_goals") != null) dto.setTraining_goals((String) formDataMap.get("training_goals"));
        if (formDataMap.get("body_type") != null) dto.setBody_type((String) formDataMap.get("body_type"));
        if (formDataMap.get("occupation") != null) dto.setOccupation((String) formDataMap.get("occupation"));
        if (formDataMap.get("work_environment") != null) dto.setWork_environment((String) formDataMap.get("work_environment"));
        if (formDataMap.get("training_history") != null) dto.setTraining_history((String) formDataMap.get("training_history"));
        if (formDataMap.get("training_frequency") != null) dto.setTraining_frequency((String) formDataMap.get("training_frequency"));
        if (formDataMap.get("customerId") != null) dto.setCustomerId((Integer) formDataMap.get("customerId"));
        
        return dto;
    }
    
    // Helper method để cập nhật bản ghi hiện có
    private void updateExistingUserDataAi(UserDataAi existing, UserDataDTO newData) {
        // Cập nhật các thông tin có thể thay đổi
        if (newData.getChest_circumference() != null) {
            existing.setChest_circumference(newData.getChest_circumference());
        }
        if (newData.getWaist_circumference() != null) {
            existing.setWaist_circumference(newData.getWaist_circumference());
        }
        if (newData.getWeight() != null) {
            existing.setWeight(newData.getWeight());
        }
        if (newData.getBmi() != null) {
            existing.setBmi(newData.getBmi());
        }
        
        // Cập nhật thời gian sửa đổi nếu có field này
        // existing.setUpdatedAt(new Date());
        
        logger.info("Updated existing UserDataAi record with new measurements");
    }

    @GetMapping("/recommendation-step-7")
    public ResponseEntity<?> getRecommendationStep7Data(
            @RequestParam(required = false) String formDataJson,
            @RequestParam(required = false) Integer recommendedCourseId) {
        try {
            // Check if required parameters are provided
            if (formDataJson == null || recommendedCourseId == null) {
                logger.warn("Missing required parameters: formDataJson or recommendedCourseId");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Missing required parameters: formDataJson or recommendedCourseId");
                errorResponse.put("currentStep", 7);
                errorResponse.put("totalSteps", 7);
                errorResponse.put("stepTitle", "Xác Nhận Khóa Học Được Đề Xuất");
                
                // Add empty recommendation to avoid frontend errors
                errorResponse.put("recommendedCourse", Collections.emptyMap());
                errorResponse.put("userMetrics", Collections.emptyMap());
                errorResponse.put("explanation", "Không thể lấy thông tin chi tiết khóa học. Vui lòng thử lại sau.");
                
                return ResponseEntity.ok(errorResponse);
            }
            
            // Parse formData from JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> formDataMap = objectMapper.readValue(formDataJson, Map.class);
            UserDataDTO formData = parseFormDataFromMap(formDataMap);
            
            // Get course details
            Course recommendedCourse = courseService.findCourseById(recommendedCourseId);
            if (recommendedCourse == null) {
                return ResponseEntity.badRequest()
                        .body("Course not found with ID: " + recommendedCourseId);
            }
            
            // Get course type details if available
            Map<String, Object> courseTypeDetails = new HashMap<>();
            if (recommendedCourse.getCourseType() != null) {
                courseTypeDetails.put("id", recommendedCourse.getCourseType().getCourseTypeId());
                courseTypeDetails.put("name", recommendedCourse.getCourseType().getCourseTypeName());
            }
            
            // Create response with all the details
            Map<String, Object> recommendationDetails = new HashMap<>();
            recommendationDetails.put("customerId", formData.getCustomerId());
            
            // User metrics từ form data - bao gồm vòng eo và vòng ngực
            Map<String, Object> userMetrics = new HashMap<>();
            userMetrics.put("age", formData.getAge());
            userMetrics.put("gender", formData.getGender());
            userMetrics.put("height", formData.getHeight());
            userMetrics.put("weight", formData.getWeight());
            userMetrics.put("bmi", formData.getBmi());
            userMetrics.put("chest_circumference", formData.getChest_circumference()); // *** HIỂN THỊ VÒNG NGỰC ***
            userMetrics.put("waist_circumference", formData.getWaist_circumference()); // *** HIỂN THỊ VÒNG EO ***
            userMetrics.put("body_type", formData.getBody_type());
            userMetrics.put("training_goals", formData.getTraining_goals());
            userMetrics.put("training_history", formData.getTraining_history());
            userMetrics.put("training_frequency", formData.getTraining_frequency());
            userMetrics.put("occupation", formData.getOccupation());
            userMetrics.put("work_environment", formData.getWork_environment());
            
            recommendationDetails.put("userMetrics", userMetrics);
            
            // Course details
            Map<String, Object> courseDetails = new HashMap<>();
            courseDetails.put("id", recommendedCourse.getCourseId());
            courseDetails.put("name", recommendedCourse.getCourseName());
            courseDetails.put("description", recommendedCourse.getDescription());
            courseDetails.put("duration", recommendedCourse.getDuration());
            courseDetails.put("image", recommendedCourse.getImage());
            courseDetails.put("status", recommendedCourse.getStatus());
            courseDetails.put("courseType", courseTypeDetails);
            
            recommendationDetails.put("recommendedCourse", courseDetails);
            recommendationDetails.put("isConfirmed", false); // Chưa xác nhận
            
            // Add explanation - tạo temporary UserDataAi để generate explanation
            UserDataAi tempUserDataAi = createUserDataAiFromDTO(formData, null);
            String explanation = generateRecommendationExplanation(tempUserDataAi, recommendedCourse);
            recommendationDetails.put("explanation", explanation);
            
            // Add step information for the front-end
            recommendationDetails.put("currentStep", 7);
            recommendationDetails.put("totalSteps", 7);
            recommendationDetails.put("stepTitle", "Xác Nhận Khóa Học Được Đề Xuất");
            
            // Add confirmation actions - cần gửi formData và courseId khi confirm
            Map<String, Object> actions = new HashMap<>();
            actions.put("confirmUrl", "/api/v1/track-data-ai/confirm-recommendation");
            actions.put("skipUrl", "/collection/recommended-courses");
            actions.put("formData", formData); // Để frontend có thể gửi lại khi confirm
            actions.put("recommendedCourseId", recommendedCourseId);
            recommendationDetails.put("actions", actions);
            
            return ResponseEntity.ok(recommendationDetails);
        } catch (Exception e) {
            logger.error("Error getting step 7 data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting step 7 data: " + e.getMessage());
        }
    }

    private String getTrainingGoalVietnamese(String trainingGoal) {
        if (trainingGoal == null) return "";
        switch (trainingGoal.trim()) {
            case "1, Strength Training":
                return "Tăng sức mạnh";
            case "2, Cardiovascular Training":
                return "Cải thiện tim mạch";
            case "3, Flexibility and Balance":
                return "Tăng linh hoạt & thăng bằng";
            case "4, Train strength and endurance":
                return "Tăng sức bền & sức mạnh";
            case "5, Mind-Body Wellness":
                return "Sức khỏe tinh thần & thể chất";
            case "6, Functional Training":
                return "Tập luyện chức năng";
            case "7, Weight Loss":
                return "Giảm cân tổng thể";
            default:
                return trainingGoal; // fallback nếu không khớp
        }
    }

    @PostMapping("/update-post-course/{userDataId}")
    public ResponseEntity<?> updatePostCourseResults(@PathVariable Integer userDataId, 
                                                    @RequestBody PostCourseUpdateDTO updateData) {
        try {
            // Tìm bản ghi UserDataAi
            UserDataAi userDataAi = userDataAiService.findUserDataAiById(userDataId);
            if (userDataAi == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User data AI record not found with ID: " + userDataId);
            }
            
            // *** VALIDATION THỜI GIAN: Chỉ cho phép cập nhật sau ít nhất 1 tháng ***
            if (userDataAi.getCreated_date() != null) {
                LocalDateTime oneMonthAgo = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
                
                if (userDataAi.getCreated_date().isAfter(oneMonthAgo)) {
                    long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), userDataAi.getCreated_date().plus(1, ChronoUnit.MONTHS));
                    
                    return ResponseEntity.badRequest()
                            .body("Chỉ có thể cập nhật kết quả sau ít nhất 1 tháng kể từ khi bắt đầu khóa học. " +
                                  "Bạn còn " + daysRemaining + " ngày nữa mới có thể cập nhật.");
                }
                
                logger.info("Time validation passed: Record created {} (more than 1 month ago)", userDataAi.getCreated_date());
            } else {
                logger.warn("No created_date found for userDataAi {}, allowing update", userDataId);
            }
            
            // Kiểm tra xem đã có dữ liệu _after chưa
            if (userDataAi.getWeight_after() != null || 
                userDataAi.getChest_circumference_after() != null || 
                userDataAi.getWaist_circumference_after() != null) {
                logger.info("UserDataAi {} already has post-course data, updating existing values", userDataId);
            }
            
            // Lưu giá trị BAN ĐẦU để so sánh (KHÔNG GHI ĐÈ)
            Double originalWeight = userDataAi.getWeight();
            Double originalChest = userDataAi.getChest_circumference();
            Double originalWaist = userDataAi.getWaist_circumference();
            
            // Logic đánh giá hiệu quả:
            // So sánh giữa dữ liệu GỐC và dữ liệu SAU KHI TẬP
            boolean isEffective = true;
            StringBuilder evaluationReason = new StringBuilder("Đánh giá hiệu quả sau khi tập luyện: ");
            
            // Kiểm tra cân nặng
            if (updateData.getWeight_after() != null && originalWeight != null) {
                if (updateData.getWeight_after() <= originalWeight) {
                    evaluationReason.append("Cân nặng giảm/duy trì từ ")
                        .append(originalWeight).append("kg xuống ").append(updateData.getWeight_after()).append("kg. ");
                } else {
                    isEffective = false;
                    evaluationReason.append("Cân nặng tăng từ ")
                        .append(originalWeight).append("kg lên ").append(updateData.getWeight_after()).append("kg. ");
                }
            }
            
            // Kiểm tra vòng eo (quan trọng nhất)
            if (updateData.getWaist_circumference_after() != null && originalWaist != null) {
                if (updateData.getWaist_circumference_after() <= originalWaist) {
                    evaluationReason.append("Vòng eo giảm/duy trì từ ")
                        .append(originalWaist).append("cm xuống ").append(updateData.getWaist_circumference_after()).append("cm. ");
                } else {
                    isEffective = false;
                    evaluationReason.append("Vòng eo tăng từ ")
                        .append(originalWaist).append("cm lên ").append(updateData.getWaist_circumference_after()).append("cm. ");
                }
            }
            
            // Kiểm tra vòng ngực (có thể tăng do tăng cơ)
            if (updateData.getChest_circumference_after() != null && originalChest != null) {
                if (updateData.getChest_circumference_after() >= originalChest) {
                    evaluationReason.append("Vòng ngực duy trì/tăng từ ")
                        .append(originalChest).append("cm lên ").append(updateData.getChest_circumference_after()).append("cm (tích cực). ");
                } else {
                    evaluationReason.append("Vòng ngực giảm từ ")
                        .append(originalChest).append("cm xuống ").append(updateData.getChest_circumference_after()).append("cm. ");
                }
            }
            
            // *** CẬP NHẬT VÀO CÁC TRƯỜNG _AFTER (KHÔNG GHI ĐÈ TRƯỜNG GỐC) ***
            if (updateData.getWeight_after() != null) {
                userDataAi.setWeight_after(updateData.getWeight_after());
                logger.info("Updated weight_after: {} (original weight: {})", updateData.getWeight_after(), originalWeight);
            }
            
            if (updateData.getChest_circumference_after() != null) {
                userDataAi.setChest_circumference_after(updateData.getChest_circumference_after());
                logger.info("Updated chest_circumference_after: {} (original: {})", updateData.getChest_circumference_after(), originalChest);
            }
            
            if (updateData.getWaist_circumference_after() != null) {
                userDataAi.setWaist_circumference_after(updateData.getWaist_circumference_after());
                logger.info("Updated waist_circumference_after: {} (original: {})", updateData.getWaist_circumference_after(), originalWaist);
            }
            
            // Cập nhật trạng thái hiệu quả
            userDataAi.setEffective(isEffective);
            
            // Lưu vào database
            userDataAiService.saveUserDataAi(userDataAi);
            
            // Kết quả đánh giá
            evaluationReason.append(isEffective ? "Kết luận: Khóa học hiệu quả!" : "Kết luận: Khóa học cần cải thiện.");
            
            logger.info("Updated post-course results for userDataId {}: effective = {}", userDataId, isEffective);
            
            // Trả về kết quả với thông tin so sánh
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cập nhật kết quả sau khóa học thành công");
            response.put("effective", isEffective);
            response.put("evaluationReason", evaluationReason.toString());
            
            // Dữ liệu TRƯỚC và SAU để so sánh
            Map<String, Object> beforeData = new HashMap<>();
            beforeData.put("weight", originalWeight);
            beforeData.put("chest_circumference", originalChest);
            beforeData.put("waist_circumference", originalWaist);
            response.put("beforeData", beforeData);
            
            Map<String, Object> afterData = new HashMap<>();
            afterData.put("weight_after", userDataAi.getWeight_after());
            afterData.put("chest_circumference_after", userDataAi.getChest_circumference_after());
            afterData.put("waist_circumference_after", userDataAi.getWaist_circumference_after());
            response.put("afterData", afterData);
            
            // Tính toán sự thay đổi
            Map<String, Object> changes = new HashMap<>();
            if (originalWeight != null && userDataAi.getWeight_after() != null) {
                changes.put("weight_change", userDataAi.getWeight_after() - originalWeight);
            }
            if (originalChest != null && userDataAi.getChest_circumference_after() != null) {
                changes.put("chest_change", userDataAi.getChest_circumference_after() - originalChest);
            }
            if (originalWaist != null && userDataAi.getWaist_circumference_after() != null) {
                changes.put("waist_change", userDataAi.getWaist_circumference_after() - originalWaist);
            }
            response.put("changes", changes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating post-course results: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating post-course results: " + e.getMessage());
        }
    }

    @GetMapping("/compare-results/{userDataId}")
    public ResponseEntity<?> getComparisonResults(@PathVariable Integer userDataId) {
        try {
            // Tìm bản ghi UserDataAi
            UserDataAi userDataAi = userDataAiService.findUserDataAiById(userDataId);
            if (userDataAi == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User data AI record not found with ID: " + userDataId);
            }
            
            // Tạo response với thông tin so sánh
            Map<String, Object> response = new HashMap<>();
            response.put("userDataId", userDataId);
            response.put("customerId", userDataAi.getCustomer() != null ? userDataAi.getCustomer().getCustomerId() : null);
            response.put("courseName", userDataAi.getCourse() != null ? userDataAi.getCourse().getCourseName() : null);
            response.put("effective", userDataAi.getEffective());
            response.put("createdDate", userDataAi.getCreated_date());
            response.put("updatedDate", userDataAi.getUpdated_date());
            
            // Dữ liệu ban đầu
            Map<String, Object> beforeData = new HashMap<>();
            beforeData.put("weight", userDataAi.getWeight());
            beforeData.put("chest_circumference", userDataAi.getChest_circumference());
            beforeData.put("waist_circumference", userDataAi.getWaist_circumference());
            beforeData.put("bmi", userDataAi.getBmi());
            beforeData.put("height", userDataAi.getHeight());
            response.put("beforeData", beforeData);
            
            // Dữ liệu sau khi tập
            Map<String, Object> afterData = new HashMap<>();
            afterData.put("weight_after", userDataAi.getWeight_after());
            afterData.put("chest_circumference_after", userDataAi.getChest_circumference_after());
            afterData.put("waist_circumference_after", userDataAi.getWaist_circumference_after());
            
            // Tính BMI sau nếu có cân nặng sau
            Double bmiAfter = null;
            if (userDataAi.getWeight_after() != null && userDataAi.getHeight() != null) {
                double heightInMeters = userDataAi.getHeight() / 100.0;
                bmiAfter = userDataAi.getWeight_after() / (heightInMeters * heightInMeters);
                bmiAfter = Math.round(bmiAfter * 100.0) / 100.0;
            }
            afterData.put("bmi_after", bmiAfter);
            response.put("afterData", afterData);
            
            // Tính toán sự thay đổi
            Map<String, Object> changes = new HashMap<>();
            if (userDataAi.getWeight() != null && userDataAi.getWeight_after() != null) {
                changes.put("weight_change", userDataAi.getWeight_after() - userDataAi.getWeight());
            }
            if (userDataAi.getChest_circumference() != null && userDataAi.getChest_circumference_after() != null) {
                changes.put("chest_change", userDataAi.getChest_circumference_after() - userDataAi.getChest_circumference());
            }
            if (userDataAi.getWaist_circumference() != null && userDataAi.getWaist_circumference_after() != null) {
                changes.put("waist_change", userDataAi.getWaist_circumference_after() - userDataAi.getWaist_circumference());
            }
            if (userDataAi.getBmi() != null && bmiAfter != null) {
                changes.put("bmi_change", bmiAfter - userDataAi.getBmi());
            }
            response.put("changes", changes);
            
            // Phân tích kết quả
            boolean hasAfterData = userDataAi.getWeight_after() != null || 
                                 userDataAi.getChest_circumference_after() != null || 
                                 userDataAi.getWaist_circumference_after() != null;
            
            response.put("hasAfterData", hasAfterData);
            
            if (!hasAfterData) {
                response.put("message", "Chưa có dữ liệu sau khi tập luyện. Hãy cập nhật sau ít nhất 1 tháng.");
            } else {
                // Tạo thông điệp đánh giá
                StringBuilder evaluation = new StringBuilder();
                if (userDataAi.getEffective() != null && userDataAi.getEffective()) {
                    evaluation.append("🎉 Chúc mừng! Khóa học đã mang lại hiệu quả tích cực. ");
                } else {
                    evaluation.append("⚠️ Khóa học chưa đạt hiệu quả mong đợi. ");
                }
                
                // Thêm chi tiết thay đổi
                if (changes.containsKey("weight_change")) {
                    double weightChange = (Double) changes.get("weight_change");
                    if (weightChange <= 0) {
                        evaluation.append("Cân nặng giảm ").append(Math.abs(weightChange)).append("kg. ");
                    } else {
                        evaluation.append("Cân nặng tăng ").append(weightChange).append("kg. ");
                    }
                }
                
                if (changes.containsKey("waist_change")) {
                    double waistChange = (Double) changes.get("waist_change");
                    if (waistChange <= 0) {
                        evaluation.append("Vòng eo giảm ").append(Math.abs(waistChange)).append("cm. ");
                    } else {
                        evaluation.append("Vòng eo tăng ").append(waistChange).append("cm. ");
                    }
                }
                
                response.put("evaluation", evaluation.toString());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting comparison results: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting comparison results: " + e.getMessage());
        }
    }

    @PostMapping("/generate-recommendation-explanation")
    public ResponseEntity<?> generateRecommendationExplanation(@RequestBody Map<String, Object> requestData) {
        try {
            // Parse dữ liệu từ request
            Map<String, Object> userMetricsMap = (Map<String, Object>) requestData.get("userMetrics");
            Map<String, Object> courseData = (Map<String, Object>) requestData.get("courseData");
            
            if (userMetricsMap == null || courseData == null) {
                return ResponseEntity.badRequest().body("Missing userMetrics or courseData");
            }
            
            // Lấy thông tin khóa học chi tiết
            Integer courseId = (Integer) courseData.get("id");
            Course course = courseService.findCourseById(courseId);
            
            if (course == null) {
                return ResponseEntity.badRequest().body("Course not found");
            }
            
            // Tạo prompt chi tiết cho Claude
            String claudePrompt = createDetailedRecommendationPrompt(userMetricsMap, course);
            
            // Gọi Claude API
            String explanation = callClaudeForRecommendationExplanation(claudePrompt);
            
            // Trả về kết quả
            Map<String, Object> response = new HashMap<>();
            response.put("explanation", explanation);
            response.put("courseId", courseId);
            response.put("courseName", course.getCourseName());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error generating recommendation explanation: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating explanation: " + e.getMessage());
        }
    }
    
    private String createDetailedRecommendationPrompt(Map<String, Object> userMetrics, Course course) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là một huấn luyện viên thể hình chuyên nghiệp. Hãy giải thích tại sao khóa học được đề xuất phù hợp với hồ sơ của người dùng.\n\n");
        
        // Thông tin người dùng
        prompt.append("THÔNG TIN NGƯỜI DÙNG:\n");
        prompt.append("- Độ tuổi: ").append(userMetrics.get("age")).append("\n");
        prompt.append("- Giới tính: ").append(userMetrics.get("gender")).append("\n");
        prompt.append("- Chiều cao: ").append(userMetrics.get("height")).append(" cm\n");
        prompt.append("- Cân nặng: ").append(userMetrics.get("weight")).append(" kg\n");
        prompt.append("- BMI: ").append(userMetrics.get("bmi")).append("\n");
        
        if (userMetrics.get("waist_circumference") != null) {
            prompt.append("- Vòng eo: ").append(userMetrics.get("waist_circumference")).append(" cm\n");
        }
        if (userMetrics.get("chest_circumference") != null) {
            prompt.append("- Vòng ngực: ").append(userMetrics.get("chest_circumference")).append(" cm\n");
        }
        
        prompt.append("- Loại hình thể: ").append(userMetrics.get("body_type")).append("\n");
        prompt.append("- Nghề nghiệp: ").append(userMetrics.get("occupation")).append("\n");
        prompt.append("- Môi trường làm việc: ").append(userMetrics.get("work_environment")).append("\n");
        prompt.append("- Lịch sử tập luyện: ").append(userMetrics.get("training_history")).append("\n");
        prompt.append("- Tần suất mong muốn: ").append(userMetrics.get("training_frequency")).append("\n");
        prompt.append("- Mục tiêu tập luyện: ").append(userMetrics.get("training_goals")).append("\n");
        

        
        // Thông tin khóa học
        prompt.append("\nKHÓA HỌC ĐƯỢC ĐỀ XUẤT:\n");
        prompt.append("- Tên khóa học: ").append(course.getCourseName()).append("\n");
        prompt.append("- Mô tả: ").append(course.getDescription() != null ? course.getDescription() : "Khóa học toàn diện").append("\n");
        prompt.append("- Thời lượng: ").append(course.getDuration() != null ? course.getDuration() : "Không xác định").append("\n");
        
        if (course.getCourseType() != null) {
            prompt.append("- Loại khóa học: ").append(course.getCourseType().getCourseTypeName()).append("\n");
        }
        
        // Yêu cầu cụ thể
        prompt.append("\nYÊU CẦU:\n");
        prompt.append("Hãy viết một giải thích chi tiết (200-300 từ) bằng tiếng Việt về:\n");
        prompt.append("1. Tại sao khóa học này phù hợp với thông số cơ thể của người dùng\n");
        prompt.append("2. Khóa học sẽ giúp đạt được mục tiêu như thế nào\n");
        prompt.append("3. Cách khóa học phù hợp với lịch sử tập luyện và tần suất mong muốn\n");
        prompt.append("4. Lưu ý đặc biệt về sức khỏe (nếu có)\n");
        prompt.append("5. Kết quả có thể mong đợi sau khi hoàn thành khóa học\n\n");
        prompt.append("Hãy viết theo tone chuyên nghiệp nhưng thân thiện, dễ hiểu.");
        
        return prompt.toString();
    }
    
    private String callClaudeForRecommendationExplanation(String prompt) {
        try {
            // Rate limiting check
            synchronized (claudeApiLock) {
                long currentTime = System.currentTimeMillis();
                long timeSinceLastCall = currentTime - lastCallTimestamp;
                if (timeSinceLastCall < MIN_CALL_INTERVAL_MS) {
                    Thread.sleep(MIN_CALL_INTERVAL_MS - timeSinceLastCall);
                }
                lastCallTimestamp = System.currentTimeMillis();
            }

            // Thiết lập header
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", claudeApiKey);
            headers.set("anthropic-version", claudeApiVersion);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", claudeModel != null ? claudeModel : "claude-3-haiku-20240307");
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            // Tạo messages
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);
            messages.add(userMessage);
            requestBody.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            logger.info("Calling Claude API for recommendation explanation");

            ResponseEntity<Map> response = restTemplate.exchange(claudeApiUrl, HttpMethod.POST, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                Map responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("content")) {
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseBody.get("content");
                    if (!contentList.isEmpty()) {
                        Map<String, Object> firstContent = contentList.get(0);
                        if (firstContent.containsKey("text")) {
                            return (String) firstContent.get("text");
                        }
                    }
                }
            }
            
            // Fallback nếu API call thất bại
            return "Không thể tạo giải thích chi tiết từ AI. Khóa học được đề xuất dựa trên phân tích thông số cơ thể và mục tiêu tập luyện của bạn.";
            
        } catch (Exception e) {
            logger.error("Error calling Claude for recommendation explanation: " + e.getMessage(), e);
            return "Không thể tạo giải thích chi tiết từ AI. Khóa học được đề xuất dựa trên phân tích thông số cơ thể và mục tiêu tập luyện của bạn.";
        }
    }
    
    // ==================== DRAFT DATA MANAGEMENT ENDPOINTS ====================
    
    /**
     * Lưu tạm dữ liệu form wizard để user có thể quay lại chỉnh sửa
     */
    @PostMapping("/save-draft")
    public ResponseEntity<?> saveDraft(@RequestBody FormWizardDraftDTO draftData) {
        try {
            if (draftData.getCustomerId() == null) {
                return ResponseEntity.badRequest().body("Customer ID is required");
            }
            
            // Kiểm tra customer tồn tại
            Customer customer = customerService.findById(draftData.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // Set timestamps
            String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            if (draftData.getCreatedAt() == null) {
                draftData.setCreatedAt(currentTime);
            }
            draftData.setUpdatedAt(currentTime);
            
            // Lưu vào in-memory storage (có thể thay bằng database sau)
            draftStorage.put(draftData.getCustomerId(), draftData);
            
            logger.info("Saved draft data for customer {} at step {}", 
                draftData.getCustomerId(), draftData.getCurrentStep());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Draft data saved successfully");
            response.put("customerId", draftData.getCustomerId());
            response.put("currentStep", draftData.getCurrentStep());
            response.put("savedAt", currentTime);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving draft data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error saving draft data: " + e.getMessage());
        }
    }
    
    /**
     * Lấy dữ liệu draft đã lưu của customer
     */
    @GetMapping("/get-draft/{customerId}")
    public ResponseEntity<?> getDraft(@PathVariable Integer customerId) {
        try {
            // Kiểm tra customer tồn tại
            Customer customer = customerService.findById(customerId);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            FormWizardDraftDTO draftData = draftStorage.get(customerId);
            
            if (draftData == null) {
                // Không có draft data
                Map<String, Object> response = new HashMap<>();
                response.put("status", "no_draft");
                response.put("message", "No draft data found for this customer");
                response.put("customerId", customerId);
                response.put("hasData", false);
                
                return ResponseEntity.ok(response);
            }
            
            logger.info("Retrieved draft data for customer {} at step {}", 
                customerId, draftData.getCurrentStep());
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "found");
            response.put("message", "Draft data retrieved successfully");
            response.put("hasData", true);
            response.put("draftData", draftData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error retrieving draft data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving draft data: " + e.getMessage());
        }
    }
    
    /**
     * Xóa dữ liệu draft khi user hoàn thành form hoặc muốn bắt đầu lại
     */
    @DeleteMapping("/clear-draft/{customerId}")
    public ResponseEntity<?> clearDraft(@PathVariable Integer customerId) {
        try {
            FormWizardDraftDTO removedData = draftStorage.remove(customerId);
            
            Map<String, Object> response = new HashMap<>();
            if (removedData != null) {
                response.put("status", "success");
                response.put("message", "Draft data cleared successfully");
                response.put("customerId", customerId);
                
                logger.info("Cleared draft data for customer {}", customerId);
            } else {
                response.put("status", "no_data");
                response.put("message", "No draft data found to clear");
                response.put("customerId", customerId);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing draft data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error clearing draft data: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật draft data từng bước
     */
    @PutMapping("/update-draft-step")
    public ResponseEntity<?> updateDraftStep(@RequestBody Map<String, Object> updateData) {
        try {
            Integer customerId = (Integer) updateData.get("customerId");
            Integer step = (Integer) updateData.get("step");
            Map<String, Object> stepData = (Map<String, Object>) updateData.get("stepData");
            
            if (customerId == null || step == null || stepData == null) {
                return ResponseEntity.badRequest()
                        .body("customerId, step, and stepData are required");
            }
            
            // Lấy draft data hiện tại hoặc tạo mới
            FormWizardDraftDTO draftData = draftStorage.getOrDefault(customerId, new FormWizardDraftDTO());
            if (draftData.getCustomerId() == null) {
                draftData.setCustomerId(customerId);
                draftData.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            }
            
            // Cập nhật current step
            draftData.setCurrentStep(step);
            draftData.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            // Cập nhật data theo từng step
            updateDraftDataByStep(draftData, step, stepData);
            
            // Lưu lại
            draftStorage.put(customerId, draftData);
            
            logger.info("Updated draft data for customer {} at step {}", customerId, step);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Draft step updated successfully");
            response.put("customerId", customerId);
            response.put("step", step);
            response.put("updatedAt", draftData.getUpdatedAt());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating draft step: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating draft step: " + e.getMessage());
        }
    }
    
    /**
     * Helper method để cập nhật draft data theo từng step
     */
    private void updateDraftDataByStep(FormWizardDraftDTO draftData, Integer step, Map<String, Object> stepData) {
        switch (step) {
            case 1:
                // Step 1: Thông tin cơ bản
                if (stepData.get("age") != null) draftData.setAge((Integer) stepData.get("age"));
                if (stepData.get("gender") != null) draftData.setGender((String) stepData.get("gender"));
                if (stepData.get("height") != null) draftData.setHeight(((Number) stepData.get("height")).doubleValue());
                if (stepData.get("weight") != null) draftData.setWeight(((Number) stepData.get("weight")).doubleValue());
                if (stepData.get("bmi") != null) draftData.setBmi(((Number) stepData.get("bmi")).doubleValue());
                break;
                
            case 2:
                // Step 2: Đo lường cơ thể
                if (stepData.get("chest_circumference") != null) {
                    draftData.setChest_circumference(((Number) stepData.get("chest_circumference")).doubleValue());
                }
                if (stepData.get("waist_circumference") != null) {
                    draftData.setWaist_circumference(((Number) stepData.get("waist_circumference")).doubleValue());
                }
                break;
                
            case 3:
                // Step 3: Mục tiêu tập luyện
                if (stepData.get("training_goals") != null) {
                    draftData.setTraining_goals((String) stepData.get("training_goals"));
                }
                break;
                
            case 4:
                // Step 4: Lịch sử tập luyện
                if (stepData.get("training_history") != null) {
                    draftData.setTraining_history((String) stepData.get("training_history"));
                }
                if (stepData.get("training_frequency") != null) {
                    draftData.setTraining_frequency((String) stepData.get("training_frequency"));
                }
                break;
                
            case 5:
                // Step 5: Thông tin nghề nghiệp và sức khỏe
                if (stepData.get("occupation") != null) {
                    draftData.setOccupation((String) stepData.get("occupation"));
                }
                if (stepData.get("work_environment") != null) {
                    draftData.setWork_environment((String) stepData.get("work_environment"));
                }
                break;
                
            case 6:
                // Step 6: Phân tích hình ảnh
                if (stepData.get("body_type") != null) {
                    draftData.setBody_type((String) stepData.get("body_type"));
                }
                if (stepData.get("imageAnalysis") != null) {
                    draftData.setImageAnalysis((String) stepData.get("imageAnalysis"));
                }
                if (stepData.get("imageRecommendations") != null) {
                    draftData.setImageRecommendations((String) stepData.get("imageRecommendations"));
                }
                if (stepData.get("imageBase64") != null) {
                    draftData.setImageBase64((String) stepData.get("imageBase64"));
                }
                break;
                
            case 7:
                // Step 7: Khóa học được đề xuất
                if (stepData.get("recommendedCourseId") != null) {
                    draftData.setRecommendedCourseId((Integer) stepData.get("recommendedCourseId"));
                }
                if (stepData.get("recommendedCourseName") != null) {
                    draftData.setRecommendedCourseName((String) stepData.get("recommendedCourseName"));
                }
                break;
                
            default:
                logger.warn("Unknown step: {}", step);
                break;
        }
    }
    
    /**
     * Kiểm tra xem customer có draft data không
     */
    @GetMapping("/check-draft-status/{customerId}")
    public ResponseEntity<?> checkDraftStatus(@PathVariable Integer customerId) {
        try {
            FormWizardDraftDTO draftData = draftStorage.get(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            
            if (draftData == null) {
                response.put("hasDraft", false);
                response.put("currentStep", 1);
                response.put("message", "No draft found, starting fresh");
            } else {
                response.put("hasDraft", true);
                response.put("currentStep", draftData.getCurrentStep() != null ? draftData.getCurrentStep() : 1);
                response.put("lastUpdated", draftData.getUpdatedAt());
                response.put("message", "Draft found, can resume from step " + draftData.getCurrentStep());
                
                // Thống kê các step đã hoàn thành
                Map<String, Boolean> completedSteps = new HashMap<>();
                completedSteps.put("step1", draftData.getAge() != null && draftData.getGender() != null);
                completedSteps.put("step2", draftData.getChest_circumference() != null || draftData.getWaist_circumference() != null);
                completedSteps.put("step3", draftData.getTraining_goals() != null);
                completedSteps.put("step4", draftData.getTraining_history() != null || draftData.getTraining_frequency() != null);
                completedSteps.put("step5", draftData.getOccupation() != null || draftData.getWork_environment() != null);
                completedSteps.put("step6", draftData.getBody_type() != null || draftData.getImageAnalysis() != null);
                completedSteps.put("step7", draftData.getRecommendedCourseId() != null);
                
                response.put("completedSteps", completedSteps);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error checking draft status: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking draft status: " + e.getMessage());
        }
    }
    
    /**
     * Chuyển đổi draft data thành UserFormDataDTO để process
     */
    @PostMapping("/convert-draft-to-form/{customerId}")
    public ResponseEntity<?> convertDraftToForm(@PathVariable Integer customerId) {
        try {
            FormWizardDraftDTO draftData = draftStorage.get(customerId);
            
            if (draftData == null) {
                return ResponseEntity.badRequest().body("No draft data found for customer");
            }
            
            // Tạo UserFormDataDTO từ draft data
            UserFormDataDTO formData = new UserFormDataDTO();
            
            // Map all fields
            formData.setCustomerId(customerId);
            formData.setAge(draftData.getAge());
            formData.setGender(draftData.getGender());
            formData.setHeight(draftData.getHeight());
            formData.setWeight(draftData.getWeight());
            formData.setBmi(draftData.getBmi());
            formData.setChest_circumference(draftData.getChest_circumference());
            formData.setWaist_circumference(draftData.getWaist_circumference());
            formData.setTraining_goals(draftData.getTraining_goals());
            formData.setTraining_history(draftData.getTraining_history());
            formData.setTraining_frequency(draftData.getTraining_frequency());
            formData.setOccupation(draftData.getOccupation());
            formData.setWork_environment(draftData.getWork_environment());
            formData.setBody_type(draftData.getBody_type());
            
            // Process form data như bình thường
            ResponseEntity<RecommendationDTO> processResult = processFormData(formData);
            
            logger.info("Converted draft data to form and processed for customer {}", customerId);
            
            // Trả về kết quả process cùng với thông tin draft
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Draft converted and processed successfully");
            response.put("processResult", processResult.getBody());
            response.put("draftData", draftData); // Để frontend có thể hiển thị lại Step 6 analysis nếu cần
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error converting draft to form: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error converting draft to form: " + e.getMessage());
        }
    }
    
    /**
     * ENDPOINT TỔNG HỢP: Auto-save và load draft data cho frontend
     * Frontend chỉ cần call endpoint này khi user chuyển step
     */
    @PostMapping("/auto-save-and-get-step/{customerId}/{step}")
    public ResponseEntity<?> autoSaveAndGetStep(@PathVariable Integer customerId, 
                                                @PathVariable Integer step,
                                                @RequestBody(required = false) Map<String, Object> currentStepData) {
        try {
            // Step 1: Save current step data (nếu có)
            if (currentStepData != null && !currentStepData.isEmpty()) {
                FormWizardDraftDTO draftData = draftStorage.getOrDefault(customerId, new FormWizardDraftDTO());
                if (draftData.getCustomerId() == null) {
                    draftData.setCustomerId(customerId);
                    draftData.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                }
                
                // Lưu data của step hiện tại
                Integer currentStep = (Integer) currentStepData.get("fromStep");
                if (currentStep != null) {
                    updateDraftDataByStep(draftData, currentStep, currentStepData);
                    draftData.setCurrentStep(step); // Update to target step
                    draftData.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    draftStorage.put(customerId, draftData);
                    
                    logger.info("Auto-saved step {} data for customer {}", currentStep, customerId);
                }
            }
            
            // Step 2: Load draft data cho step đích
            FormWizardDraftDTO draftData = draftStorage.get(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("targetStep", step);
            response.put("status", "success");
            
            if (draftData == null) {
                // Không có draft data
                response.put("hasData", false);
                response.put("stepData", new HashMap<>());
                response.put("message", "No draft data, starting fresh");
            } else {
                // Có draft data - trả về data của step đích
                response.put("hasData", true);
                response.put("stepData", getStepDataFromDraft(draftData, step));
                response.put("currentStep", draftData.getCurrentStep());
                response.put("lastUpdated", draftData.getUpdatedAt());
                response.put("message", "Draft data loaded successfully");
                
                // Thống kê progress
                Map<String, Boolean> completedSteps = new HashMap<>();
                completedSteps.put("step1", draftData.getAge() != null && draftData.getGender() != null);
                completedSteps.put("step2", draftData.getChest_circumference() != null || draftData.getWaist_circumference() != null);
                completedSteps.put("step3", draftData.getTraining_goals() != null);
                completedSteps.put("step4", draftData.getTraining_history() != null || draftData.getTraining_frequency() != null);
                completedSteps.put("step5", draftData.getOccupation() != null || draftData.getWork_environment() != null);
                completedSteps.put("step6", draftData.getBody_type() != null || draftData.getImageAnalysis() != null);
                completedSteps.put("step7", draftData.getRecommendedCourseId() != null);
                
                response.put("completedSteps", completedSteps);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in auto-save-and-get-step: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error in auto-save-and-get-step: " + e.getMessage());
        }
    }
    
    /**
     * Helper method để lấy data của step cụ thể từ draft
     */
    private Map<String, Object> getStepDataFromDraft(FormWizardDraftDTO draftData, Integer step) {
        Map<String, Object> stepData = new HashMap<>();
        
        switch (step) {
            case 1:
                // Step 1: Thông tin cơ bản
                if (draftData.getAge() != null) stepData.put("age", draftData.getAge());
                if (draftData.getGender() != null) stepData.put("gender", draftData.getGender());
                if (draftData.getHeight() != null) stepData.put("height", draftData.getHeight());
                if (draftData.getWeight() != null) stepData.put("weight", draftData.getWeight());
                if (draftData.getBmi() != null) stepData.put("bmi", draftData.getBmi());
                break;
                
            case 2:
                // Step 2: Đo lường cơ thể
                if (draftData.getChest_circumference() != null) {
                    stepData.put("chest_circumference", draftData.getChest_circumference());
                }
                if (draftData.getWaist_circumference() != null) {
                    stepData.put("waist_circumference", draftData.getWaist_circumference());
                }
                break;
                
            case 3:
                // Step 3: Mục tiêu tập luyện
                if (draftData.getTraining_goals() != null) {
                    stepData.put("training_goals", draftData.getTraining_goals());
                }
                break;
                
            case 4:
                // Step 4: Lịch sử tập luyện
                if (draftData.getTraining_history() != null) {
                    stepData.put("training_history", draftData.getTraining_history());
                }
                if (draftData.getTraining_frequency() != null) {
                    stepData.put("training_frequency", draftData.getTraining_frequency());
                }
                break;
                
            case 5:
                // Step 5: Thông tin nghề nghiệp và sức khỏe
                if (draftData.getOccupation() != null) {
                    stepData.put("occupation", draftData.getOccupation());
                }
                if (draftData.getWork_environment() != null) {
                    stepData.put("work_environment", draftData.getWork_environment());
                }
                break;
                
            case 6:
                // Step 6: Phân tích hình ảnh
                if (draftData.getBody_type() != null) {
                    stepData.put("body_type", draftData.getBody_type());
                }
                if (draftData.getImageAnalysis() != null) {
                    stepData.put("imageAnalysis", draftData.getImageAnalysis());
                }
                if (draftData.getImageRecommendations() != null) {
                    stepData.put("imageRecommendations", draftData.getImageRecommendations());
                }
                if (draftData.getImageBase64() != null) {
                    stepData.put("imageBase64", draftData.getImageBase64());
                }
                break;
                
            case 7:
                // Step 7: Khóa học được đề xuất
                if (draftData.getRecommendedCourseId() != null) {
                    stepData.put("recommendedCourseId", draftData.getRecommendedCourseId());
                }
                if (draftData.getRecommendedCourseName() != null) {
                    stepData.put("recommendedCourseName", draftData.getRecommendedCourseName());
                }
                break;
                
            default:
                logger.warn("Unknown step: {}", step);
                break;
        }
        
        return stepData;
    }
    
    /**
     * ENDPOINT ĐƠN GIẢN: Chỉ load data cho step cụ thể (dùng khi component mount)
     */
    @GetMapping("/load-step-data/{customerId}/{step}")
    public ResponseEntity<?> loadStepData(@PathVariable Integer customerId, @PathVariable Integer step) {
        try {
            FormWizardDraftDTO draftData = draftStorage.get(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("step", step);
            response.put("status", "success");
            
            if (draftData == null) {
                response.put("hasData", false);
                response.put("stepData", new HashMap<>());
                response.put("message", "No saved data for this step");
            } else {
                Map<String, Object> stepData = getStepDataFromDraft(draftData, step);
                response.put("hasData", !stepData.isEmpty());
                response.put("stepData", stepData);
                response.put("message", stepData.isEmpty() ? "No data for this step" : "Step data loaded successfully");
                response.put("lastUpdated", draftData.getUpdatedAt());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error loading step data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading step data: " + e.getMessage());
        }
    }
}