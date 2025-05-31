package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.*;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.service.*;
import com.capstone.wellnessnavigatorgym.utils.BuildDecisionTree;
import com.capstone.wellnessnavigatorgym.utils.C45DecisionTree;
import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
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
import java.util.concurrent.ThreadLocalRandom;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.Base64;

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
    private C45DecisionTree c45DecisionTree;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${claude.api.key:sk-ant-api03-G47aLO10DvYoxHuaEnCRL4y_gGTevFEQPLvkHU81DYujJ81lKUMuX28cyJIiD5_z1KpuDpiLzg1QLppX17F9Q-TMiqkgAA}")
    private String claudeApiKey;

    @Value("${claude.api.url:https://api.anthropic.com/v1/messages}")
    private String claudeApiUrl;

    @Value("${claude.api.version:2023-06-01}")
    private String claudeApiVersion;

    @Value("${claude.api.model:claude-3-5-sonnet-20241022}")
    private String claudeModel;

    // XML file path is no longer needed as the system now uses database training data
    // private static final String XML_FILE_PATH = "D:/DATN/CODE/start-gym-BE/start-gym-BE/wellness-navigator-gym/src/main/resources/static/DataTraining.xml";
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
        
        // Save the recommended course if available - USING FULL COURSE OBJECT
        if (dto.getRecommendedCourseId() != null) {
            try {
                // Lấy đối tượng Course đầy đủ từ database thay vì chỉ tạo Course với ID
                Course course = courseService.findCourseById(dto.getRecommendedCourseId());
                userDataAi.setCourse(course);
                logger.info("Retrieved and set full course object with ID: {} and name: {}", 
                    course.getCourseId(), course.getCourseName());
            } catch (Exception e) {
                logger.error("Failed to retrieve course with ID: {}. Error: {}", dto.getRecommendedCourseId(), e.getMessage());
                // Fallback: Vẫn tạo Course chỉ với ID trong trường hợp lỗi
                Course course = new Course(dto.getRecommendedCourseId());
                userDataAi.setCourse(course);
                logger.warn("Using fallback: Set course with ID only: {}", dto.getRecommendedCourseId());
            }
        } else {
            logger.warn("No recommendedCourseId provided in DTO - course will be null");
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
            logger.info("=== ENHANCED C4.5 RECOMMENDATION SYSTEM ===");
            logger.info("Processing recommendation request for customer: {}", userDataDTO.getCustomerId());
            
            // Lấy dữ liệu training và courses
            List<TrackDataAi> allTrainingData = trackDataAiService.getAllTrackDataAi();
            List<Course> allCourses = courseService.findAll();
            
            logger.info("Loaded {} training records and {} courses", allTrainingData.size(), allCourses.size());
            
            // Chuẩn bị dữ liệu cho C4.5
            c45DecisionTree.setTrainingData(allTrainingData);
            c45DecisionTree.setCourses(allCourses);
            
            // Xây dựng cây quyết định C4.5 cải thiện
            TreeNode decisionTreeRoot = c45DecisionTree.buildDecisionTree();
            
            Course recommendedCourse = null;
            String recommendationSource = "fallback";
            
            if (decisionTreeRoot != null) {
                logger.info("✅ C4.5 decision tree built successfully");
                
                // Chuẩn bị dữ liệu người dùng cho traversal
                Map<String, Object> userData = extractUserDataFromTrackDataAi(userDataDTO);
                
                // Log chi tiết dữ liệu người dùng
                logger.info("User data for traversal:");
                userData.forEach((key, value) -> 
                    logger.info("- {}: {}", key, value));
                
                // Duyệt cây quyết định
                final Course finalRecommendedCourse = c45DecisionTree.traverseDecisionTree(decisionTreeRoot, userData);
                recommendedCourse = finalRecommendedCourse;
                
                if (recommendedCourse != null) {
                    recommendationSource = "c45_decision_tree";
                    logger.info("🎯 C4.5 Decision Tree SUCCESS: Recommended course {} - {}", 
                        recommendedCourse.getCourseId(), recommendedCourse.getCourseName());
                } else {
                    logger.warn("⚠️ C4.5 Decision Tree traversal returned null");
                }
            } else {
                logger.warn("⚠️ C4.5 Decision Tree building failed");
            }
            
            // Fallback logic nếu C4.5 không trả về kết quả
            if (recommendedCourse == null) {
                logger.info("🔄 Using intelligent fallback system...");
                final Course fallbackCourse = getDefaultCourseByTrainingGoals(userDataDTO, allCourses);
                recommendedCourse = fallbackCourse;
                recommendationSource = "intelligent_fallback";
                
                if (recommendedCourse != null) {
                    logger.info("🎯 Intelligent Fallback SUCCESS: Recommended course {} - {}", 
                        recommendedCourse.getCourseId(), recommendedCourse.getCourseName());
                } else {
                    logger.error("❌ All recommendation methods failed!");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new RecommendationDTO(null, "❌ Unable to generate course recommendation"));
                }
            }
            
            // Tạo response
            String message = generateRecommendationMessage(
                "c45_decision_tree".equals(recommendationSource), 
                recommendationSource
            );
            
            RecommendationDTO response = new RecommendationDTO(recommendedCourse, message);
            
            // Lưu dữ liệu người dùng vào database
            try {
                Integer customerId = userDataDTO.getCustomerId();
                if (customerId != null) {
                    Customer customer = customerService.findById(customerId);
                    if (customer != null) {
                        // Kiểm tra duplicate records trước khi tạo mới
                        final Course finalCourseForFilter = recommendedCourse;
                        List<UserDataAi> existingUserData = userDataAiService.findByCustomerId(customerId);
                        List<UserDataAi> existingRecords = existingUserData.stream()
                            .filter(record -> record.getCourse() != null && 
                                            record.getCourse().getCourseId().equals(finalCourseForFilter.getCourseId()))
                            .collect(Collectors.toList());
                        
                        Integer userDataId;
                        if (!existingRecords.isEmpty()) {
                            // Cập nhật bản ghi hiện có
                            UserDataAi existingRecord = existingRecords.get(0);
                            updateExistingUserDataAi(existingRecord, userDataDTO);
                            userDataId = existingRecord.getUser_data_id();
                            logger.info("Updated existing UserDataAi record: {}", userDataId);
                        } else {
                            // Tạo bản ghi mới
                            UserDataAi userDataAi = createUserDataAiFromDTO(userDataDTO, customer);
                            userDataAi.setCourse(recommendedCourse);
                            userDataAi.setEffective(true); // Tất cả bản ghi mới đều bắt đầu với effective=true
                            
                            UserDataAi savedData = userDataAiService.saveUserDataAi(userDataAi);
                            userDataId = savedData.getUser_data_id();
                            logger.info("Created new UserDataAi record: {}", userDataId);
                        }
                        
                        response = new RecommendationDTO(recommendedCourse, message, userDataId);
                        
                        // Thêm formData vào response
                        userDataDTO.setRecommendedCourseId(recommendedCourse.getCourseId());
                        response.setFormData(userDataDTO);
                    }
                }
            } catch (Exception e) {
                logger.error("Error saving user data: {}", e.getMessage());
                // Không fail request nếu không save được data
            }
            
            logger.info("🎉 Recommendation completed successfully using: {}", recommendationSource);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error in recommendation system: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RecommendationDTO(null, "❌ System error: " + e.getMessage()));
        }
    }

    /**
     * Lấy khóa học mặc định dựa trên training goals với logic thông minh
     */
    private Course getDefaultCourseByTrainingGoals(UserDataDTO userDataDTO, List<Course> allCourses) {
        String trainingGoals = userDataDTO.getTraining_goals();
        logger.info("Looking for course based on training goals: {}", trainingGoals);
        
        // Mapping training goals với course IDs phù hợp - CẢI THIỆN
        List<Integer> suggestedCourseIds = new ArrayList<>();
        
        if (trainingGoals != null) {
            String goals = trainingGoals.toLowerCase();
            
            if (goals.contains("strength") || goals.contains("1")) {
                // Strength Training courses
                suggestedCourseIds.addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 25, 26, 27, 28));
            } else if (goals.contains("cardiovascular") || goals.contains("cardio") || goals.contains("2")) {
                // Cardiovascular Training courses  
                suggestedCourseIds.addAll(Arrays.asList(9, 10, 11, 12, 13, 14, 19, 20, 21, 22, 29, 30, 31, 32));
            } else if (goals.contains("flexibility") || goals.contains("balance") || goals.contains("3")) {
                // Flexibility and Balance courses
                suggestedCourseIds.addAll(Arrays.asList(33, 34, 35, 36, 37, 38, 39, 40, 47, 48, 49, 50));
            } else if (goals.contains("endurance") || goals.contains("4")) {
                // Train strength and endurance courses
                suggestedCourseIds.addAll(Arrays.asList(23, 24, 25, 26, 27, 28, 41, 42, 43, 44, 51, 52));
            } else if (goals.contains("mind") || goals.contains("wellness") || goals.contains("5")) {
                // Mind-Body Wellness courses
                suggestedCourseIds.addAll(Arrays.asList(45, 46, 47, 48, 49, 50, 57, 58, 59, 60));
            } else if (goals.contains("functional") || goals.contains("6")) {
                // Functional Training courses
                suggestedCourseIds.addAll(Arrays.asList(53, 54, 55, 56, 61, 62, 63, 64, 65, 66));
            } else if (goals.contains("weight") || goals.contains("loss") || goals.contains("7")) {
                // Weight Loss courses - CẢI THIỆN với course IDs mới
                suggestedCourseIds.addAll(Arrays.asList(67, 68, 69, 70, 71, 72, 73, 29, 30, 31, 32, 9, 10, 11, 12));
            }
        }
        
        // Nếu không tìm thấy training goals phù hợp, sử dụng courses phù hợp với người mới bắt đầu
        if (suggestedCourseIds.isEmpty()) {
            logger.info("No specific training goals found, using beginner-friendly courses");
            suggestedCourseIds.addAll(Arrays.asList(1, 9, 17, 25, 33, 41, 49, 57)); // Các khóa học cơ bản
        }
        
        // LOGIC THÔNG MINH: Chọn course dựa trên profile người dùng
        Course selectedCourse = selectBestCourseFromProfile(suggestedCourseIds, userDataDTO, allCourses);
        if (selectedCourse != null) {
            logger.info("🎯 Smart course selection: {} - {} based on user profile", 
                selectedCourse.getCourseId(), selectedCourse.getCourseName());
            return selectedCourse;
        }
        
        // Fallback: Tìm khóa học đầu tiên có sẵn trong hệ thống từ danh sách gợi ý
        for (Integer courseId : suggestedCourseIds) {
            Course course = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
                
            if (course != null) {
                logger.info("🎯 Found course by training goals: {} - {}", course.getCourseId(), course.getCourseName());
                return course;
            }
        }
        
        // Fallback cuối cùng thông minh: Tìm course phù hợp nhất thay vì luôn trả về course đầu tiên
        logger.warn("⚠️ No specific course found for training goals, using intelligent fallback");
        return selectIntelligentFallbackCourse(userDataDTO, allCourses);
    }
    
    /**
     * Chọn khóa học tốt nhất dựa trên profile người dùng
     */
    private Course selectBestCourseFromProfile(List<Integer> courseIds, UserDataDTO userDataDTO, List<Course> allCourses) {
        String trainingHistory = userDataDTO.getTraining_history();
        String bodyType = userDataDTO.getBody_type();
        Double bmi = userDataDTO.getBmi();
        Integer age = userDataDTO.getAge();
        
        logger.debug("Selecting course based on profile: history={}, bodyType={}, bmi={}, age={}", 
            trainingHistory, bodyType, bmi, age);
        
        for (Integer courseId : courseIds) {
            Course course = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
                
            if (course != null && isCourseSuitableForUserProfile(course, userDataDTO)) {
                logger.debug("Selected course {} as suitable for user profile", courseId);
                return course;
            }
        }
        
        return null;
    }
    
    /**
     * Kiểm tra khóa học có phù hợp với profile người dùng không
     */
    private boolean isCourseSuitableForUserProfile(Course course, UserDataDTO userDataDTO) {
        String trainingHistory = userDataDTO.getTraining_history();
        Integer age = userDataDTO.getAge();
        Double bmi = userDataDTO.getBmi();
        
        // Logic cho người mới bắt đầu (Never, Occasionally)
        if ("Never".equals(trainingHistory) || "Occasionally".equals(trainingHistory)) {
            // Ưu tiên courses cơ bản (ID thấp hơn hoặc courses dành cho beginners)
            return course.getCourseId() <= 30; // Ví dụ: courses 1-30 là cơ bản
        }
        
        // Logic cho người có kinh nghiệm (Regularly, Advanced)
        if ("Regularly".equals(trainingHistory) || "Advanced".equals(trainingHistory)) {
            // Có thể chọn courses nâng cao hơn
            return course.getCourseId() > 15; // Cho phép courses từ 16 trở lên
        }
        
        // Logic dựa trên độ tuổi
        if (age != null) {
            if (age < 25) {
                // Người trẻ: có thể chọn courses năng động hơn
                return course.getCourseId() != 45 && course.getCourseId() != 46; // Tránh mind-body wellness
            } else if (age > 40) {
                // Người lớn tuổi: ưu tiên low-impact exercises
                return course.getCourseId() >= 33; // Ưu tiên flexibility, mind-body
            }
        }
        
        // Logic dựa trên BMI
        if (bmi != null) {
            if (bmi > 30) {
                // Người béo phì: ưu tiên cardio và weight loss
                return course.getCourseId() >= 67 || // Weight loss courses
                       (course.getCourseId() >= 9 && course.getCourseId() <= 14); // Cardio courses
            } else if (bmi < 18.5) {
                // Người gầy: ưu tiên strength training
                return course.getCourseId() >= 1 && course.getCourseId() <= 8; // Strength courses
            }
        }
        
        return true; // Default: chấp nhận course
    }
    
    /**
     * Chọn khóa học fallback thông minh dựa trên profile người dùng
     */
    private Course selectIntelligentFallbackCourse(UserDataDTO userDataDTO, List<Course> allCourses) {
        String trainingHistory = userDataDTO.getTraining_history();
        Integer age = userDataDTO.getAge();
        Double bmi = userDataDTO.getBmi();
        String bodyType = userDataDTO.getBody_type();
        
        logger.info("Selecting intelligent fallback course for profile: age={}, bmi={}, history={}, bodyType={}", 
            age, bmi, trainingHistory, bodyType);
        
        // Priority 1: Dựa trên BMI
        if (bmi != null) {
            if (bmi > 30) {
                // Người béo phì -> Weight Loss courses
                Course weightLossCourse = findAvailableCourse(Arrays.asList(70, 71, 72, 73, 9, 10), allCourses);
                if (weightLossCourse != null) {
                    logger.info("🎯 Selected weight loss course for high BMI: {}", weightLossCourse.getCourseId());
                    return weightLossCourse;
                }
            } else if (bmi < 18.5) {
                // Người gầy -> Strength Training
                Course strengthCourse = findAvailableCourse(Arrays.asList(1, 2, 3, 4, 5), allCourses);
                if (strengthCourse != null) {
                    logger.info("🎯 Selected strength course for low BMI: {}", strengthCourse.getCourseId());
                    return strengthCourse;
                }
            }
        }
        
        // Priority 2: Dựa trên độ tuổi
        if (age != null) {
            if (age < 25) {
                // Người trẻ -> Dynamic courses
                Course dynamicCourse = findAvailableCourse(Arrays.asList(9, 10, 11, 12, 23, 24), allCourses);
                if (dynamicCourse != null) {
                    logger.info("🎯 Selected dynamic course for young age: {}", dynamicCourse.getCourseId());
                    return dynamicCourse;
                }
            } else if (age > 40) {
                // Người lớn tuổi -> Low-impact courses
                Course lowImpactCourse = findAvailableCourse(Arrays.asList(33, 34, 35, 45, 46), allCourses);
                if (lowImpactCourse != null) {
                    logger.info("🎯 Selected low-impact course for older age: {}", lowImpactCourse.getCourseId());
                    return lowImpactCourse;
                }
            }
        }
        
        // Priority 3: Dựa trên training history
        if ("Never".equals(trainingHistory)) {
            Course beginnerCourse = findAvailableCourse(Arrays.asList(1, 9, 17, 25, 33), allCourses);
            if (beginnerCourse != null) {
                logger.info("🎯 Selected beginner course for training history 'Never': {}", beginnerCourse.getCourseId());
                return beginnerCourse;
            }
        }
        
        // Ultimate fallback: Course đầu tiên có sẵn (chỉ khi tất cả logic khác thất bại)
        if (!allCourses.isEmpty()) {
            Course ultimateFallback = allCourses.get(0);
            logger.warn("⚠️ Using ultimate fallback course: {} - {} (This should rarely happen!)", 
                ultimateFallback.getCourseId(), ultimateFallback.getCourseName());
            return ultimateFallback;
        }
        
        logger.error("❌ No courses available in system!");
        return null;
    }
    
    /**
     * Tìm khóa học có sẵn đầu tiên từ danh sách ưu tiên
     */
    private Course findAvailableCourse(List<Integer> priorityCourseIds, List<Course> allCourses) {
        for (Integer courseId : priorityCourseIds) {
            Course course = allCourses.stream()
                .filter(c -> c.getCourseId().equals(courseId))
                .findFirst()
                .orElse(null);
            
            if (course != null) {
                return course;
            }
        }
        return null;
    }

    /**
     * Tạo thông điệp mô tả nguồn gợi ý
     */
    private String generateRecommendationMessage(boolean usedDecisionTree, String source) {
        switch (source) {
            case "c45_decision_tree":
                return "Course recommendations generated successfully using advanced C4.5 AI decision tree algorithm";
            case "training_goals_fallback": 
                return "Course recommendations generated based on your training goals (C4.5 decision tree data insufficient)";
            case "error_fallback":
                return "Course recommendations generated using training goals fallback (AI system error)";
            case "ultimate_fallback":
                return "⚠️ Using default course - unable to determine specific recommendations";
            default:
                return "Course recommendations generated using intelligent matching system";
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

            // Nếu có customerId, lưu kết quả vào draftStorage
            if (customerId != null) {
                try {
                    // Lấy hoặc tạo draft cho customer này
                    FormWizardDraftDTO draft = draftStorage.get(customerId);
                    if (draft == null) {
                        draft = new FormWizardDraftDTO();
                        draft.setCustomerId(customerId);
                        draft.setCreatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    }
                    
                    // Cập nhật thông tin phân tích cơ thể
                    draft.setBody_type(response.getBodyType());
                    draft.setImageAnalysis(response.getAnalysis());
                    draft.setImageRecommendations(response.getRecommendations());
                    draft.setCurrentStep(6); // Đang ở bước phân tích hình ảnh
                    draft.setUpdatedAt(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    
                    // Lưu ảnh dưới dạng base64 để hiển thị lại
                    try {
                        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
                        draft.setImageBase64(base64Image);
                    } catch (Exception e) {
                        logger.error("Failed to convert image to base64: {}", e.getMessage());
                    }
                    
                    // Lưu vào draft storage
                    draftStorage.put(customerId, draft);
                    logger.info("Image analysis result saved to draft storage for customer ID: {}", customerId);
                } catch (Exception e) {
                    logger.error("Error saving to draft storage: {}", e.getMessage());
                }
                
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
                    Integer userDataId = recommendation.getUserDataId();
                    response.setUserDataId(userDataId);
                    logger.info("Setting userDataId: {} in image analysis response", userDataId);
                } else {
                    logger.warn("No course was recommended based on image analysis");
                }
            } else {
                logger.warn("No customerId provided - image analysis results will not be persisted");
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
        String systemPrompt = "Bạn là chuyên gia phân tích thể hình và huấn luyện viên cá nhân với 15 năm kinh nghiệm, chuyên phân loại hình thể và tư vấn lộ trình tập luyện phù hợp. " +
                "Nhiệm vụ của bạn là phân tích CHÍNH XÁC hình thể của người dùng, cung cấp đánh giá chi tiết về chỉ số sức khỏe, và đề xuất phương pháp tập luyện phù hợp. " +
                
                "📋 5 LOẠI HÌNH THỂ BẠN CẦN PHÂN LOẠI: " +
                "1. **THIN**: Thể trạng quá gầy - Cân nặng thấp, xương nổi rõ, cơ bắp ít " +
                "2. **SKINNY FAT**: Gầy nhưng có mỡ bụng - Cân nặng trong ngưỡng bình thường nhưng tỷ lệ mỡ cao, cơ bắp yếu " +
                "3. **NORMAL**: Bình thường - Cân đối, không quá gầy cũng không quá béo, ít cơ bắp " +
                "4. **OVERWEIGHT**: Thừa cân - Tỷ lệ mỡ cao, đặc biệt ở vùng bụng và thân " +
                "5. **FIT**: Săn chắc - Tỷ lệ cơ-mỡ cân đối, cơ bắp rõ nét, thể hiện tập luyện thường xuyên " +
                
                "🧐 YÊU CẦU VỀ PHÂN TÍCH: " +
                "- LUÔN xưng hô với người dùng là 'Bạn' trong MỌI câu phân tích (ví dụ: 'Bạn có chỉ số BMI...', 'Dựa vào hình ảnh, bạn có...') " +
                "- Phân tích CHI TIẾT về chỉ số BMI, cân nặng, chiều cao và tỷ lệ cơ thể (ít nhất 150-200 từ) " +
                "- Nhận xét cụ thể từng vùng cơ thể: vai, ngực, bụng, tay, chân " + 
                "- Đánh giá điểm mạnh và điểm cần cải thiện của cơ thể " +
                "- Sử dụng các thuật ngữ chuyên môn về thể hình nhưng giải thích dễ hiểu " +
                "- Dùng ngôn ngữ thân thiện, động viên, tích cực, tránh gây tự ti " +
                
                "💪 YÊU CẦU VỀ ĐỀ XUẤT (PHẢI DÀI VÀ CHI TIẾT 250-300 TỪ): " +
                "- Gợi ý các KHÓA HỌC CỤ THỂ phù hợp (cardio, sức mạnh, linh hoạt...) " +
                "- Đề xuất ít nhất 5 LOẠI BÀI TẬP cụ thể phù hợp với thể trạng và mục tiêu, nêu rõ số hiệp và số lần lặp lại " +
                "- Hướng dẫn CHẾ ĐỘ DINH DƯỠNG chi tiết: tỷ lệ protein, carbs, chất béo, danh sách thực phẩm cụ thể nên ăn và nên tránh " +
                "- Gợi ý tần suất tập luyện, thời lượng mỗi buổi và lịch nghỉ ngơi " +
                "- Đề xuất cách theo dõi tiến độ và điều chỉnh kế hoạch " +
                
                "Hãy trả lời BẰNG TIẾNG VIỆT với giọng điệu THÂN THIỆN, CHUYÊN NGHIỆP và ĐẦY ĐỘNG VIÊN.";

        // Tạo nội dung user message với prompt chi tiết hơn
        String prompt = "Xin chào! Hãy phân tích hình thể của người dùng trong ảnh này và đưa ra lời khuyên phù hợp. " +
                "Thông số: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "BMI = " + Math.round((weight / Math.pow(height/100, 2)) * 100) / 100.0 + ". " +
                
                "📊 PHÂN TÍCH YÊU CẦU: " +
                "1. Xác định chính xác loại hình thể (THIN, SKINNY FAT, NORMAL, OVERWEIGHT, hoặc FIT) " +
                "2. Phân tích chi tiết chỉ số BMI, cân nặng, tỷ lệ các vùng cơ thể " +
                "3. Đánh giá cụ thể từng vùng cơ thể: vai, ngực, bụng, tay, chân " +
                "4. Phân tích điểm mạnh và điểm cần cải thiện " +
                "5. Dự đoán tình trạng sức khỏe tổng quát dựa trên hình dáng cơ thể " +
                
                "🏋️ ĐỀ XUẤT CHI TIẾT (ĐỀ XUẤT PHẢI DÀI VÀ CỤ THỂ): " +
                "1. Khóa học phù hợp nhất với hình thể này " +
                "2. 5 bài tập cụ thể (chi tiết số hiệp, số lần) giúp cải thiện nhược điểm " +
                "3. Chế độ dinh dưỡng chi tiết (tỷ lệ protein/carbs/chất béo, danh sách thực phẩm nên ăn và tránh) " +
                "4. Tần suất, thời lượng tập luyện lý tưởng và lịch nghỉ ngơi hợp lý " +
                
                "📱 ĐỊNH DẠNG PHẢN HỒI: " +
                "- Vui lòng trả lời với định dạng JSON: {\"bodyType\": \"...\", \"analysis\": \"...\", \"recommendations\": \"...\"} " +
                "- Trong trường \"analysis\": phân tích chi tiết hình thể, ít nhất 150-200 từ " +
                "- Trong trường \"recommendations\": đề xuất chi tiết, ít nhất 250-300 từ " +
                "- LUÔN xưng hô với người dùng là 'Bạn' trong MỌI câu";

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
            // Log original response for debugging
            logger.info("Parsing Claude response. Response length: {}", claudeResponse != null ? claudeResponse.length() : 0);
            
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

            // If no valid JSON found, check if the response contains HTML content
            if (claudeResponse.contains("<div") || claudeResponse.contains("<h3") || claudeResponse.contains("<p")) {
                logger.info("Detected HTML content in Claude response");
                
                // Extract body type using regex
                Pattern bodyTypePattern = Pattern.compile("Loại cơ thể:\\s*([A-Za-z\\s]+)", Pattern.CASE_INSENSITIVE);
                Matcher bodyTypeMatcher = bodyTypePattern.matcher(claudeResponse);
                String bodyType = bodyTypeMatcher.find() ? bodyTypeMatcher.group(1).trim() : "Normal";
                
                // Split HTML content if it contains both analysis and recommendations
                String analysis = claudeResponse;
                String recommendations = "";
                
                if (claudeResponse.contains("ĐỀ XUẤT LUYỆN TẬP") || claudeResponse.contains("<h3")) {
                    // Try to split content at the heading that separates analysis from recommendations
                    String[] parts = claudeResponse.split("<h3[^>]*>ĐỀ XUẤT LUYỆN TẬP</h3>", 2);
                    if (parts.length > 1) {
                        analysis = parts[0];
                        recommendations = parts[1];
                    } else {
                        // Alternative splitting approach
                        parts = claudeResponse.split("<h3[^>]*>");
                        if (parts.length > 2) { // If we have at least two headings
                            analysis = "<h3>" + parts[1]; // First heading section
                            recommendations = "<h3>" + parts[2]; // Second heading section
                        }
                    }
                }
                
                // Sanitize HTML content
                analysis = sanitizeHtmlContent(analysis);
                recommendations = sanitizeHtmlContent(recommendations.isEmpty() ? analysis : recommendations);
                
                ImageAnalysisResponseDTO result = new ImageAnalysisResponseDTO();
                result.setBodyType(bodyType);
                result.setAnalysis(analysis);
                result.setRecommendations(recommendations);
                
                logger.info("Successfully parsed HTML content. Body type: {}, Analysis length: {}, Recommendations length: {}", 
                    bodyType, analysis.length(), recommendations.length());
                
                return result;
            }

            // If no structured format detected, try to extract information using regex
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
     * Helper method to sanitize HTML content
     * @param html The HTML content to sanitize
     * @return Sanitized HTML content
     */
    private String sanitizeHtmlContent(String html) {
        if (html == null) return "";
        
        // Remove any script tags for security
        String sanitized = html.replaceAll("<script\\b[^<]*(?:(?!</script>)<[^<]*)*</script>", "");
        
        // Ensure all HTML tags are properly closed
        sanitized = sanitized.replaceAll("<(br|hr)>", "<$1/>");
        
        // Fix any malformed div, span, p tags
        sanitized = sanitized.replaceAll("<(div|span|p)\\s+([^>]*)>", "<$1 $2>");
        
        return sanitized;
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
        String systemPrompt = "Bạn là chuyên gia phân tích thể hình và huấn luyện viên cá nhân với 15 năm kinh nghiệm, chuyên phân loại hình thể và tư vấn lộ trình tập luyện phù hợp. " +
                "Nhiệm vụ của bạn là phân tích CHÍNH XÁC hình thể của người dùng, cung cấp đánh giá chi tiết về chỉ số sức khỏe, và đề xuất phương pháp tập luyện phù hợp. " +
                
                "📋 5 LOẠI HÌNH THỂ BẠN CẦN PHÂN LOẠI: " +
                "1. **THIN**: Thể trạng quá gầy - Cân nặng thấp, xương nổi rõ, cơ bắp ít " +
                "2. **SKINNY FAT**: Gầy nhưng có mỡ bụng - Cân nặng trong ngưỡng bình thường nhưng tỷ lệ mỡ cao, cơ bắp yếu " +
                "3. **NORMAL**: Bình thường - Cân đối, không quá gầy cũng không quá béo, ít cơ bắp " +
                "4. **OVERWEIGHT**: Thừa cân - Tỷ lệ mỡ cao, đặc biệt ở vùng bụng và thân " +
                "5. **FIT**: Săn chắc - Tỷ lệ cơ-mỡ cân đối, cơ bắp rõ nét, thể hiện tập luyện thường xuyên " +
                
                "🧐 YÊU CẦU VỀ PHÂN TÍCH: " +
                "- LUÔN xưng hô với người dùng là 'Bạn' trong MỌI câu phân tích (ví dụ: 'Bạn có chỉ số BMI...', 'Dựa vào hình ảnh, bạn có...') " +
                "- Phân tích CHI TIẾT về chỉ số BMI, cân nặng, chiều cao và tỷ lệ cơ thể (ít nhất 150-200 từ) " +
                "- Nhận xét cụ thể từng vùng cơ thể: vai, ngực, bụng, tay, chân " + 
                "- Đánh giá điểm mạnh và điểm cần cải thiện của cơ thể " +
                "- Sử dụng các thuật ngữ chuyên môn về thể hình nhưng giải thích dễ hiểu " +
                "- Dùng ngôn ngữ thân thiện, động viên, tích cực, tránh gây tự ti " +
                
                "💪 YÊU CẦU VỀ ĐỀ XUẤT (PHẢI DÀI VÀ CHI TIẾT 250-300 TỪ): " +
                "- Gợi ý các KHÓA HỌC CỤ THỂ phù hợp (cardio, sức mạnh, linh hoạt...) " +
                "- Đề xuất ít nhất 5 LOẠI BÀI TẬP cụ thể phù hợp với thể trạng và mục tiêu, nêu rõ số hiệp và số lần lặp lại " +
                "- Hướng dẫn CHẾ ĐỘ DINH DƯỠNG chi tiết: tỷ lệ protein, carbs, chất béo, danh sách thực phẩm cụ thể nên ăn và nên tránh " +
                "- Gợi ý tần suất tập luyện, thời lượng mỗi buổi và lịch nghỉ ngơi " +
                "- Đề xuất cách theo dõi tiến độ và điều chỉnh kế hoạch " +
                
                "Hãy trả lời BẰNG TIẾNG VIỆT với giọng điệu THÂN THIỆN, CHUYÊN NGHIỆP và ĐẦY ĐỘNG VIÊN.";
        requestBody.put("system", systemPrompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        // Create user message content với tiêu chí chính xác
        String prompt = "Xin chào! Hãy phân tích hình thể của người dùng trong ảnh này và đưa ra lời khuyên phù hợp. " +
                "Thông số: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "BMI = " + Math.round((weight / Math.pow(height/100, 2)) * 100) / 100.0 + ". " +
                
                "📊 PHÂN TÍCH YÊU CẦU: " +
                "1. Xác định chính xác loại hình thể (THIN, SKINNY FAT, NORMAL, OVERWEIGHT, hoặc FIT) " +
                "2. Phân tích chi tiết chỉ số BMI, cân nặng, tỷ lệ các vùng cơ thể " +
                "3. Đánh giá cụ thể từng vùng cơ thể: vai, ngực, bụng, tay, chân " +
                "4. Phân tích điểm mạnh và điểm cần cải thiện " +
                "5. Dự đoán tình trạng sức khỏe tổng quát dựa trên hình dáng cơ thể " +
                
                "🏋️ ĐỀ XUẤT CHI TIẾT (ĐỀ XUẤT PHẢI DÀI VÀ CỤ THỂ): " +
                "1. Khóa học phù hợp nhất với hình thể này " +
                "2. 5 bài tập cụ thể (chi tiết số hiệp, số lần) giúp cải thiện nhược điểm " +
                "3. Chế độ dinh dưỡng chi tiết (tỷ lệ protein/carbs/chất béo, danh sách thực phẩm nên ăn và tránh) " +
                "4. Tần suất, thời lượng tập luyện lý tưởng và lịch nghỉ ngơi hợp lý " +
                
                "📱 ĐỊNH DẠNG PHẢN HỒI: " +
                "- Vui lòng trả lời với định dạng JSON: {\"bodyType\": \"...\", \"analysis\": \"...\", \"recommendations\": \"...\"} " +
                "- Trong trường \"analysis\": phân tích chi tiết hình thể, ít nhất 150-200 từ " +
                "- Trong trường \"recommendations\": đề xuất chi tiết, ít nhất 250-300 từ " +
                "- LUÔN xưng hô với người dùng là 'Bạn' trong MỌI câu";

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
            Integer recommendedCourseId = null;
            
            // Log thông tin request để debug
            logger.info("Received confirmation request data: {}", requestData);
            
            if (requestData.containsKey("formData")) {
                // Parse formData từ request
                Map<String, Object> formDataMap = (Map<String, Object>) requestData.get("formData");
                formData = parseFormDataFromMap(formDataMap);
                logger.info("Parsed formData: customerId={}, gender={}", 
                    formData.getCustomerId(), formData.getGender());
            }
            
            if (requestData.containsKey("recommendedCourseId")) {
                Object rawCourseId = requestData.get("recommendedCourseId");
                logger.info("Raw recommendedCourseId from request: {} (type: {})", 
                    rawCourseId, rawCourseId != null ? rawCourseId.getClass().getName() : "null");
                
                if (rawCourseId instanceof Integer) {
                    recommendedCourseId = (Integer) rawCourseId;
                } else if (rawCourseId instanceof Number) {
                    recommendedCourseId = ((Number) rawCourseId).intValue();
                } else if (rawCourseId instanceof String) {
                    try {
                        recommendedCourseId = Integer.parseInt((String) rawCourseId);
                    } catch (NumberFormatException e) {
                        logger.warn("Failed to parse recommendedCourseId from string: {}", rawCourseId);
                        return ResponseEntity.badRequest()
                            .body("Invalid recommendedCourseId format. Expected a number but got: " + rawCourseId);
                    }
                } else if (rawCourseId instanceof Map) {
                    // Xử lý trường hợp là map/object với courseId hoặc id
                    Map<String, Object> courseIdMap = (Map<String, Object>) rawCourseId;
                    if (courseIdMap.containsKey("courseId")) {
                        Object courseIdValue = courseIdMap.get("courseId");
                        if (courseIdValue instanceof Integer) {
                            recommendedCourseId = (Integer) courseIdValue;
                        } else if (courseIdValue instanceof Number) {
                            recommendedCourseId = ((Number) courseIdValue).intValue();
                        } else if (courseIdValue instanceof String) {
                            try {
                                recommendedCourseId = Integer.parseInt((String) courseIdValue);
                            } catch (NumberFormatException e) {
                                logger.warn("Failed to parse courseId from object: {}", courseIdValue);
                            }
                        }
                    } else if (courseIdMap.containsKey("id")) {
                        Object idValue = courseIdMap.get("id");
                        if (idValue instanceof Integer) {
                            recommendedCourseId = (Integer) idValue;
                        } else if (idValue instanceof Number) {
                            recommendedCourseId = ((Number) idValue).intValue();
                        } else if (idValue instanceof String) {
                            try {
                                recommendedCourseId = Integer.parseInt((String) idValue);
                            } catch (NumberFormatException e) {
                                logger.warn("Failed to parse id from object: {}", idValue);
                            }
                        }
                    }
                }
            }
            
            // Kiểm tra các thông tin bắt buộc
            if (formData == null) {
                logger.error("Missing formData in request");
                return ResponseEntity.badRequest()
                        .body("Missing required data: formData");
            }
            
            if (recommendedCourseId == null) {
                logger.error("Missing or invalid recommendedCourseId in request");
                return ResponseEntity.badRequest()
                        .body("Missing or invalid recommendedCourseId");
            }
            
            // Kiểm tra tồn tại customerId
            if (formData.getCustomerId() == null) {
                logger.error("Missing customerId in formData");
                return ResponseEntity.badRequest()
                        .body("Missing customerId in formData");
            }
            
            // Tạo biến final để sử dụng trong lambda
            final Integer finalRecommendedCourseId = recommendedCourseId;
            
            Customer customer = customerService.findById(formData.getCustomerId());
            if (customer == null) {
                logger.error("Customer not found with ID: {}", formData.getCustomerId());
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // *** KIỂM TRA DUPLICATE: Chỉ tạo bản ghi mới nếu chưa có customer + course_id này ***
            // Tạm thời kiểm tra bằng cách lấy tất cả records của customer và filter
            List<UserDataAi> allUserRecords = userDataAiService.findByCustomerId(formData.getCustomerId());
            List<UserDataAi> existingRecords = allUserRecords.stream()
                .filter(record -> record.getCourse() != null && 
                               record.getCourse().getCourseId().equals(finalRecommendedCourseId))
                .collect(Collectors.toList());
            
            Integer userDataId = null;
            
            if (!existingRecords.isEmpty()) {
                // Đã có bản ghi với customer và course này - cập nhật bản ghi hiện có
                UserDataAi existingRecord = existingRecords.get(0);
                userDataId = existingRecord.getUser_data_id();
                logger.info("Found existing record for customer {} and course {}: userDataId {}", 
                    formData.getCustomerId(), finalRecommendedCourseId, userDataId);
                
                // Cập nhật thông tin mới (vòng eo, vòng ngực, etc.)
                updateExistingUserDataAi(existingRecord, formData);
                // Đảm bảo effective = true
                existingRecord.setEffective(true);
                userDataAiService.saveUserDataAi(existingRecord);
                
            } else {
                // Chưa có bản ghi, tạo mới
                logger.info("Creating new record for customer {} and course {}", 
                    formData.getCustomerId(), finalRecommendedCourseId);
                
                formData.setRecommendedCourseId(finalRecommendedCourseId);
                UserDataAi userDataAi = createUserDataAiFromDTO(formData, customer);
                userDataAi.setEffective(true); // Đã được xác nhận
                userDataAi = userDataAiService.saveUserDataAi(userDataAi);
                userDataId = userDataAi.getUser_data_id();
            }
            
            // Lưu khóa học đề xuất vào CustomerCourse (nếu chưa có)
            List<CustomerCourse> existingCourses = customerCourseService.findByCustomerId(formData.getCustomerId());
            boolean courseExists = existingCourses.stream()
                    .anyMatch(cc -> cc.getCourse().getCourseId().equals(finalRecommendedCourseId));
            
            if (!courseExists) {
                logger.info("Saving confirmed course {} for user {}", finalRecommendedCourseId, formData.getCustomerId());
                customerCourseService.saveRecommendedCourseById(finalRecommendedCourseId, formData.getCustomerId());
            } else {
                logger.info("Course {} already exists for user {}, skipping save", finalRecommendedCourseId, formData.getCustomerId());
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
            
            // *** Lấy customerId từ formData để kiểm tra draftStorage
            Integer customerId = formData.getCustomerId();
            logger.info("Recommendation Step 7: Processing request for customerId: {}", customerId);
            
            // *** Nếu có customerId, lấy dữ liệu phân tích cơ thể từ draftStorage
            if (customerId != null) {
                FormWizardDraftDTO draft = draftStorage.get(customerId);
                if (draft != null && draft.getBody_type() != null) {
                    logger.info("Found draft data for customer {}. Using body_type: {}", customerId, draft.getBody_type());
                    
                    // Ưu tiên sử dụng body_type từ phân tích ảnh đã lưu
                    formData.setBody_type(draft.getBody_type());
                    
                    // Log để xác nhận
                    logger.info("Using body_type from draft storage: {}", draft.getBody_type());
                } else {
                    logger.warn("No draft data found for customer {} or body_type is null", customerId);
                }
            } else {
                logger.warn("No customerId provided in formData, cannot retrieve draft data");
            }
            
            // Get course details
            Course recommendedCourse = courseService.findCourseById(recommendedCourseId);
            if (recommendedCourse == null) {
                logger.error("Course not found with ID: {}", recommendedCourseId);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Course not found with ID: " + recommendedCourseId);
                errorResponse.put("message", "Không tìm thấy khóa học được đề xuất. Vui lòng thử lại sau.");
                errorResponse.put("currentStep", 7);
                errorResponse.put("totalSteps", 7);
                errorResponse.put("stepTitle", "Xác Nhận Khóa Học Được Đề Xuất");
                
                // Add empty recommendation to avoid frontend errors
                errorResponse.put("recommendedCourse", Collections.emptyMap());
                errorResponse.put("userMetrics", formDataMap);
                
                return ResponseEntity.ok(errorResponse);
            }
            
            // Log detailed course information
            logger.info("Step 7 data for course ID {}: {}", recommendedCourseId, recommendedCourse.getCourseName());
            
            // Log thông tin body_type để debug
            logger.info("Body type being used for recommendation: {}", formData.getBody_type());
            
            // Get course type details if available
            Map<String, Object> courseTypeDetails = new HashMap<>();
            if (recommendedCourse.getCourseType() != null) {
                courseTypeDetails.put("id", recommendedCourse.getCourseType().getCourseTypeId());
                courseTypeDetails.put("name", recommendedCourse.getCourseType().getCourseTypeName());
            } else {
                logger.warn("Course {} has no course type information", recommendedCourseId);
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
            
            // Kiểm tra xem có phân tích ảnh từ draft storage không
            if (customerId != null) {
                FormWizardDraftDTO draft = draftStorage.get(customerId);
                if (draft != null) {
                    // Thêm phân tích ảnh và khuyến nghị nếu có
                    if (draft.getImageAnalysis() != null) {
                        userMetrics.put("imageAnalysis", draft.getImageAnalysis());
                    }
                    if (draft.getImageRecommendations() != null) {
                        userMetrics.put("imageRecommendations", draft.getImageRecommendations());
                    }
                    if (draft.getImageBase64() != null) {
                        userMetrics.put("imageBase64", draft.getImageBase64());
                    }
                }
            }
            
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
            // ALSO add for frontend compatibility
            recommendationDetails.put("recommendedCourses", courseDetails);
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
            
            logger.info("Successfully generated step 7 data with course ID: {}", recommendedCourseId);
            
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
            if (userDataAi.getCreatedDate() != null) {
                LocalDateTime oneMonthAgo = LocalDateTime.now().minus(1, ChronoUnit.MONTHS);
                
                if (userDataAi.getCreatedDate().isAfter(oneMonthAgo)) {
                    long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), userDataAi.getCreatedDate().plus(1, ChronoUnit.MONTHS));
                    
                    return ResponseEntity.badRequest()
                            .body("Chỉ có thể cập nhật kết quả sau ít nhất 1 tháng kể từ khi bắt đầu khóa học. " +
                                  "Bạn còn " + daysRemaining + " ngày nữa mới có thể cập nhật.");
                }
                
                logger.info("Time validation passed: Record created {} (more than 1 month ago)", userDataAi.getCreatedDate());
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
            boolean previousEffective = userDataAi.getEffective() != null ? userDataAi.getEffective() : true;
            userDataAi.setEffective(isEffective);
            
            // Lưu vào database
            userDataAiService.saveUserDataAi(userDataAi);
            
            // Nếu khóa học không hiệu quả (effective=false), xử lý logic tạo dữ liệu huấn luyện mới
            boolean newTrainingDataCreated = false;
            String trainingDataMessage = "";
            
            if (!isEffective && (previousEffective || previousEffective != isEffective)) {
                logger.info("Course was not effective. We should create new training data.");
                
                // Trong điều kiện thực tế, bạn sẽ cần thêm phương thức vào service để tạo bản ghi TrackDataAi mới
                // Đây là mô tả những gì cần làm:
                // 1. Lấy ID lớn nhất từ bảng track_data_ai + 1
                // 2. Tạo bản ghi TrackDataAi mới với dữ liệu từ userDataAi (sử dụng các giá trị _after)
                // 3. Chọn một khóa học khác (không phải khóa học hiện tại) làm đề xuất mới
                // 4. Lưu bản ghi mới vào database
                
                // Do phương thức yêu cầu chưa được triển khai, chỉ log thông báo
                trainingDataMessage = "Hệ thống đã ghi nhận kết quả không hiệu quả và sẽ điều chỉnh cho lần đề xuất tiếp theo.";
                logger.warn("Creating new TrackDataAi record not implemented yet - requires service method updates");
            }
            
            // Kết quả đánh giá
            evaluationReason.append(isEffective ? "Kết luận: Khóa học hiệu quả!" : "Kết luận: Khóa học cần cải thiện.");
            
            logger.info("Updated post-course results for userDataId {}: effective = {}", userDataId, isEffective);
            
            // Trả về kết quả với thông tin so sánh
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Cập nhật kết quả sau khóa học thành công");
            response.put("effective", isEffective);
            response.put("evaluationReason", evaluationReason.toString());
            
            // Thêm thông tin về yêu cầu tạo dữ liệu huấn luyện mới (nếu có)
            if (!trainingDataMessage.isEmpty()) {
                response.put("trainingDataMessage", trainingDataMessage);
            }
            
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
            response.put("createdDate", userDataAi.getCreatedDate());
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
        prompt.append("Bạn là huấn luyện viên thể hình chuyên nghiệp với kinh nghiệm 15 năm. Hãy tư vấn cho khách hàng về khóa học tập luyện phù hợp dựa trên hình thể và mục tiêu của họ.\n\n");
        
        // Thông tin người dùng
        prompt.append("📊 THÔNG TIN CỦA BẠN:\n");
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
        prompt.append("\n🏋️ KHÓA HỌC ĐƯỢC ĐỀ XUẤT CHO BẠN:\n");
        prompt.append("- Tên khóa học: ").append(course.getCourseName()).append("\n");
        prompt.append("- Mô tả: ").append(course.getDescription() != null ? course.getDescription() : "Khóa học toàn diện phù hợp với nhu cầu của bạn").append("\n");
        prompt.append("- Thời lượng: ").append(course.getDuration() != null ? course.getDuration() : "Được thiết kế phù hợp với lịch trình của bạn").append("\n");
        
        if (course.getCourseType() != null) {
            prompt.append("- Loại khóa học: ").append(course.getCourseType().getCourseTypeName()).append("\n");
        }
        
        // Yêu cầu giải thích chi tiết
        prompt.append("\n💡 YÊU CẦU GIẢI THÍCH:\n");
        prompt.append("Hãy viết một bài tư vấn chi tiết (600-800 từ) bằng tiếng Việt với nội dung:\n");
        prompt.append("1. Phân tích chi tiết về hình thể hiện tại của bạn dựa trên các chỉ số đã cung cấp (ít nhất 150-200 từ)\n");
        prompt.append("2. Lý do tại sao khóa học này HOÀN TOÀN PHÙ HỢP với thể trạng và mục tiêu của bạn\n");
        prompt.append("3. Chi tiết các bài tập cụ thể trong khóa học (ít nhất 5 bài tập kèm số hiệp, số lần và cách thực hiện) sẽ giúp bạn cải thiện từng điểm yếu\n");
        prompt.append("4. Chế độ dinh dưỡng đề xuất chi tiết kèm theo khóa học:\n");
        prompt.append("   - Tỷ lệ protein/carbs/chất béo cụ thể\n");
        prompt.append("   - Danh sách thực phẩm nên ăn cho từng bữa (sáng, trưa, tối, trước/sau tập)\n");
        prompt.append("   - Các thực phẩm nên tránh và lý do\n");
        prompt.append("   - Chế độ ăn mẫu cho 1 ngày\n");
        prompt.append("5. Lộ trình tiến bộ dự kiến chi tiết:\n");
        prompt.append("   - Kết quả có thể thấy sau 2 tuần, 1 tháng, 3 tháng\n");
        prompt.append("   - Cách điều chỉnh chương trình khi đạt được các mốc tiến bộ\n");
        prompt.append("   - Phương pháp theo dõi tiến độ hiệu quả\n\n");
        prompt.append("QUAN TRỌNG:\n");
        prompt.append("- LUÔN xưng hô với người dùng là 'Bạn' trong MỌI câu\n");
        prompt.append("- Viết với giọng điệu thân thiện, chuyên nghiệp, động viên và cá nhân hóa\n");
        prompt.append("- Đề cập đến các thông số cụ thể của người dùng (như chiều cao, cân nặng, BMI) trong toàn bộ bài tư vấn\n");
        prompt.append("- Sử dụng các thuật ngữ thể hình chuyên môn nhưng giải thích dễ hiểu");
        
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

    @PostMapping("/generate-xml-from-database")
    public ResponseEntity<?> generateXmlFromDatabase() {
        try {
            List<TrackDataAi> allData = trackDataAiService.getAllTrackDataAi();
            
            if (allData.isEmpty()) {
                return ResponseEntity.badRequest().body("No data available in database to generate XML");
            }
            
            // Tạo XML từ dữ liệu database
            StringBuilder xmlBuilder = new StringBuilder();
            xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            xmlBuilder.append("<dataset>\n");
            
            for (TrackDataAi data : allData) {
                xmlBuilder.append("  <record>\n");
                xmlBuilder.append("    <track_data_ai_id>").append(data.getTrackDataAiId()).append("</track_data_ai_id>\n");
                xmlBuilder.append("    <age>").append(data.getAge()).append("</age>\n");
                xmlBuilder.append("    <gender>").append(data.getGender()).append("</gender>\n");
                xmlBuilder.append("    <height>").append(data.getHeight()).append("</height>\n");
                xmlBuilder.append("    <weight>").append(data.getWeight()).append("</weight>\n");
                xmlBuilder.append("    <waist_circumference>").append(data.getWaist_circumference()).append("</waist_circumference>\n");
                xmlBuilder.append("    <chest_circumference>").append(data.getChest_circumference()).append("</chest_circumference>\n");
                xmlBuilder.append("    <bmi>").append(data.getBmi()).append("</bmi>\n");
                xmlBuilder.append("    <training_goals>").append(data.getTraining_goals()).append("</training_goals>\n");
                xmlBuilder.append("    <training_history>").append(data.getTraining_history()).append("</training_history>\n");
                xmlBuilder.append("    <occupation>").append(data.getOccupation()).append("</occupation>\n");
                xmlBuilder.append("    <work_environment>").append(data.getWork_environment()).append("</work_environment>\n");
                xmlBuilder.append("    <effective>").append(data.getEffective()).append("</effective>\n");
                if (data.getCourse() != null) {
                    xmlBuilder.append("    <course_id>").append(data.getCourse().getCourseId()).append("</course_id>\n");
                }
                xmlBuilder.append("  </record>\n");
            }
            
            xmlBuilder.append("</dataset>");
            
            // Lưu file XML
            String xmlContent = xmlBuilder.toString();
            String filePath = XML_FILE_PATH;
            
            try (FileWriter writer = new FileWriter(filePath)) {
                writer.write(xmlContent);
            }
            
            return ResponseEntity.ok("XML file generated successfully with " + allData.size() + " records at: " + filePath);
            
        } catch (Exception e) {
            logger.error("Error generating XML from database: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error generating XML: " + e.getMessage());
        }
    }
    
    /**
     * Test endpoint để kiểm tra thuật toán C4.5 mới
     */
    @PostMapping("/test-c45-algorithm")
    public ResponseEntity<?> testC45Algorithm(@RequestBody UserDataDTO testUserData) {
        try {
            logger.info("=== TESTING C4.5 ALGORITHM ===");
            
            // Step 1: Kiểm tra dữ liệu training
            List<TrackDataAi> allTrainingData = trackDataAiService.getAllTrackDataAi();
            logger.info("Total training records: {}", allTrainingData.size());
            
            // Phân tích dữ liệu training
            Map<String, Object> trainingAnalysis = analyzeTrainingData(allTrainingData);
            
            // Step 2: Lọc dữ liệu effective
            List<TrackDataAi> effectiveData = allTrainingData.stream()
                .filter(data -> data.getCourse() != null && data.getEffective() != null && data.getEffective())
                .collect(Collectors.toList());
            logger.info("Effective training records: {}", effectiveData.size());
            
            // Step 3: Phân tích phân bố course_id
            Map<Integer, Long> courseDistribution = effectiveData.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getCourse().getCourseId(), 
                    Collectors.counting()
                ));
            logger.info("Course distribution: {}", courseDistribution);
            
            // Step 4: Test C4.5 algorithm
            List<Course> allCourses = courseService.findAll();
            C45DecisionTree c45DecisionTree = new C45DecisionTree(effectiveData);
            c45DecisionTree.setCourses(allCourses);
            
            TreeNode decisionTreeRoot = c45DecisionTree.buildDecisionTree();
            
            Map<String, Object> response = new HashMap<>();
            response.put("trainingAnalysis", trainingAnalysis);
            response.put("effectiveRecords", effectiveData.size());
            response.put("courseDistribution", courseDistribution);
            response.put("decisionTreeBuilt", decisionTreeRoot != null);
            
            if (decisionTreeRoot != null) {
                // Step 5: Test với dữ liệu người dùng
                Map<String, Object> userData = extractUserDataFromTrackDataAi(testUserData);
                logger.info("Testing with user data: {}", userData);
                
                Course recommendedCourse = c45DecisionTree.traverseDecisionTree(decisionTreeRoot, userData);
                
                if (recommendedCourse != null) {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("courseId", recommendedCourse.getCourseId());
                    courseInfo.put("courseName", recommendedCourse.getCourseName());
                    courseInfo.put("source", "c45_decision_tree");
                    response.put("recommendedCourse", courseInfo);
                    logger.info("✅ C4.5 recommended course: {} - {}", 
                        recommendedCourse.getCourseId(), recommendedCourse.getCourseName());
                } else {
                    response.put("recommendedCourse", null);
                    response.put("error", "C4.5 algorithm failed to recommend a course");
                    logger.warn("❌ C4.5 algorithm failed to recommend a course");
                }
                
                // Step 6: Phân tích cây quyết định
                response.put("treeAnalysis", analyzeDecisionTree(decisionTreeRoot));
            } else {
                response.put("error", "Failed to build C4.5 decision tree");
                logger.error("❌ Failed to build C4.5 decision tree");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error testing C4.5 algorithm: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error testing C4.5 algorithm: " + e.getMessage());
        }
    }
    
    private Map<String, Object> analyzeTrainingData(List<TrackDataAi> trainingData) {
        Map<String, Object> analysis = new HashMap<>();
        
        // Phân tích training_goals
        Map<String, Long> trainingGoalsDistribution = trainingData.stream()
            .filter(d -> d.getTraining_goals() != null)
            .collect(Collectors.groupingBy(
                TrackDataAi::getTraining_goals, 
                Collectors.counting()
            ));
        analysis.put("trainingGoalsDistribution", trainingGoalsDistribution);
        
        // Phân tích training_history
        Map<String, Long> trainingHistoryDistribution = trainingData.stream()
            .filter(d -> d.getTraining_history() != null)
            .collect(Collectors.groupingBy(
                TrackDataAi::getTraining_history, 
                Collectors.counting()
            ));
        analysis.put("trainingHistoryDistribution", trainingHistoryDistribution);
        
        // Phân tích effective status
        Map<Boolean, Long> effectiveDistribution = trainingData.stream()
            .filter(d -> d.getEffective() != null)
            .collect(Collectors.groupingBy(
                TrackDataAi::getEffective, 
                Collectors.counting()
            ));
        analysis.put("effectiveDistribution", effectiveDistribution);
        
        // Phân tích course_id có sẵn
        Set<Integer> availableCourseIds = trainingData.stream()
            .filter(d -> d.getCourse() != null)
            .map(d -> d.getCourse().getCourseId())
            .collect(Collectors.toSet());
        analysis.put("availableCourseIds", availableCourseIds);
        analysis.put("uniqueCourses", availableCourseIds.size());
        
        return analysis;
    }
    
    private Map<String, Object> analyzeDecisionTree(TreeNode root) {
        Map<String, Object> analysis = new HashMap<>();
        
        if (root == null) {
            analysis.put("error", "Root node is null");
            return analysis;
        }
        
        analysis.put("rootAttribute", root.getAttributeName());
        analysis.put("isLeaf", root.isLeaf());
        
        if (root.isLeaf()) {
            if (root.getCourseDataId() != null) {
                analysis.put("leafCourseId", root.getCourseDataId().getCourseId());
                analysis.put("leafCourseName", root.getCourseDataId().getCourseName());
            } else {
                analysis.put("leafCourse", null);
            }
        } else {
            Map<Object, TreeNode> children = root.getChildren();
            if (children != null) {
                analysis.put("childrenCount", children.size());
                analysis.put("childrenKeys", children.keySet());
                
                // Phân tích từng nhánh con
                Map<String, Object> childrenAnalysis = new HashMap<>();
                for (Map.Entry<Object, TreeNode> entry : children.entrySet()) {
                    Object key = entry.getKey();
                    TreeNode child = entry.getValue();
                    
                    Map<String, Object> childInfo = new HashMap<>();
                    childInfo.put("isLeaf", child.isLeaf());
                    if (child.isLeaf() && child.getCourseDataId() != null) {
                        childInfo.put("courseId", child.getCourseDataId().getCourseId());
                        childInfo.put("courseName", child.getCourseDataId().getCourseName());
                    } else if (!child.isLeaf()) {
                        childInfo.put("attribute", child.getAttributeName());
                        if (child.getChildren() != null) {
                            childInfo.put("grandChildrenCount", child.getChildren().size());
                        }
                    }
                    
                    childrenAnalysis.put(key.toString(), childInfo);
                }
                analysis.put("childrenAnalysis", childrenAnalysis);
            }
        }
        
        return analysis;
    }

    @GetMapping("/analyze-c45-performance")
    public ResponseEntity<?> analyzeC45Performance() {
        try {
            logger.info("=== ANALYZING C4.5 PERFORMANCE ===");
            
            List<TrackDataAi> allTrainingData = trackDataAiService.getAllTrackDataAi();
            List<Course> allCourses = courseService.findAll();
            
            Map<String, Object> performanceReport = new HashMap<>();
            
            // 1. Phân tích dữ liệu training
            performanceReport.put("totalTrainingRecords", allTrainingData.size());
            performanceReport.put("totalCourses", allCourses.size());
            
            // 2. Phân tích effective data
            List<TrackDataAi> effectiveData = allTrainingData.stream()
                .filter(data -> data.getCourse() != null && data.getEffective() != null && data.getEffective())
                .collect(Collectors.toList());
            performanceReport.put("effectiveRecords", effectiveData.size());
            
            // 3. Phân tích phân bố course_id trong effective data
            Map<Integer, Long> courseDistribution = effectiveData.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getCourse().getCourseId(), 
                    Collectors.counting()
                ));
            performanceReport.put("courseDistribution", courseDistribution);
            
            // 4. Kiểm tra overfitting
            boolean isOverfitting = courseDistribution.size() == 1 || 
                courseDistribution.values().stream().anyMatch(count -> count > effectiveData.size() * 0.8);
            performanceReport.put("isOverfitting", isOverfitting);
            
            if (isOverfitting) {
                performanceReport.put("overfittingReason", 
                    "Dữ liệu training bị thiên lệch về một hoặc vài khóa học cụ thể");
            }
            
            // 5. Test với nhiều user profile khác nhau
            List<Map<String, Object>> testProfiles = createTestUserProfiles();
            List<Map<String, Object>> testResults = new ArrayList<>();
            
            C45DecisionTree c45DecisionTree = new C45DecisionTree(effectiveData);
            c45DecisionTree.setCourses(allCourses);
            TreeNode decisionTreeRoot = c45DecisionTree.buildDecisionTree();
            
            if (decisionTreeRoot != null) {
                for (Map<String, Object> profile : testProfiles) {
                    Course recommendedCourse = c45DecisionTree.traverseDecisionTree(decisionTreeRoot, profile);
                    
                    Map<String, Object> testResult = new HashMap<>();
                    testResult.put("profile", profile);
                    if (recommendedCourse != null) {
                        testResult.put("recommendedCourseId", recommendedCourse.getCourseId());
                        testResult.put("recommendedCourseName", recommendedCourse.getCourseName());
                    } else {
                        testResult.put("recommendedCourse", null);
                    }
                    
                    testResults.add(testResult);
                }
            }
            
            performanceReport.put("testResults", testResults);
            
            // 6. Đề xuất cải thiện
            List<String> improvements = new ArrayList<>();
            if (effectiveData.size() < 100) {
                improvements.add("Cần thêm dữ liệu training (hiện tại chỉ có " + effectiveData.size() + " records)");
            }
            if (courseDistribution.size() < 5) {
                improvements.add("Dữ liệu thiếu đa dạng về khóa học (chỉ có " + courseDistribution.size() + " khóa học khác nhau)");
            }
            if (isOverfitting) {
                improvements.add("Cần cân bằng dữ liệu training để tránh overfitting");
            }
            
            performanceReport.put("improvements", improvements);
            
            return ResponseEntity.ok(performanceReport);
            
        } catch (Exception e) {
            logger.error("Error analyzing C4.5 performance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error analyzing C4.5 performance: " + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> createTestUserProfiles() {
        List<Map<String, Object>> profiles = new ArrayList<>();
        
        // Profile 1: Weight Loss - Overweight
        Map<String, Object> profile1 = new HashMap<>();
        profile1.put("training_goals", "7, Weight Loss");
        profile1.put("body_type", "Overweight");
        profile1.put("bmi", 28.0);
        profile1.put("training_history", "Beginner");
        profile1.put("training_frequency", "Moderate");
        profile1.put("gender", "Male");
        profile1.put("age", 35);
        profiles.add(profile1);
        
        // Profile 2: Strength Training - Normal
        Map<String, Object> profile2 = new HashMap<>();
        profile2.put("training_goals", "1, Strength Training");
        profile2.put("body_type", "Normal");
        profile2.put("bmi", 22.0);
        profile2.put("training_history", "Regularly");
        profile2.put("training_frequency", "Advanced");
        profile2.put("gender", "Female");
        profile2.put("age", 25);
        profiles.add(profile2);
        
        // Profile 3: Cardiovascular - Thin
        Map<String, Object> profile3 = new HashMap<>();
        profile3.put("training_goals", "2, Cardiovascular Training");
        profile3.put("body_type", "Thin");
        profile3.put("bmi", 18.0);
        profile3.put("training_history", "Never");
        profile3.put("training_frequency", "Beginner");
        profile3.put("gender", "Male");
        profile3.put("age", 20);
        profiles.add(profile3);
        
        // Profile 4: Flexibility - Fit
        Map<String, Object> profile4 = new HashMap<>();
        profile4.put("training_goals", "3, Flexibility and Balance");
        profile4.put("body_type", "Fit");
        profile4.put("bmi", 24.0);
        profile4.put("training_history", "Regularly");
        profile4.put("training_frequency", "Advanced");
        profile4.put("gender", "Female");
        profile4.put("age", 30);
        profiles.add(profile4);
        
        return profiles;
    }

    /**
     * Debug endpoint để phân tích nguyên nhân recommend course ID 1
     */
    @PostMapping("/debug-recommendation")
    public ResponseEntity<?> debugRecommendation(@RequestBody UserDataDTO testUserData) {
        try {
            logger.info("=== DEBUG RECOMMENDATION SYSTEM ===");
            
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Step 1: Kiểm tra dữ liệu training
            List<TrackDataAi> allTrainingData = trackDataAiService.getAllTrackDataAi();
            debugInfo.put("totalTrainingRecords", allTrainingData.size());
            
            // Step 2: Phân tích dữ liệu effective
            List<TrackDataAi> effectiveData = allTrainingData.stream()
                .filter(data -> data.getCourse() != null && data.getEffective() != null && data.getEffective())
                .collect(Collectors.toList());
            debugInfo.put("effectiveRecords", effectiveData.size());
            
            // Step 3: Phân tích phân bố course_id trong dữ liệu effective
            Map<Integer, Long> courseDistribution = effectiveData.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getCourse().getCourseId(), 
                    Collectors.counting()
                ));
            debugInfo.put("courseDistribution", courseDistribution);
            
            // Step 4: Phân tích training_goals
            Map<String, Long> trainingGoalsDistribution = effectiveData.stream()
                .filter(d -> d.getTraining_goals() != null)
                .collect(Collectors.groupingBy(
                    TrackDataAi::getTraining_goals, 
                    Collectors.counting()
                ));
            debugInfo.put("trainingGoalsDistribution", trainingGoalsDistribution);
            
            // Step 5: Kiểm tra training_goals của user test
            String userTrainingGoals = testUserData.getTraining_goals();
            debugInfo.put("userTrainingGoals", userTrainingGoals);
            
            // Step 6: Lọc dữ liệu theo training_goals của user
            List<TrackDataAi> matchingGoalsData = effectiveData.stream()
                .filter(d -> d.getTraining_goals() != null && 
                           d.getTraining_goals().equals(userTrainingGoals))
                .collect(Collectors.toList());
            debugInfo.put("matchingGoalsRecords", matchingGoalsData.size());
            
            if (!matchingGoalsData.isEmpty()) {
                Map<Integer, Long> matchingCourseDistribution = matchingGoalsData.stream()
                    .collect(Collectors.groupingBy(
                        d -> d.getCourse().getCourseId(), 
                        Collectors.counting()
                    ));
                debugInfo.put("matchingCourseDistribution", matchingCourseDistribution);
            }
            
            // Step 7: Test C4.5 algorithm
            try {
                List<Course> allCourses = courseService.findAll();
                C45DecisionTree c45DecisionTree = new C45DecisionTree(effectiveData);
                c45DecisionTree.setCourses(allCourses);
                
                TreeNode decisionTreeRoot = c45DecisionTree.buildDecisionTree();
                debugInfo.put("decisionTreeBuilt", decisionTreeRoot != null);
                
                if (decisionTreeRoot != null) {
                    // Phân tích cây quyết định
                    debugInfo.put("treeRootAttribute", decisionTreeRoot.getAttributeName());
                    debugInfo.put("treeIsLeaf", decisionTreeRoot.isLeaf());
                    
                    if (decisionTreeRoot.getChildren() != null) {
                        debugInfo.put("treeChildrenCount", decisionTreeRoot.getChildren().size());
                        debugInfo.put("treeChildrenKeys", decisionTreeRoot.getChildren().keySet());
                    }
                    
                    // Test traversal với user data
                    Map<String, Object> userData = extractUserDataFromTrackDataAi(testUserData);
                    logger.info("User data for traversal: {}", userData);
                    
                    Course recommendedCourse = c45DecisionTree.traverseDecisionTree(decisionTreeRoot, userData);
                    
                    if (recommendedCourse != null) {
                        debugInfo.put("c45RecommendedCourseId", recommendedCourse.getCourseId());
                        debugInfo.put("c45RecommendedCourseName", recommendedCourse.getCourseName());
                    } else {
                        debugInfo.put("c45RecommendedCourse", null);
                    }
                }
            } catch (Exception e) {
                debugInfo.put("c45Error", e.getMessage());
                logger.error("C4.5 algorithm error: {}", e.getMessage(), e);
            }
            
            // Step 8: Test fallback logic
            List<Course> allCourses = courseService.findAll();
            Course defaultCourse = getDefaultCourseByTrainingGoals(testUserData, allCourses);
            if (defaultCourse != null) {
                debugInfo.put("defaultCourseId", defaultCourse.getCourseId());
                debugInfo.put("defaultCourseName", defaultCourse.getCourseName());
            }
            
            // Step 9: Kiểm tra logic recommend hiện tại
            try {
                ResponseEntity<RecommendationDTO> currentRecommendation = recommendCourse(testUserData);
                if (currentRecommendation.getBody() != null) {
                    Course currentCourse = currentRecommendation.getBody().getRecommendedCourses();
                    if (currentCourse != null) {
                        debugInfo.put("currentRecommendedCourseId", currentCourse.getCourseId());
                        debugInfo.put("currentRecommendedCourseName", currentCourse.getCourseName());
                    }
                    debugInfo.put("currentMessage", currentRecommendation.getBody().getMessage());
                }
            } catch (Exception e) {
                debugInfo.put("currentRecommendationError", e.getMessage());
            }
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            logger.error("Error in debug recommendation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Debug error: " + e.getMessage());
        }
    }

    /**
     * Test endpoint đơn giản để kiểm tra dữ liệu và logic
     */
    @GetMapping("/simple-data-analysis")
    public ResponseEntity<?> simpleDataAnalysis() {
        try {
            Map<String, Object> analysis = new HashMap<>();
            
            // Lấy tất cả dữ liệu training
            List<TrackDataAi> allData = trackDataAiService.getAllTrackDataAi();
            analysis.put("totalRecords", allData.size());
            
            // Phân tích effective status
            long effectiveTrue = allData.stream()
                .filter(d -> d.getEffective() != null && d.getEffective())
                .count();
            long effectiveFalse = allData.stream()
                .filter(d -> d.getEffective() != null && !d.getEffective())
                .count();
            long effectiveNull = allData.stream()
                .filter(d -> d.getEffective() == null)
                .count();
            
            analysis.put("effectiveTrue", effectiveTrue);
            analysis.put("effectiveFalse", effectiveFalse);
            analysis.put("effectiveNull", effectiveNull);
            
            // Phân tích course_id trong dữ liệu effective=true
            List<TrackDataAi> effectiveData = allData.stream()
                .filter(d -> d.getCourse() != null && d.getEffective() != null && d.getEffective())
                .collect(Collectors.toList());
            
            Map<Integer, Long> effectiveCourseDistribution = effectiveData.stream()
                .collect(Collectors.groupingBy(
                    d -> d.getCourse().getCourseId(), 
                    Collectors.counting()
                ));
            analysis.put("effectiveCourseDistribution", effectiveCourseDistribution);
            
            // Phân tích training_goals trong dữ liệu effective=true
            Map<String, Long> effectiveGoalsDistribution = effectiveData.stream()
                .filter(d -> d.getTraining_goals() != null)
                .collect(Collectors.groupingBy(
                    TrackDataAi::getTraining_goals, 
                    Collectors.counting()
                ));
            analysis.put("effectiveGoalsDistribution", effectiveGoalsDistribution);
            
            // Test với training_goals = "7, Weight Loss"
            List<TrackDataAi> weightLossData = effectiveData.stream()
                .filter(d -> d.getTraining_goals() != null && 
                           d.getTraining_goals().contains("Weight Loss"))
                .collect(Collectors.toList());
            
            if (!weightLossData.isEmpty()) {
                Map<Integer, Long> weightLossCourses = weightLossData.stream()
                    .collect(Collectors.groupingBy(
                        d -> d.getCourse().getCourseId(), 
                        Collectors.counting()
                    ));
                analysis.put("weightLossEffectiveCourses", weightLossCourses);
                analysis.put("weightLossRecordsCount", weightLossData.size());
            } else {
                analysis.put("weightLossEffectiveCourses", "No effective records found for Weight Loss");
            }
            
            // Kiểm tra course ID 1 có tồn tại không
            List<Course> allCourses = courseService.findAll();
            Course course1 = allCourses.stream()
                .filter(c -> c.getCourseId().equals(1))
                .findFirst()
                .orElse(null);
            
            if (course1 != null) {
                analysis.put("course1Exists", true);
                analysis.put("course1Name", course1.getCourseName());
            } else {
                analysis.put("course1Exists", false);
            }
            
            // Kiểm tra tổng số courses
            analysis.put("totalCourses", allCourses.size());
            
            return ResponseEntity.ok(analysis);
            
        } catch (Exception e) {
            logger.error("Error in simple data analysis: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để kiểm tra hệ thống C4.5 cải thiện
     */
    @PostMapping("/test-enhanced-c45")
    public ResponseEntity<?> testEnhancedC45(@RequestBody(required = false) UserDataDTO testUserData) {
        try {
            logger.info("=== TESTING ENHANCED C4.5 SYSTEM ===");
            
            Map<String, Object> testResults = new HashMap<>();
            
            // Nếu không có test data, sử dụng data mẫu
            if (testUserData == null) {
                testUserData = new UserDataDTO();
                testUserData.setAge(35);
                testUserData.setGender("Male");
                testUserData.setHeight(180.0);
                testUserData.setWeight(85.0);
                testUserData.setBmi(26.2);
                testUserData.setChest_circumference(95.0);
                testUserData.setWaist_circumference(85.0);
                testUserData.setTraining_goals("7, Weight Loss");
                testUserData.setTraining_history("Regularly");
                testUserData.setBody_type("Overweight");
                testUserData.setOccupation("Office Worker");
                testUserData.setWork_environment("Sedentary");
                testUserData.setTraining_frequency("Advanced");
                testUserData.setCustomerId(4);
            }
            
            testResults.put("inputData", testUserData);
            
            // Test 1: Phân tích dữ liệu training
            List<TrackDataAi> allTrainingData = trackDataAiService.getAllTrackDataAi();
            Map<String, Object> dataAnalysis = analyzeTrainingDataDistribution(allTrainingData);
            testResults.put("trainingDataAnalysis", dataAnalysis);
            
            // Test 2: Xây dựng và test C4.5 decision tree
            c45DecisionTree.setTrainingData(allTrainingData);
            List<Course> allCourses = courseService.findAll();
            c45DecisionTree.setCourses(allCourses);
            
            TreeNode decisionTreeRoot = c45DecisionTree.buildDecisionTree();
            testResults.put("decisionTreeBuilt", decisionTreeRoot != null);
            
            if (decisionTreeRoot != null) {
                // Test traversal
                Map<String, Object> userData = extractUserDataFromTrackDataAi(testUserData);
                Course recommendedCourse = c45DecisionTree.traverseDecisionTree(decisionTreeRoot, userData);
                
                Map<String, Object> treeResults = new HashMap<>();
                treeResults.put("traversalSuccessful", recommendedCourse != null);
                if (recommendedCourse != null) {
                    treeResults.put("recommendedCourseId", recommendedCourse.getCourseId());
                    treeResults.put("recommendedCourseName", recommendedCourse.getCourseName());
                    treeResults.put("source", "c45_decision_tree");
                }
                testResults.put("c45Results", treeResults);
            }
            
            // Test 3: Test intelligent fallback
            Course fallbackCourse = getDefaultCourseByTrainingGoals(testUserData, allCourses);
            Map<String, Object> fallbackResults = new HashMap<>();
            fallbackResults.put("fallbackSuccessful", fallbackCourse != null);
            if (fallbackCourse != null) {
                fallbackResults.put("fallbackCourseId", fallbackCourse.getCourseId());
                fallbackResults.put("fallbackCourseName", fallbackCourse.getCourseName());
                fallbackResults.put("source", "intelligent_fallback");
            }
            testResults.put("fallbackResults", fallbackResults);
            
            // Test 4: Test với nhiều profile khác nhau
            List<Map<String, Object>> multipleProfileTests = testMultipleUserProfiles();
            testResults.put("multipleProfileTests", multipleProfileTests);
            
            logger.info("✅ Enhanced C4.5 system test completed successfully");
            return ResponseEntity.ok(testResults);
            
        } catch (Exception e) {
            logger.error("Error testing enhanced C4.5 system: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Test failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
        }
    }
    
    /**
     * Phân tích phân phối dữ liệu training
     */
    private Map<String, Object> analyzeTrainingDataDistribution(List<TrackDataAi> trainingData) {
        Map<String, Object> analysis = new HashMap<>();
        
        analysis.put("totalRecords", trainingData.size());
        
        // Phân tích effective distribution
        long effectiveTrue = trainingData.stream()
            .filter(d -> d.getEffective() != null && d.getEffective())
            .count();
        analysis.put("effectiveRecords", effectiveTrue);
        analysis.put("ineffectiveRecords", trainingData.size() - effectiveTrue);
        
        // Phân tích course distribution
        Map<Integer, Long> courseDistribution = trainingData.stream()
            .filter(d -> d.getCourse() != null)
            .collect(Collectors.groupingBy(
                d -> d.getCourse().getCourseId(),
                Collectors.counting()
            ));
        
        analysis.put("uniqueCourses", courseDistribution.size());
        
        // Lấy top 10 courses theo số lượng
        List<Map.Entry<Integer, Long>> topCourses = courseDistribution.entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
            
        // Chuyển thành Map kết quả
        Map<Integer, Long> topCoursesMap = new LinkedHashMap<>();
        for (Map.Entry<Integer, Long> entry : topCourses) {
            topCoursesMap.put(entry.getKey(), entry.getValue());
        }
            
        analysis.put("topCourses", topCoursesMap);
        
        // Phân tích training goals distribution
        Map<String, Long> trainingGoalsDistribution = trainingData.stream()
            .filter(d -> d.getTraining_goals() != null)
            .collect(Collectors.groupingBy(
                TrackDataAi::getTraining_goals,
                Collectors.counting()
            ));
        analysis.put("trainingGoalsDistribution", trainingGoalsDistribution);
        
        return analysis;
    }
    
    /**
     * Test với nhiều profile người dùng khác nhau
     */
    private List<Map<String, Object>> testMultipleUserProfiles() {
        List<Map<String, Object>> results = new ArrayList<>();
        
        // Profile 1: Người trẻ muốn tăng cơ
        UserDataDTO profile1 = new UserDataDTO();
        profile1.setAge(22);
        profile1.setGender("Male");
        profile1.setTraining_goals("1, Strength Training");
        profile1.setTraining_history("Never");
        profile1.setBmi(20.5);
        results.add(testSingleProfile("Young Beginner - Strength", profile1));
        
        // Profile 2: Người trung niên muốn giảm cân
        UserDataDTO profile2 = new UserDataDTO();
        profile2.setAge(40);
        profile2.setGender("Female");
        profile2.setTraining_goals("7, Weight Loss");
        profile2.setTraining_history("Occasionally");
        profile2.setBmi(28.5);
        results.add(testSingleProfile("Middle-aged - Weight Loss", profile2));
        
        // Profile 3: Người có kinh nghiệm muốn cardio
        UserDataDTO profile3 = new UserDataDTO();
        profile3.setAge(30);
        profile3.setGender("Male");
        profile3.setTraining_goals("2, Cardiovascular Training");
        profile3.setTraining_history("Regularly");
        profile3.setBmi(23.0);
        results.add(testSingleProfile("Experienced - Cardio", profile3));
        
        return results;
    }
    
    /**
     * Test một profile cụ thể
     */
    private Map<String, Object> testSingleProfile(String profileName, UserDataDTO profile) {
        Map<String, Object> result = new HashMap<>();
        result.put("profileName", profileName);
        result.put("profileData", profile);
        
        try {
            List<Course> allCourses = courseService.findAll();
            
            // Test C4.5 decision tree
            Map<String, Object> userData = extractUserDataFromTrackDataAi(profile);
            TreeNode decisionTreeRoot = c45DecisionTree.buildDecisionTree();
            
            if (decisionTreeRoot != null) {
                Course c45Course = c45DecisionTree.traverseDecisionTree(decisionTreeRoot, userData);
                if (c45Course != null) {
                    Map<String, Object> c45Recommendation = new HashMap<>();
                    c45Recommendation.put("courseId", c45Course.getCourseId());
                    c45Recommendation.put("courseName", c45Course.getCourseName());
                    result.put("c45Recommendation", c45Recommendation);
                }
            }
            
            // Test intelligent fallback
            Course fallbackCourse = getDefaultCourseByTrainingGoals(profile, allCourses);
            if (fallbackCourse != null) {
                Map<String, Object> fallbackRecommendation = new HashMap<>();
                fallbackRecommendation.put("courseId", fallbackCourse.getCourseId());
                fallbackRecommendation.put("courseName", fallbackCourse.getCourseName());
                result.put("fallbackRecommendation", fallbackRecommendation);
            }
            
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}