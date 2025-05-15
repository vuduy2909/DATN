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
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/track-data-ai")
@CrossOrigin(origins = "http://localhost:3000")
public class TrackDataAiController {

    private static final Logger logger = LoggerFactory.getLogger(TrackDataAiController.class);

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
            TreeNode decisionTree = buildDecisionTree.buildDecisionTree();

            // Import cây quyết định từ file (điều chỉnh đường dẫn tùy theo hệ thống của bạn)
            buildDecisionTree.importTreeFromXml("D:\\DuyTan_University\\Project_Capstone_1\\CAPSTONE1-KyoGYM\\C1SE.32-Capstone1-BE\\wellness-navigator-gym\\src\\main\\resources\\static\\DataTraining1.xml");

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

            return ResponseEntity.ok(new RecommendationDTO(recommendations, "Course recommendations generated successfully based on form data"));
        } catch (Exception e) {
            e.printStackTrace();
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
        TreeNode decisionTree = buildDecisionTree.buildDecisionTree();

        // Xuất cây ra file XML
        //buildDecisionTree.exportTreeToXml(decisionTree, "D:\\DuyTan_University\\Project_Capstone_1\\CAPSTONE1-KyoGYM\\C1SE.32-Capstone1-BE\\wellness-navigator-gym\\src\\main\\resources\\static\\DataTraining1.xml");
        //return null;
        buildDecisionTree.importTreeFromXml("D:\\DuyTan_University\\Project_Capstone_1\\CAPSTONE1-KyoGYM\\C1SE.32-Capstone1-BE\\wellness-navigator-gym\\src\\main\\resources\\static\\DataTraining1.xml");

        // Tạo map dữ liệu người dùng từ TrackDataAi
        Map<String, Object> userData = extractUserDataFromTrackDataAi(userDataDTO);

        // Duyệt qua cây quyết định và tìm đề xuất
        Course recommendations = buildDecisionTree.traverseDecisionTree(decisionTree, userData);

        Customer customer = customerService.findById(userDataDTO.getCustomerId());
        UserDataAi userDataAi = createUserDataAiFromDTO(userDataDTO, customer);
        userDataAiService.saveUserDataAi(userDataAi);

        customerCourseService.saveRecommendedCourses(recommendations, userDataDTO.getCustomerId());

        return ResponseEntity.ok(new RecommendationDTO(recommendations, "Course recommendations generated successfully"));
    }

    private Map<String, Object> extractUserDataFromTrackDataAi(UserDataDTO userDataDTO) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("activity_level", userDataDTO.getActivity_level());
        userData.put("age", userDataDTO.getAge());
        userData.put("gender", userDataDTO.getGender());
        userData.put("bmi", userDataDTO.getBmi());
        userData.put("training_goals", userDataDTO.getTraining_goals());
        userData.put("training_history", userDataDTO.getTraining_history());

        // Thêm các trường mới vào map
        userData.put("height", userDataDTO.getHeight());
        userData.put("weight", userDataDTO.getWeight());
        userData.put("bodyType", userDataDTO.getBodyType());
        userData.put("occupation", userDataDTO.getOccupation());
        userData.put("workEnvironment", userDataDTO.getWorkEnvironment());
        userData.put("healthIssues", userDataDTO.getHealthIssues());
        userData.put("chestSize", userDataDTO.getChestSize());
        userData.put("waistSize", userDataDTO.getWaistSize());

        return userData;
    }

    private List<String> getAttributeNames() {
        // Cập nhật danh sách thuộc tính bao gồm các trường mới
        return Arrays.asList(
                "activity_level", "age", "gender", "bmi", "training_goals", "training_history",
                "height", "weight", "bodyType", "occupation", "workEnvironment", "healthIssues",
                "chestSize", "waistSize"
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
                                                                 @RequestParam("gender") String gender) {
        try {
            logger.info("Starting image analysis - height: {}, weight: {}, gender: {}, image name: {}, size: {}",
                    height, weight, gender, image.getOriginalFilename(), image.getSize());

            // Kiểm tra nếu ảnh không có hoặc rỗng
            if (image == null || image.isEmpty() || image.getSize() == 0) {
                logger.error("No image or empty image provided");
                return ResponseEntity.badRequest()
                        .body(new ImageAnalysisResponseDTO("Unknown",
                                "Không có ảnh hoặc ảnh rỗng",
                                "Vui lòng tải lên ảnh rõ nét hoặc chọn loại cơ thể thủ công."));
            }

            // Thử gọi Claude API với tối đa 3 lần nếu có lỗi
            String claudeResponse = null;
            Exception lastException = null;
            int maxRetries = 3;

            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    logger.info("Attempt {} to call Claude API", attempt);
                    claudeResponse = callClaudeApi(image, height, weight, gender);

                    // Nếu có kết quả (không null) và không chứa thông báo lỗi, thì dừng lại
                    // Trường hợp nếu Claude từ chối phân tích, vẫn xử lý bình thường
                    if (claudeResponse != null) {
                        if (!claudeResponse.contains("Lỗi kết nối") &&
                                !claudeResponse.contains("Lỗi xác thực") &&
                                !claudeResponse.contains("Vượt quá giới hạn")) {
                            break;
                        }
                    }

                    // Nếu có lỗi, chờ trước khi thử lại
                    Thread.sleep(1000 * attempt); // 1s, 2s, 3s delay giữa các lần thử
                } catch (Exception e) {
                    lastException = e;
                    logger.error("Error calling Claude API - attempt {}: {}", attempt, e.getMessage());
                    if (attempt < maxRetries) {
                        Thread.sleep(1000 * attempt);
                    }
                }
            }

            // Kiểm tra kết quả cuối cùng
            if (claudeResponse == null) {
                // Nếu sau các lần thử vẫn không có kết quả
                throw new RuntimeException("Không thể kết nối với Claude API sau " + maxRetries + " lần thử", lastException);
            }

            // Kiểm tra các lỗi dịch vụ
            if (claudeResponse.contains("Lỗi kết nối") ||
                    claudeResponse.contains("Lỗi xác thực") ||
                    claudeResponse.contains("Vượt quá giới hạn") ||
                    claudeResponse.contains("Dịch vụ Claude API hiện không khả dụng")) {
                // Trả về lỗi từ Claude API
                logger.warn("Claude API service error: {}", claudeResponse);
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(new ImageAnalysisResponseDTO("Unknown",
                                "Lỗi không xác định (503): " + claudeResponse,
                                "Vui lòng thử lại sau hoặc chọn loại cơ thể thủ công."));
            }

            // Parse kết quả từ Claude
            ImageAnalysisResponseDTO response = parseClaudeResponse(claudeResponse);
            logger.info("Successfully analyzed image. Body type: {}", response.getBodyType());

            // Kiểm tra nếu Claude từ chối phân tích vì chính sách, thông báo cho người dùng
            if (response.getBodyType().equals("Unknown") &&
                    (response.getAnalysis().contains("không thể phân tích") ||
                            response.getAnalysis().contains("Xin lỗi") ||
                            response.getAnalysis().contains("khuôn mặt"))) {

                // Thử gọi API với prompt thay thế khác (không đề cập đến khuôn mặt)
                logger.info("Retrying with alternative prompt due to policy restriction");
                try {
                    String altResponse = callClaudeApiAlternative(image, height, weight, gender);
                    ImageAnalysisResponseDTO altResult = parseClaudeResponse(altResponse);

                    // Nếu kết quả thay thế hoạt động, sử dụng nó
                    if (!altResult.getBodyType().equals("Unknown")) {
                        logger.info("Alternative prompt succeeded with body type: {}", altResult.getBodyType());
                        return ResponseEntity.ok(altResult);
                    }
                } catch (Exception ex) {
                    logger.error("Alternative prompt also failed: {}", ex.getMessage());
                }

                // Nếu các cách tiếp cận thay thế đều thất bại, trả về lỗi chuẩn
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ImageAnalysisResponseDTO("Unknown",
                                "Không thể phân tích ảnh vì chính sách Claude AI. Hãy chọn ảnh không có khuôn mặt.",
                                "Vui lòng sử dụng ảnh chỉ hiển thị cơ thể (không có mặt) hoặc chọn loại cơ thể thủ công."));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to analyze image: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ImageAnalysisResponseDTO("Unknown",
                            "Không thể phân tích hình ảnh: " + e.getMessage(),
                            "Vui lòng tải lên ảnh khác hoặc chọn loại cơ thể thủ công."));
        }
    }

    private String callClaudeApi(MultipartFile image, Double height, Double weight, String gender) throws Exception {
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

        // Tạo nội dung yêu cầu
        Map<String, Object> requestBody = new HashMap<>();
        // Sử dụng model được cấu hình hoặc dùng giá trị mặc định
        requestBody.put("model", claudeModel != null ? claudeModel : "claude-3-haiku-20240307");
        requestBody.put("max_tokens", 1000);

        // Tạo nội dung system message - ĐÃ CẢI TIẾN THEO HƯỚNG MỚI
        String systemPrompt = "Bạn là một huấn luyện viên thể hình chuyên nghiệp, có khả năng phân tích hình thể khách quan dựa trên hình ảnh và các thông số. " + 
                "Khi đánh giá ảnh cơ thể, bạn CHỈ tập trung vào hình dạng cơ thể, phân bố cơ-mỡ, và cấu trúc cơ bắp. " + 
                "Đây là một ứng dụng fitness nên bạn cần đưa ra phân tích chuyên nghiệp, chi tiết và hữu ích. " +
                "Khi nhìn thấy một bức ảnh, tập trung vào các phần cơ thể hiển thị và bỏ qua các phần khác. " +
                "TRẢ LỜI HOÀN TOÀN BẰNG TIẾNG VIỆT. " +
                "QUAN TRỌNG: Luôn xưng hô với người dùng là 'bạn' thay vì 'người này' hoặc các đại từ khác để tạo sự thân thiện. " +
                "Hãy phân tích thật kỹ tỷ lệ mỡ/cơ trong hình ảnh. Đặc biệt chú ý tới phần bụng, hông, ngực và tay để phân loại chính xác. " +
                "Hãy thật cẩn thận khi phân loại hình thể, đặc biệt là phân biệt giữa 'Normal' và 'Skinny Fat': " +
                "- Normal: Cơ thể cân đối, ít mỡ bụng, có đường nét cơ bắp khá rõ ràng. " +
                "- Skinny Fat: Cân nặng có vẻ bình thường nhưng có mỡ bụng, cơ bắp ít và không rõ nét. " +
                
                "Khi đánh giá ảnh cơ thể, hãy phân tích chi tiết các yếu tố sau:\n\n" +
                "1. Tỷ lệ cơ-mỡ: Xác định tỷ lệ cơ-mỡ tương đối (thấp, trung bình, cao), phân bố mỡ chính trên cơ thể (mỡ bụng, mỡ dưới da, mỡ nội tạng).\n" +
                "2. Đường nét cơ bắp: Đánh giá độ rõ nét của các nhóm cơ (cơ ngực, cơ tay, cơ bụng, cơ chân), sự cân đối giữa các nhóm cơ.\n" +
                "3. Tỷ lệ cơ thể: Phân tích tỷ lệ vai-eo-hông, chiều dài tay/chân so với thân, sự cân đối tổng thể.\n" +
                "4. Điểm mạnh/yếu: Xác định 2-3 điểm mạnh và 2-3 điểm yếu rõ ràng nhất của hình thể.\n\n" + 
                
                "Dựa trên phân tích, hãy phân loại thân hình vào một trong các nhóm sau và giải thích lý do:\n" + 
                "- Thin (Gầy): Cơ thể thiếu cân, ít cơ bắp, tỷ lệ mỡ thấp.\n" + 
                "- Skinny Fat (Gầy có mỡ): Cân nặng có thể bình thường hoặc thấp nhưng tỷ lệ mỡ cao, ít cơ bắp.\n" + 
                "- Overweight (Thừa cân): Cân nặng và tỷ lệ mỡ cao hơn mức khuyến nghị.\n" + 
                "- Normal (Bình thường): Cân nặng cân đối, tỷ lệ cơ-mỡ trong mức trung bình.\n" + 
                "- Fit (Có cơ bắp): Cân nặng hợp lý, tỷ lệ mỡ thấp, cơ bắp rõ nét.\n\n" +
                
                "Đề xuất tập luyện cần bao gồm:\n" + 
                "1. Kế hoạch tổng thể: Mục tiêu tập luyện phù hợp với thể trạng (tăng cơ, giảm mỡ, cân bằng, v.v.).\n" + 
                "2. Tần suất tập luyện: Số buổi/tuần, thời gian/buổi phù hợp.\n" + 
                "3. Các bài tập cụ thể (ít nhất 5 bài): Tên bài tập, nhóm cơ mục tiêu, số hiệp/lần lặp lại.\n" + 
                "4. Dinh dưỡng: Hướng dẫn ngắn gọn về chế độ ăn phù hợp (lượng protein, carbs, chất béo).\n" + 
                "5. Cardio và nghỉ ngơi: Đề xuất về cardio và thời gian nghỉ ngơi/phục hồi.\n\n" +
                
                "Mục tiêu của bạn là đưa ra đánh giá cơ thể chuyên nghiệp, khách quan và hữu ích cho khách hàng. " +
                "Không phân tích các phần không liên quan đến thể hình như khuôn mặt nếu có. " +
                "Hãy trả về CHÍNH XÁC định dạng JSON với cấu trúc sau:" +
                "{\n" +
                "  \"bodyType\": \"[Loại hình thể - chọn một trong các loại: Thin (gầy), Skinny Fat (gầy có mỡ), Overweight (thừa cân), Normal (bình thường), hoặc Fit (có cơ bắp)]\",\n" +
                "  \"analysis\": \"[Phân tích chi tiết theo 5 yếu tố đã nêu ở trên, với ngôn ngữ TIẾNG VIỆT rõ ràng, dễ hiểu]\",\n" +
                "  \"recommendations\": \"[Đề xuất chi tiết 5 khía cạnh về tập luyện và dinh dưỡng như đã nêu ở trên, BẰNG TIẾNG VIỆT]\"\n" +
                "}";
        
        // Thêm system message như một tham số cấp cao
        requestBody.put("system", systemPrompt);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        
        // Chỉ thêm user message
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        // Tạo nội dung tin nhắn user - ĐÃ CẢI TIẾN
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
                
                "Hãy trả về KẾT QUẢ DẠNG JSON với 3 trường: bodyType, analysis, và recommendations. " +
                "TRẢ LỜI HOÀN TOÀN BẰNG TIẾNG VIỆT với ngôn ngữ dễ hiểu nhưng chuyên nghiệp, tránh dùng tiếng Anh trừ các thuật ngữ thông dụng. " +
                "QUAN TRỌNG: Sử dụng từ 'bạn' thay vì 'người này' khi đề cập đến chủ nhân của hình thể trong phân tích.";

        List<Map<String, Object>> content = new ArrayList<>();

        // Thêm nội dung text
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        content.add(textContent);

        // Thêm nội dung hình ảnh
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

        // Gửi yêu cầu và nhận kết quả
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            logger.info("Sending request to Claude API with model: {}", claudeModel);
            logger.info("Request contains system message as top-level parameter + user prompt");

            ResponseEntity<Map> response = restTemplate.exchange(claudeApiUrl, HttpMethod.POST, entity, Map.class);
            HttpStatus statusCode = response.getStatusCode();

            if (statusCode.is2xxSuccessful()) {
                logger.info("Claude API call successful with status: {}", statusCode);

                // Parse và trả về kết quả
                Map responseBody = response.getBody();
                logger.debug("Claude API response received");

                // Thử phương pháp 1: Parse đứng đắn
                try {
                    ObjectMapper objectMapper = new ObjectMapper();

                    if (responseBody != null && responseBody.containsKey("content")) {
                        List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseBody.get("content");
                        if (!contentList.isEmpty()) {
                            Map<String, Object> firstContent = contentList.get(0);
                            if (firstContent.containsKey("text")) {
                                String text = (String) firstContent.get("text");
                                logger.info("Successfully parsed Claude response");
                                return text;
                            }
                        }
                    }

                    // Nếu cách trên không được, thử cách khác
                    String jsonString = objectMapper.writeValueAsString(responseBody);
                    if (jsonString.contains("\"content\":")) {
                        try {
                            JsonNode rootNode = objectMapper.readTree(jsonString);
                            JsonNode contentNode = rootNode.get("content");
                            if (contentNode != null && contentNode.isArray() && contentNode.size() > 0) {
                                JsonNode firstContentNode = contentNode.get(0);
                                if (firstContentNode.has("text")) {
                                    String text = firstContentNode.get("text").asText();
                                    logger.info("Successfully parsed Claude response with JsonNode");
                                    return text;
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error parsing JSON with JsonNode: {}", e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error converting response to JSON: {}", e.getMessage());
                }

                // Nếu không thể parse một cách chuẩn, trả về thông báo lỗi
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Không thể phân tích chi tiết từ AI. Có lỗi khi xử lý phản hồi.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            } else {
                logger.error("Claude API call failed with status: {}", statusCode);
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi kết nối tới Claude API. Mã trạng thái: " + statusCode + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            }
        } catch (Exception e) {
            logger.error("Error when calling Claude API: {}", e.getMessage(), e);

            // Trả về JSON mặc định với thông báo lỗi
            if (e instanceof org.springframework.web.client.HttpClientErrorException) {
                org.springframework.web.client.HttpClientErrorException clientError = (org.springframework.web.client.HttpClientErrorException)e;
                logger.error("HTTP Client Error: {} - {}", clientError.getStatusCode(), clientError.getResponseBodyAsString());

                if (clientError.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi xác thực với Claude API. Kiểm tra API key: " + clientError.getStatusText() + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
                } else if (clientError.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    return "{\"bodyType\": \"Normal\", \"analysis\": \"Vượt quá giới hạn yêu cầu API. Vui lòng chờ một lúc rồi thử lại.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
                } else if (clientError.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    return "{\"bodyType\": \"Normal\", \"analysis\": \"Yêu cầu không hợp lệ. Lỗi: " + clientError.getResponseBodyAsString() + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
                }
            } else if (e instanceof org.springframework.web.client.HttpServerErrorException) {
                org.springframework.web.client.HttpServerErrorException serverError = (org.springframework.web.client.HttpServerErrorException)e;
                logger.error("HTTP Server Error: {} - {}", serverError.getStatusCode(), serverError.getResponseBodyAsString());

                if (serverError.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                    return "{\"bodyType\": \"Normal\", \"analysis\": \"Dịch vụ Claude API hiện không khả dụng. Vui lòng thử lại sau.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
                }
            } else if (e instanceof org.springframework.web.client.ResourceAccessException) {
                logger.error("Network error: {}", e.getMessage());
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi mạng khi kết nối đến Claude API. Hãy kiểm tra kết nối mạng của bạn.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            }

            if (e.getMessage().contains("401")) {
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi xác thực với Claude API. Vui lòng kiểm tra API key.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            } else if (e.getMessage().contains("429")) {
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Vượt quá hạn mức cuộc gọi API. Vui lòng thử lại sau.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            } else {
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi kết nối tới Claude API: " + e.getMessage() + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            }
        }
    }

    private ImageAnalysisResponseDTO parseClaudeResponse(String claudeResponse) {
        logger.info("Parsing Claude response: {}", claudeResponse);
        ObjectMapper objectMapper = new ObjectMapper();

        // Kiểm tra nếu phản hồi chứa thông báo lỗi về chính sách Claude
        if (claudeResponse.contains("không thể phân tích") ||
                claudeResponse.contains("không được yêu cầu nhận dạng") ||
                claudeResponse.contains("không thể nhận xét") ||
                claudeResponse.contains("face-blind") ||
                claudeResponse.contains("Xin lỗi")) {

            logger.warn("Claude policy restriction detected in response: {}", claudeResponse);
            ImageAnalysisResponseDTO policyRestriction = new ImageAnalysisResponseDTO();
            policyRestriction.setBodyType("Unknown");
            policyRestriction.setAnalysis("Không thể phân tích chi tiết từ AI. Phần hồi gốc: " + claudeResponse);
            policyRestriction.setRecommendations("Vui lòng chọn một ảnh khác không có khuôn mặt hoặc chọn loại cơ thể thủ công từ danh sách.");
            return policyRestriction;
        }

        try {
            // Tìm và trích xuất JSON từ phản hồi của Claude
            int startIndex = claudeResponse.indexOf("{");
            int endIndex = claudeResponse.lastIndexOf("}") + 1;

            if (startIndex >= 0 && endIndex > startIndex) {
                // Trích xuất phần JSON từ văn bản
                String jsonPart = claudeResponse.substring(startIndex, endIndex);
                logger.info("Extracted JSON part: {}", jsonPart);

                try {
                    // Sử dụng Jackson để parse JSON trực tiếp thành DTO
                    return objectMapper.readValue(jsonPart, ImageAnalysisResponseDTO.class);
                } catch (Exception e) {
                    logger.error("Could not parse JSON to ImageAnalysisResponseDTO: {}", e.getMessage());

                    // Nếu không thể parse trực tiếp, thử parse thành JsonNode
                    try {
                        JsonNode jsonNode = objectMapper.readTree(jsonPart);
                        ImageAnalysisResponseDTO result = new ImageAnalysisResponseDTO();

                        if (jsonNode.has("bodyType")) {
                            result.setBodyType(jsonNode.get("bodyType").asText());
                        } else {
                            result.setBodyType("Normal");
                        }

                        if (jsonNode.has("analysis")) {
                            result.setAnalysis(jsonNode.get("analysis").asText());
                        } else {
                            result.setAnalysis("Không tìm thấy phân tích trong phản hồi.");
                        }

                        if (jsonNode.has("recommendations")) {
                            result.setRecommendations(jsonNode.get("recommendations").asText());
                        } else {
                            result.setRecommendations("Không tìm thấy đề xuất trong phản hồi.");
                        }

                        return result;
                    } catch (Exception ex) {
                        logger.error("Error parsing with JsonNode: {}", ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting JSON: {}", e.getMessage());
        }

        // Nếu không thể parse, trả về kết quả mặc định với toàn bộ phản hồi
        ImageAnalysisResponseDTO fallback = new ImageAnalysisResponseDTO();
        fallback.setBodyType("Normal");
        fallback.setAnalysis("Không thể phân tích chi tiết từ AI. Phản hồi gốc: " + claudeResponse);
        fallback.setRecommendations("Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.");
        return fallback;
    }

    /**
     * Phương pháp thay thế để gọi Claude API khi phương pháp chính gặp hạn chế về chính sách
     * Sử dụng cách tiếp cận khác với prompt không đề cập đến khuôn mặt
     */
    private String callClaudeApiAlternative(MultipartFile image, Double height, Double weight, String gender) throws Exception {
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

        // Chỉ dùng một tin nhắn người dùng
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");

        // Tạo nội dung tin nhắn người dùng không đề cập đến khuôn mặt
        String prompt = "Tôi là huấn luyện viên fitness. Hãy giúp tôi phân tích thân hình cơ thể trong ảnh này. " +
                "Thông số cơ thể: Cao " + height + "cm, nặng " + weight + "kg, giới tính: " + gender + ". " +
                "Bạn có thể phân tích và phân loại hình thể này không? " +
                "Hãy phân loại hình thể vào một trong các nhóm: Thin (gầy), Skinny Fat (gầy có mỡ), Overweight (thừa cân), Normal (bình thường), hoặc Fit (có cơ bắp). " +
                "Cũng hãy đánh giá tỷ lệ cơ/mỡ, điểm mạnh và điểm yếu, và đề xuất các bài tập phù hợp. " +
                "Cho tôi kết quả dạng JSON có bodyType, analysis, và recommendations. Đừng kèm giải thích, chỉ trả kết quả JSON.";

        List<Map<String, Object>> content = new ArrayList<>();

        // Thêm nội dung text
        Map<String, Object> textContent = new HashMap<>();
        textContent.put("type", "text");
        textContent.put("text", prompt);
        content.add(textContent);

        // Thêm nội dung hình ảnh
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

        // Gửi yêu cầu và nhận kết quả
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            logger.info("Sending alternative request to Claude API");

            ResponseEntity<Map> response = restTemplate.exchange(claudeApiUrl, HttpMethod.POST, entity, Map.class);
            HttpStatus statusCode = response.getStatusCode();

            if (statusCode.is2xxSuccessful()) {
                logger.info("Alternative Claude API call successful");

                // Parse và trả về kết quả
                Map responseBody = response.getBody();

                if (responseBody != null && responseBody.containsKey("content")) {
                    List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseBody.get("content");
                    if (!contentList.isEmpty()) {
                        Map<String, Object> firstContent = contentList.get(0);
                        if (firstContent.containsKey("text")) {
                            String text = (String) firstContent.get("text");
                            logger.info("Successfully parsed alternative Claude response");
                            return text;
                        }
                    }
                }

                // Nếu không thể parse, trả về thông báo lỗi
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Không thể phân tích chi tiết từ cách tiếp cận thay thế.\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            } else {
                logger.error("Alternative Claude API call failed with status: {}", statusCode);
                return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi kết nối tới Claude API với cách tiếp cận thay thế. Mã trạng thái: " + statusCode + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
            }
        } catch (Exception e) {
            logger.error("Error when calling alternative Claude API: {}", e.getMessage());
            return "{\"bodyType\": \"Normal\", \"analysis\": \"Lỗi khi gọi Claude API với cách tiếp cận thay thế: " + e.getMessage() + "\", \"recommendations\": \"Đề xuất mặc định: Tập trung vào các bài tập cardio và cường độ vừa phải.\"}";
        }
    }
}