package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.EffectivenessUpdateDTO;
import com.capstone.wellnessnavigatorgym.dto.ImageAnalysisResponseDTO;
import com.capstone.wellnessnavigatorgym.dto.tree.RecommendationDTO;
import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
import com.capstone.wellnessnavigatorgym.dto.tree.UserDataDTO;
import com.capstone.wellnessnavigatorgym.dto.tree.UserFormDataDTO;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.service.*;
import com.capstone.wellnessnavigatorgym.utils.BuildDecisionTree;
import com.capstone.wellnessnavigatorgym.utils.XMLGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private BuildDecisionTree buildDecisionTree;

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
    private static final String XML_FILE_PATH = "D:/DA-CNPM-FINAL/CODE/kyo-gym-BE/kyo-gym-BE/wellness-navigator-gym/src/main/resources/static/DataTraining.xml";

    @GetMapping("/{id}")
    public ResponseEntity<TrackDataAi> getTrackDataAiById(@PathVariable Integer id) {
        return new ResponseEntity<>(trackDataAiService.findTrackDataAiById(id), HttpStatus.OK);
    }

    @PostMapping("/process-form-data")
    public ResponseEntity<RecommendationDTO> processFormData(@RequestBody UserFormDataDTO formData) {
        try {
            // Chuyển đổi dữ liệu từ form sang UserDataDTO để xử lý với hệ thống hiện tại
            UserDataDTO userDataDTO = convertToUserDataDTO(formData);

            // Tính BMI nếu chưa có
            if (userDataDTO.getBmi() == null && userDataDTO.getHeight() != null && userDataDTO.getWeight() != null) {
                double heightInMeters = userDataDTO.getHeight() / 100.0;
                double bmi = userDataDTO.getWeight() / (heightInMeters * heightInMeters);
                userDataDTO.setBmi(Math.round(bmi * 10.0) / 10.0); // Làm tròn đến 1 chữ số thập phân
            }

            // Lấy danh sách thuộc tính và kiểm tra chúng
            List<String> attributeNames = getAttributeNames();
            if (attributeNames == null || attributeNames.isEmpty()) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "Attribute names are missing or invalid"));
            }

            // Lấy dữ liệu và xây dựng cây quyết định
            List<TrackDataAi> trackDataAis = trackDataAiService.getAllTrackDataAi();
            if (trackDataAis.isEmpty()) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "No matching track data found with the given filters"));
            }

            // Tải cây quyết định
            List<Course> courses = courseService.findAll();
            buildDecisionTree = new BuildDecisionTree(trackDataAis);
            buildDecisionTree.setCourses(courses);
            
            // Build và xuất cây quyết định mới
            TreeNode decisionTree = buildDecisionTree.buildAndExportDecisionTree(XML_FILE_PATH);
            
            if (decisionTree == null) {
                logger.error("Failed to build decision tree");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationDTO(null, "Failed to build decision tree"));
            }

            // Tạo map dữ liệu người dùng
            Map<String, Object> userData = extractUserDataFromTrackDataAi(userDataDTO);

            // Duyệt qua cây quyết định và tìm đề xuất
            Course recommendations = buildDecisionTree.traverseDecisionTree(decisionTree, userData);

            // Lưu dữ liệu người dùng
            Customer customer = customerService.findById(userDataDTO.getCustomerId());
            UserDataAi userDataAi = createUserDataAiFromDTO(userDataDTO, customer);
            userDataAiService.saveUserDataAi(userDataAi);

            // Lưu khóa học đề xuất cho người dùng
            customerCourseService.saveRecommendedCourses(recommendations, userDataDTO.getCustomerId());

            if (recommendations == null) {
                logger.warn("No suitable course recommendation found for the user");
                return ResponseEntity.ok(new RecommendationDTO(null, "No suitable course recommendation found"));
            }

            return ResponseEntity.ok(new RecommendationDTO(recommendations, "Course recommendations generated successfully"));
        } catch (Exception e) {
            logger.error("Error processing form data: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationDTO(null, "Error processing form data: " + e.getMessage()));
        }
    }

    private UserDataAi createUserDataAiFromDTO(UserDataDTO dto, Customer customer) {
        UserDataAi userDataAi = new UserDataAi();
        userDataAi.setActivityLevel(dto.getActivity_level());
        userDataAi.setAge(dto.getAge());
        userDataAi.setGender(dto.getGender());
        userDataAi.setBmi(dto.getBmi());
        userDataAi.setTrainingGoals(dto.getTraining_goals());
        userDataAi.setTrainingHistory(dto.getTraining_history());

        // Thêm các trường mở rộng
        userDataAi.setHeight(dto.getHeight());
        userDataAi.setWeight(dto.getWeight());
        userDataAi.setBodyType(dto.getBodyType());
        userDataAi.setOccupation(dto.getOccupation());
        userDataAi.setWorkEnvironment(dto.getWorkEnvironment());
        userDataAi.setHealthIssues(dto.getHealthIssues());
        userDataAi.setChestSize(dto.getChestSize());
        userDataAi.setWaistSize(dto.getWaistSize());

        userDataAi.setEffective(true);
        userDataAi.setCustomer(customer);

        return userDataAi;
    }

    private UserDataDTO convertToUserDataDTO(UserFormDataDTO formData) {
        UserDataDTO dto = new UserDataDTO();

        // Mapping các trường dữ liệu
        dto.setActivity_level(formData.getTrainingFrequency());
        dto.setAge(formData.getAge());
        dto.setGender(formData.getGender());
        dto.setHeight(formData.getHeight());
        dto.setWeight(formData.getWeight());

        // Tính BMI
        double heightInMeters = formData.getHeight() / 100.0;
        double bmi = formData.getWeight() / (heightInMeters * heightInMeters);
        dto.setBmi(Math.round(bmi * 10.0) / 10.0); // Làm tròn đến 1 chữ số thập phân

        dto.setTraining_goals(formData.getTrainingGoal());
        dto.setTraining_history(formData.getDesiredFrequency());
        dto.setBodyType(formData.getBodyType());
        dto.setOccupation(formData.getOccupation());
        dto.setWorkEnvironment(formData.getWorkEnvironment());

        // Chuyển đổi danh sách healthIssues thành chuỗi
        if (formData.getHealthIssues() != null && !formData.getHealthIssues().isEmpty()) {
            dto.setHealthIssues(String.join(",", formData.getHealthIssues()));
        }

        dto.setChestSize(formData.getChestSize());
        dto.setWaistSize(formData.getWaistSize());
        dto.setCustomerId(formData.getCustomerId());

        return dto;
    }

    @PostMapping("/recommend-course")
    public ResponseEntity<RecommendationDTO> recommendCourse(@RequestBody UserDataDTO userDataDTO) {
        try {
            logger.info("Received recommendation request for user ID: " + userDataDTO.getCustomerId());
            
            // Lấy danh sách thuộc tính và kiểm tra chúng
            List<String> attributeNames = getAttributeNames();
            if (attributeNames == null || attributeNames.isEmpty()) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "Attribute names are missing or invalid"));
            }
            
            // Lấy dữ liệu
            List<TrackDataAi> trackDataAis = trackDataAiService.getAllTrackDataAi();
            if (trackDataAis.isEmpty()) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "No matching track data found with the given filters"));
            }

            // Kiểm tra nếu file XML tồn tại, thì đọc từ file, nếu không thì xây dựng và lưu lại
            File xmlFile = new File(XML_FILE_PATH);
            TreeNode decisionTree;
            
            if (xmlFile.exists()) {
                logger.info("Loading decision tree from existing XML file: " + XML_FILE_PATH);
                decisionTree = buildDecisionTree.importTreeFromXml(XML_FILE_PATH);
                
                // Kiểm tra nếu import thất bại, xây dựng lại cây
                if (decisionTree == null) {
                    logger.warn("Failed to import decision tree from XML. Building new one.");
                    List<Course> courses = courseService.findAll();
                    buildDecisionTree = new BuildDecisionTree(trackDataAis);
                    buildDecisionTree.setCourses(courses);
                    decisionTree = buildDecisionTree.buildAndExportDecisionTree(XML_FILE_PATH);
                }
            } else {
                logger.info("Building new decision tree and exporting to XML");
                // Tải cây quyết định
                List<Course> courses = courseService.findAll();
                buildDecisionTree = new BuildDecisionTree(trackDataAis);
                buildDecisionTree.setCourses(courses);
                decisionTree = buildDecisionTree.buildAndExportDecisionTree(XML_FILE_PATH);
            }

            if (decisionTree == null) {
                logger.warn("Failed to load or build decision tree");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new RecommendationDTO(null, "Failed to load or build decision tree"));
            }

            // Tạo map dữ liệu người dùng từ UserDataDTO
            Map<String, Object> userData = extractUserDataFromTrackDataAi(userDataDTO);
            
            // Duyệt qua cây quyết định và tìm đề xuất
            Course recommendations = buildDecisionTree.traverseDecisionTree(decisionTree, userData);

            if (recommendations == null) {
                logger.warn("No suitable course recommendation found for the user");
                return ResponseEntity.ok(new RecommendationDTO(null, "No suitable course recommendation found"));
            }

            Customer customer = customerService.findById(userDataDTO.getCustomerId());
            if (customer == null) {
                return ResponseEntity.badRequest().body(new RecommendationDTO(null, "Customer not found"));
            }

            // Lưu dữ liệu người dùng
            UserDataAi userDataAi = createUserDataAiFromDTO(userDataDTO, customer);
            userDataAiService.saveUserDataAi(userDataAi);

            // Lưu khóa học đề xuất
            logger.info("Saving recommended course " + recommendations.getCourseId() + " for user " + userDataDTO.getCustomerId());
            customerCourseService.saveRecommendedCourses(recommendations, userDataDTO.getCustomerId());

            return ResponseEntity.ok(new RecommendationDTO(recommendations, "Course recommendations generated successfully"));
        } catch (Exception e) {
            logger.error("Error in recommendation process: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new RecommendationDTO(null, "Error in recommendation process: " + e.getMessage()));
        }
    }

    private Map<String, Object> extractUserDataFromTrackDataAi(UserDataDTO userDataDTO) {
        Map<String, Object> userData = new HashMap<>();
        // Các trường cơ bản
        userData.put("activity_level", userDataDTO.getActivity_level());
        userData.put("age", userDataDTO.getAge());
        userData.put("gender", userDataDTO.getGender());
        userData.put("bmi", userDataDTO.getBmi());
        userData.put("training_goals", userDataDTO.getTraining_goals());
        userData.put("training_history", userDataDTO.getTraining_history());
        
        // Các trường mới - đảm bảo tên trường nhất quán với BuildDecisionTree
        if (userDataDTO.getHeight() != null) userData.put("height", userDataDTO.getHeight());
        if (userDataDTO.getWeight() != null) userData.put("weight", userDataDTO.getWeight());
        if (userDataDTO.getBodyType() != null) userData.put("body_type", userDataDTO.getBodyType());
        if (userDataDTO.getOccupation() != null) userData.put("occupation", userDataDTO.getOccupation());
        if (userDataDTO.getWorkEnvironment() != null) userData.put("work_environment", userDataDTO.getWorkEnvironment());
        if (userDataDTO.getHealthIssues() != null) userData.put("health_issues", userDataDTO.getHealthIssues());
        if (userDataDTO.getChestSize() != null) userData.put("bust", userDataDTO.getChestSize());
        if (userDataDTO.getWaistSize() != null) userData.put("waist", userDataDTO.getWaistSize());
        
        return userData;
    }

    private List<String> getAttributeNames() {
        // Lấy tất cả các thuộc tính, sau đó lọc chỉ giữ lại thuộc tính hợp lệ
        return Arrays.asList(
            "activity_level",
            "age",
            "gender", 
            "bmi",
            "training_goals",
            "training_history",
            "height",
            "weight", 
            "body_type",
            "occupation",
            "work_environment",
            "health_issues",
            "bust",
            "waist"
        ).stream().filter(TrackDataAi::isValidAttribute).collect(Collectors.toList());
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
                userDataAi.setActivityLevel(data[0].trim());
                userDataAi.setAge(Integer.parseInt(data[1].trim()));
                userDataAi.setGender(data[2].trim());
                userDataAi.setHeight(Double.parseDouble(data[3].trim()));
                userDataAi.setWeight(Double.parseDouble(data[4].trim()));

                // Tính BMI
                Double height = userDataAi.getHeight() / 100; // Chuyển từ cm sang m
                Double bmi = userDataAi.getWeight() / (height * height);
                userDataAi.setBmi(bmi);

                userDataAi.setTrainingGoals(data[5].trim());
                userDataAi.setTrainingHistory(data[6].trim());
                userDataAi.setBodyType(data[7].trim());

                if (data.length > 8) userDataAi.setOccupation(data[8].trim());
                if (data.length > 9) userDataAi.setWorkEnvironment(data[9].trim());
                if (data.length > 10) userDataAi.setHealthIssues(data[10].trim());
                if (data.length > 11) userDataAi.setChestSize(Double.parseDouble(data[11].trim()));
                if (data.length > 12) userDataAi.setWaistSize(Double.parseDouble(data[12].trim()));

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
                    userDataDTO.setBmi(Math.round(bmi * 10.0) / 10.0);
                    
                    userDataDTO.setBodyType(response.getBodyType());
                    
                    // Thiết lập các giá trị mặc định
                    userDataDTO.setActivity_level("Moderate");
                    userDataDTO.setAge(30);
                    userDataDTO.setTraining_goals("Weight Management");
                    userDataDTO.setTraining_history("Beginner");
                    
                    // Lưu dữ liệu và đề xuất khóa học
                    logger.info("Attempting to recommend courses based on image analysis for customer ID: {}", customerId);
                    RecommendationDTO recommendation = recommendCourse(userDataDTO).getBody();
                    
                    if (recommendation != null && recommendation.getRecommendedCourses() != null) {
                        logger.info("Successfully recommended course: {}", recommendation.getRecommendedCourses().getCourseName());
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
        String systemPrompt = "Bạn là một huấn luyện viên thể hình chuyên nghiệp, có khả năng phân tích hình thể khách quan dựa trên hình ảnh và các thông số. " + 
                "Khi đánh giá ảnh cơ thể, bạn CHỈ tập trung vào hình dạng cơ thể, phân bố cơ-mỡ, và cấu trúc cơ bắp. " + 
                "Đây là một ứng dụng fitness nên bạn cần đưa ra phân tích chuyên nghiệp, chi tiết và hữu ích. " +
                "Khi nhìn thấy một bức ảnh, tập trung vào các phần cơ thể hiển thị và bỏ qua các phần khác. " +
                "TRẢ LỜI HOÀN TOÀN BẰNG TIẾNG VIỆT. " +
                "QUAN TRỌNG: Luôn xưng hô với người dùng là 'bạn' thay vì 'người này' hoặc các đại từ khác để tạo sự thân thiện. " +
                "Hãy phân tích thật kỹ tỷ lệ mỡ/cơ trong hình ảnh. Đặc biệt chú ý tới phần bụng, hông, ngực và tay để phân loại chính xác. " +
                "Hãy thật cẩn thận khi phân loại hình thể, đặc biệt là phân biệt giữa 'Normal' và 'Skinny Fat': " +
                "- Normal: Cơ thể cân đối, ít mỡ bụng, có đường nét cơ bắp khá rõ ràng. " +
                "- Skinny Fat: Cân nặng có vẻ bình thường nhưng có mỡ bụng, cơ bắp ít và không rõ nét. ";

        // Tạo nội dung user message
        String prompt = "Hãy phân tích hình thể cơ thể trong ảnh này (bỏ qua khuôn mặt nếu có). " + 
                "Thông số cơ thể: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "Dựa vào hình ảnh và chỉ số BMI = " + Math.round((weight / Math.pow(height/100, 2)) * 10) / 10.0 + ", hãy phân tích các yếu tố sau:\n\n" +
                "1. Phân loại hình thể (chọn MỘT loại phù hợp nhất): Thin (gầy), Skinny Fat (gầy có mỡ), Overweight (thừa cân), Normal (bình thường), hoặc Fit (có cơ bắp).\n\n" +
                "2. Phân tích chi tiết (ít nhất 150 từ):\n" + 
                "   - Tỷ lệ cơ-mỡ và phân bố\n" + 
                "   - Đường nét cơ bắp và sự rõ ràng\n" + 
                "   - Tỷ lệ cơ thể tổng thể\n" + 
                "   - 2-3 điểm mạnh rõ ràng nhất\n" + 
                "   - 2-3 điểm yếu cần cải thiện\n\n" +
                "3. Đề xuất chi tiết (ít nhất 200 từ):\n" + 
                "   - Kế hoạch tập luyện tổng thể phù hợp nhất\n" + 
                "   - Tần suất tập luyện lý tưởng (số buổi/tuần, thời gian/buổi)\n" + 
                "   - Liệt kê ít nhất 5 bài tập cụ thể (tên bài, nhóm cơ, số hiệp/lần lặp lại)\n" + 
                "   - Hướng dẫn dinh dưỡng phù hợp (lượng protein, carbs, chất béo)\n" + 
                "   - Đề xuất về cardio và thời gian nghỉ ngơi/phục hồi\n\n" +
                "Hãy trả về KẾT QUẢ DẠNG JSON với 3 trường: bodyType, analysis, và recommendations.";

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
     * Sử dụng cách tiếp cận khác với prompt không đề cập đến khuôn mặt
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
        String systemPrompt = "Bạn là một huấn luyện viên thể hình chuyên nghiệp, có chuyên môn phân tích hình thể và đưa ra lời khuyên tập luyện. " +
                "Hãy phân tích hình thể một cách khách quan và đưa ra đánh giá chuyên môn, không có ý kiến cá nhân. " +
                "Trả về kết quả dạng JSON với 3 trường: bodyType, analysis, và recommendations.";
        requestBody.put("system", systemPrompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        // Create user message content
        String prompt = "Tôi là huấn luyện viên fitness. Hãy giúp tôi phân tích thân hình cơ thể trong ảnh này. " +
                "Thông số cơ thể: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "Bạn có thể phân tích và phân loại hình thể này không? " +
                "Hãy phân loại hình thể vào một trong các nhóm: Thin (gầy), Skinny Fat (gầy có mỡ), Overweight (thừa cân), Normal (bình thường), hoặc Fit (có cơ bắp). " +
                "Cũng hãy đánh giá tỷ lệ cơ/mỡ, điểm mạnh và điểm yếu, và đề xuất các bài tập phù hợp. " +
                "Cho tôi kết quả dạng JSON có bodyType, analysis, và recommendations. Đừng kèm giải thích, chỉ trả kết quả JSON.";

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
        try {
            // Lấy tất cả dữ liệu huấn luyện
            List<TrackDataAi> trackDataAis = trackDataAiService.getAllTrackDataAi();
            if (trackDataAis.isEmpty()) {
                return ResponseEntity.badRequest().body("No training data available");
            }
            
            logger.info("Rebuilding decision tree with {} training records", trackDataAis.size());
            
            // Lấy danh sách khóa học
            List<Course> courses = courseService.findAll();
            
            // Xây dựng cây quyết định mới
            buildDecisionTree = new BuildDecisionTree(trackDataAis);
            buildDecisionTree.setCourses(courses);
            
            // Xây dựng và xuất ra file XML
            TreeNode root = buildDecisionTree.buildAndExportDecisionTree(XML_FILE_PATH);
            
            if (root == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to rebuild decision tree");
            }
            
            return ResponseEntity.ok("Decision tree rebuilt successfully and exported to " + XML_FILE_PATH);
        } catch (Exception e) {
            logger.error("Error rebuilding decision tree: " + e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error rebuilding decision tree: " + e.getMessage());
        }
    }

    @GetMapping("/generate-enhanced-xml")
    public ResponseEntity<String> generateEnhancedXML() {
        try {
            List<TrackDataAi> trackDataList = trackDataAiService.getAllTrackDataAi();
            boolean success = XMLGenerator.generateEnhancedXML(trackDataList, XML_FILE_PATH);
            
            if (success) {
                return ResponseEntity.ok("Enhanced XML file generated successfully at: " + XML_FILE_PATH);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to generate enhanced XML file. Check server logs for details.");
            }
        } catch (Exception e) {
            logger.error("Error generating enhanced XML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error generating enhanced XML: " + e.getMessage());
        }
    }

    @GetMapping("/validate-xml")
    public ResponseEntity<String> validateXML() {
        try {
            boolean valid = XMLGenerator.validateAndFixXML(XML_FILE_PATH);
            
            if (valid) {
                return ResponseEntity.ok("XML file is valid and accessible at: " + XML_FILE_PATH);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("XML file is invalid or missing. Check server logs for details.");
            }
        } catch (Exception e) {
            logger.error("Error validating XML: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error validating XML: " + e.getMessage());
        }
    }
}