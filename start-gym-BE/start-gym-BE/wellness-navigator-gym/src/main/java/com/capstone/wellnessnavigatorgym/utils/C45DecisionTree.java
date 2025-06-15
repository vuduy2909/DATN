package com.capstone.wellnessnavigatorgym.utils;

import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Cải thiện thuật toán C4.5 Decision Tree cho việc recommend khóa học
 * - Xử lý dữ liệu đa dạng tốt hơn
 * - Ưu tiên training_goals làm thuộc tính chính 
 * - Tối ưu cho 10,000 bản ghi với 70 khóa học
 */
@Component
public class C45DecisionTree {
    
    private static final Logger logger = LoggerFactory.getLogger(C45DecisionTree.class);
    private List<TrackDataAi> trainingData;
    private List<Course> courses;
    
    // Thứ tự ưu tiên thuộc tính: training_goals là quan trọng nhất, training_frequency là thứ 2
    // Loại bỏ waist_circumference và chest_circumference vì chỉ dùng để đánh giá hiệu quả sau tập
    private static final List<String> ATTRIBUTE_PRIORITY = Arrays.asList(
        "training_goals",    // Quan trọng nhất - mục tiêu tập luyện (thuộc tính chính)
        "training_frequency", // Quan trọng thứ 2 - tần suất tập luyện (thuộc tính phụ quan trọng)
        "body_type",         // Thể hình hiện tại
        "training_history",  // Kinh nghiệm tập luyện
        "age",              // Độ tuổi
        "bmi",              // Chỉ số BMI
        "gender",           // Giới tính
        "occupation",       // Nghề nghiệp
        "work_environment", // Môi trường làm việc
        "height",           // Chiều cao
        "weight"            // Cân nặng
    );
    
    // Enhanced mapping training goals với các khóa học phù hợp - CẢI THIỆN LOGIC
    private static final Map<String, List<Integer>> TRAINING_GOALS_MAPPING = new HashMap<String, List<Integer>>() {{
        // Strength Training - Tập trung vào tăng sức mạnh và phát triển cơ bắp
        put("1, Strength Training", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 25, 26, 27, 28));
        put("STRENGTH_TRAINING", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 25, 26, 27, 28));
        
        // Cardiovascular Training - Tập trung vào tim mạch và sức bền
        put("2, Cardiovascular Training", Arrays.asList(9, 10, 11, 12, 13, 14, 19, 20, 21, 22, 29, 30, 31, 32));
        put("CARDIOVASCULAR_TRAINING", Arrays.asList(9, 10, 11, 12, 13, 14, 19, 20, 21, 22, 29, 30, 31, 32));
        
        // Flexibility and Balance - Tập trung vào linh hoạt và thăng bằng
        put("3, Flexibility and Balance", Arrays.asList(33, 34, 35, 36, 37, 38, 39, 40, 47, 48, 49, 50));
        put("FLEXIBILITY_AND_BALANCE", Arrays.asList(33, 34, 35, 36, 37, 38, 39, 40, 47, 48, 49, 50));
        
        // Train strength and endurance - Kết hợp sức mạnh và sức bền
        put("4, Train strength and endurance", Arrays.asList(23, 24, 25, 26, 27, 28, 41, 42, 43, 44, 51, 52));
        put("TRAIN_STRENGTH_AND_ENDURANCE", Arrays.asList(23, 24, 25, 26, 27, 28, 41, 42, 43, 44, 51, 52));
        
        // Mind-Body Wellness - Tập trung vào sức khỏe tinh thần
        put("5, Mind-Body Wellness", Arrays.asList(45, 46, 47, 48, 49, 50, 57, 58, 59, 60));
        put("MIND_BODY_WELLNESS", Arrays.asList(45, 46, 47, 48, 49, 50, 57, 58, 59, 60));
        
        // Functional Training - Tập luyện chức năng
        put("6, Functional Training", Arrays.asList(53, 54, 55, 56, 61, 62, 63, 64, 65, 66));
        put("FUNCTIONAL_TRAINING", Arrays.asList(53, 54, 55, 56, 61, 62, 63, 64, 65, 66));
        
        // Weight Loss - Giảm cân (mở rộng với nhiều khóa học hơn)
        put("7, Weight Loss", Arrays.asList(67, 68, 69, 70, 71, 72, 73, 29, 30, 31, 32, 9, 10, 11, 12));
        put("WEIGHT_LOSS", Arrays.asList(67, 68, 69, 70, 71, 72, 73, 29, 30, 31, 32, 9, 10, 11, 12));
    }};
    
    // Mapping training frequency với độ khó khóa học
    private static final Map<String, List<String>> TRAINING_FREQUENCY_MAPPING = new HashMap<String, List<String>>() {{
        put("Never", Arrays.asList("beginner", "basic", "intro")); // Người mới bắt đầu
        put("Occasionally", Arrays.asList("beginner", "basic", "easy")); // Thỉnh thoảng
        put("Moderate", Arrays.asList("intermediate", "moderate", "standard")); // Vừa phải
        put("Regularly", Arrays.asList("intermediate", "advanced", "regular")); // Thường xuyên
        put("Advanced", Arrays.asList("advanced", "expert", "professional")); // Nâng cao
    }};
    
    // Threshold minimum cho entropy để tạo leaf node - Điều chỉnh để ưu tiên training_goals
    private static final double MIN_ENTROPY_THRESHOLD = 0.15; // Tăng để tạo cây nhỏ hơn, tập trung vào thuộc tính quan trọng
    private static final int MIN_SAMPLES_FOR_SPLIT = 8; // Giảm để có thể phân chia tốt hơn
    
    public C45DecisionTree() {}
    
    public C45DecisionTree(List<TrackDataAi> trainingData) {
        this.trainingData = trainingData;
    }
    
    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }
    
    public void setTrainingData(List<TrackDataAi> trainingData) {
        this.trainingData = trainingData;
    }
    
    /**
     * Xây dựng cây quyết định C4.5 được cải thiện với ưu tiên training_goals
     */
    public TreeNode buildDecisionTree() {
        if (trainingData == null || trainingData.isEmpty()) {
            logger.error("Training data is null or empty");
            return null;
        }
        
        // Lọc chỉ lấy dữ liệu effective = true để training
        List<TrackDataAi> effectiveData = trainingData.stream()
            .filter(data -> data.getCourse() != null && 
                          data.getEffective() != null && 
                          data.getEffective())
            .collect(Collectors.toList());
        
        logger.info("Building C4.5 decision tree with {} effective training records", effectiveData.size());
        
        if (effectiveData.size() < MIN_SAMPLES_FOR_SPLIT) {
            logger.warn("Insufficient effective training data: {} records", effectiveData.size());
            return createMajorityCourseLeafNode(effectiveData);
        }
        
        // Phân tích phân phối course_id trong dữ liệu effective
        Map<Integer, Long> courseDistribution = effectiveData.stream()
            .collect(Collectors.groupingBy(
                d -> d.getCourse().getCourseId(), 
                Collectors.counting()
            ));
        
        logger.info("Course distribution in effective data: {} unique courses", courseDistribution.size());
        logger.debug("Top courses: {}", courseDistribution.entrySet().stream()
            .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList())
            .stream()
            .map(entry -> entry.getKey() + "=" + entry.getValue())
            .collect(Collectors.joining(", ")));
        
        return buildC45TreeRecursive(effectiveData, new ArrayList<>(ATTRIBUTE_PRIORITY));
    }
    
    /**
     * Đệ quy xây dựng cây C4.5
     */
    private TreeNode buildC45TreeRecursive(List<TrackDataAi> data, List<String> remainingAttributes) {
        if (data.isEmpty()) {
            logger.warn("Empty dataset encountered in tree building");
            return null;
        }
        
        // Kiểm tra stopping conditions
        double entropy = calculateEntropyByCourse(data);
        logger.debug("Current entropy: {} for {} samples", entropy, data.size());
        
        if (entropy <= MIN_ENTROPY_THRESHOLD || 
            data.size() < MIN_SAMPLES_FOR_SPLIT || 
            remainingAttributes.isEmpty()) {
            logger.debug("Stopping condition met: entropy={}, samples={}, remaining_attributes={}", 
                entropy, data.size(), remainingAttributes.size());
            return createMajorityCourseLeafNode(data);
        }
        
        // Kiểm tra nếu tất cả samples có cùng course_id
        Set<Integer> uniqueCourses = data.stream()
            .filter(d -> d.getCourse() != null)
            .map(d -> d.getCourse().getCourseId())
            .collect(Collectors.toSet());
        
        if (uniqueCourses.size() <= 1) {
            logger.debug("All samples have same course_id, creating leaf node");
            return createMajorityCourseLeafNode(data);
        }
        
        // Tìm thuộc tính tốt nhất để split
        String bestAttribute = findBestAttributeC45(data, remainingAttributes);
        if (bestAttribute == null) {
            logger.warn("No valid attribute found for splitting, creating leaf node");
            return createMajorityCourseLeafNode(data);
        }
        
        logger.debug("Selected best attribute: {} with {} samples", bestAttribute, data.size());
        
        // Tạo node với thuộc tính tốt nhất
        TreeNode node = new TreeNode(bestAttribute);
        node.setLeaf(false);
        
        // Phân chia dữ liệu theo thuộc tính được chọn
        Map<Object, List<TrackDataAi>> subsets = partitionDataByAttribute(data, bestAttribute);
        Map<Object, TreeNode> children = new HashMap<>();
        
        // Kiểm tra nếu không thể phân chia dữ liệu
        if (subsets.isEmpty() || subsets.size() == 1) {
            logger.warn("Attribute {} cannot split data effectively, creating leaf node", bestAttribute);
            return createMajorityCourseLeafNode(data);
        }
        
        // Loại bỏ thuộc tính đã sử dụng
        List<String> newRemainingAttributes = new ArrayList<>(remainingAttributes);
        newRemainingAttributes.remove(bestAttribute);
        
        logger.debug("Building children for attribute {} with {} subsets", bestAttribute, subsets.size());
        
        for (Map.Entry<Object, List<TrackDataAi>> entry : subsets.entrySet()) {
            Object attributeValue = entry.getKey();
            List<TrackDataAi> subset = entry.getValue();
            
            if (!subset.isEmpty()) {
                logger.debug("Processing subset for value: {} with {} samples", attributeValue, subset.size());
                TreeNode childNode = buildC45TreeRecursive(subset, newRemainingAttributes);
                if (childNode != null) {
                    children.put(attributeValue, childNode);
                } else {
                    logger.warn("Child node creation failed for value: {}", attributeValue);
                }
            }
        }
        
        // Kiểm tra nếu không có children nào được tạo thành công
        if (children.isEmpty()) {
            logger.warn("No valid children created for attribute {}, creating leaf node", bestAttribute);
            return createMajorityCourseLeafNode(data);
        }
        
        node.setChildren(children);
        logger.debug("Successfully created node for attribute {} with {} children", bestAttribute, children.size());
        return node;
    }
    
    /**
     * Tìm thuộc tính tốt nhất sử dụng Gain Ratio (C4.5) với ưu tiên training_goals và training_frequency
     */
    private String findBestAttributeC45(List<TrackDataAi> data, List<String> attributes) {
        String bestAttribute = null;
        double bestGainRatio = -1.0;
        int validAttributeCount = 0;
        
        // Ưu tiên training_goals và training_frequency - kiểm tra trước với bonus
        for (String priorityAttribute : Arrays.asList("training_goals", "training_frequency")) {
            if (attributes.contains(priorityAttribute) && TrackDataAi.isValidAttribute(priorityAttribute)) {
                double gainRatio = calculateGainRatio(data, priorityAttribute);
                logger.debug("Priority Attribute: {}, Gain Ratio: {}", priorityAttribute, gainRatio);
                
                // Nếu training_goals hoặc training_frequency có gain ratio dương, ưu tiên chọn
                if (gainRatio > 0) {
                    // Thêm bonus cho training_goals và training_frequency
                    double adjustedGainRatio = gainRatio;
                    if ("training_goals".equals(priorityAttribute)) {
                        adjustedGainRatio = gainRatio * 1.5; // Bonus 50% cho training_goals
                        logger.debug("Adjusted gain ratio for training_goals: {} -> {}", gainRatio, adjustedGainRatio);
                    } else if ("training_frequency".equals(priorityAttribute)) {
                        adjustedGainRatio = gainRatio * 1.3; // Bonus 30% cho training_frequency
                        logger.debug("Adjusted gain ratio for training_frequency: {} -> {}", gainRatio, adjustedGainRatio);
                    }
                    
                    if (adjustedGainRatio > bestGainRatio) {
                        bestGainRatio = adjustedGainRatio;
                        bestAttribute = priorityAttribute;
                        logger.info("🎯 Selected priority attribute: {} with adjusted gain ratio: {}", priorityAttribute, adjustedGainRatio);
                    }
                }
            }
        }
        
        // Nếu đã chọn được thuộc tính ưu tiên, trả về luôn
        if (bestAttribute != null && (bestAttribute.equals("training_goals") || bestAttribute.equals("training_frequency"))) {
            logger.info("🚀 Using priority attribute {} for tree split", bestAttribute);
            return bestAttribute;
        }
        
        // Nếu chưa có thuộc tính ưu tiên phù hợp, tiếp tục kiểm tra các thuộc tính khác
        for (String attribute : attributes) {
            if (TrackDataAi.isValidAttribute(attribute) && 
                !attribute.equals("training_goals") && 
                !attribute.equals("training_frequency")) {
                validAttributeCount++;
                double gainRatio = calculateGainRatio(data, attribute);
                
                logger.debug("Attribute: {}, Gain Ratio: {}", attribute, gainRatio);
                
                // Only consider positive gain ratios
                if (gainRatio > bestGainRatio && gainRatio > 0) {
                    bestGainRatio = gainRatio;
                    bestAttribute = attribute;
                }
            }
        }
        
        if (validAttributeCount == 0) {
            logger.warn("No valid attributes found for selection");
            return null;
        }
        
        if (bestAttribute == null) {
            logger.warn("No attribute with positive gain ratio found. Best gain ratio was: {}", bestGainRatio);
            
            // If no positive gain ratio found, try to select the attribute with highest (least negative) value
            bestGainRatio = Double.NEGATIVE_INFINITY;
            for (String attribute : attributes) {
                if (TrackDataAi.isValidAttribute(attribute)) {
                    double gainRatio = calculateGainRatio(data, attribute);
                    if (gainRatio > bestGainRatio) {
                        bestGainRatio = gainRatio;
                        bestAttribute = attribute;
                    }
                }
            }
        }
        
        logger.debug("Best attribute: {} with Gain Ratio: {}", bestAttribute, bestGainRatio);
        return bestAttribute;
    }
    
    /**
     * Tính toán Gain Ratio (C4.5 improvement over ID3)
     */
    private double calculateGainRatio(List<TrackDataAi> data, String attribute) {
        double informationGain = calculateInformationGain(data, attribute);
        double splitInfo = calculateSplitInfo(data, attribute);
        
        // Handle edge cases for better algorithm stability
        if (splitInfo == 0 || Double.isNaN(splitInfo) || Double.isInfinite(splitInfo)) {
            // If splitInfo is 0, the attribute doesn't split the data meaningfully
            // Return a small gain ratio based on information gain only
            return informationGain > 0 ? informationGain * 0.1 : 0.0;
        }
        
        double gainRatio = informationGain / splitInfo;
        
        // Sanity check for numerical stability
        if (Double.isNaN(gainRatio) || Double.isInfinite(gainRatio)) {
            logger.warn("Invalid gain ratio calculated for attribute {}: informationGain={}, splitInfo={}", 
                attribute, informationGain, splitInfo);
            return 0.0;
        }
        
        return gainRatio;
    }
    
    /**
     * Tính toán Information Gain dựa trên course_id distribution
     */
    private double calculateInformationGain(List<TrackDataAi> data, String attribute) {
        double entropyBefore = calculateEntropyByCourse(data);
        
        Map<Object, List<TrackDataAi>> subsets = partitionDataByAttribute(data, attribute);
        double weightedEntropy = 0.0;
        
        for (List<TrackDataAi> subset : subsets.values()) {
            double weight = (double) subset.size() / data.size();
            double subsetEntropy = calculateEntropyByCourse(subset);
            weightedEntropy += weight * subsetEntropy;
        }
        
        return entropyBefore - weightedEntropy;
    }
    
    /**
     * Tính toán entropy dựa trên phân bố course_id
     */
    private double calculateEntropyByCourse(List<TrackDataAi> data) {
        if (data == null || data.isEmpty()) {
            return 0.0;
        }
        
        // Đếm số lượng instance cho từng course_id
        Map<Integer, Long> courseCounts = data.stream()
            .filter(d -> d.getCourse() != null)
            .collect(Collectors.groupingBy(
                d -> d.getCourse().getCourseId(), 
                Collectors.counting()
            ));
        
        if (courseCounts.isEmpty()) {
            logger.debug("No courses found in data subset, entropy = 0");
            return 0.0;
        }
        
        // If all samples have the same course, entropy is 0
        if (courseCounts.size() == 1) {
            return 0.0;
        }
        
        double totalCount = data.size();
        double entropy = 0.0;
        
        for (Long count : courseCounts.values()) {
            if (count > 0) {
                double probability = count / totalCount;
                // Add small epsilon to prevent log(0)
                double logValue = Math.log(probability) / Math.log(2);
                entropy -= probability * logValue;
            }
        }
        
        // Ensure entropy is not negative due to floating point errors
        entropy = Math.max(0.0, entropy);
        
        logger.debug("Calculated entropy: {} for {} courses with distribution: {}", 
            entropy, courseCounts.size(), courseCounts);
        
        return entropy;
    }
    
    /**
     * Tính toán Split Information
     */
    private double calculateSplitInfo(List<TrackDataAi> data, String attribute) {
        Map<Object, List<TrackDataAi>> subsets = partitionDataByAttribute(data, attribute);
        
        if (subsets.isEmpty()) {
            logger.debug("No subsets created for attribute {}, split info = 0", attribute);
            return 0.0;
        }
        
        // If all data goes to one subset, no information is gained from splitting
        if (subsets.size() == 1) {
            return 0.0;
        }
        
        double totalSize = data.size();
        double splitInfo = 0.0;
        
        for (List<TrackDataAi> subset : subsets.values()) {
            if (!subset.isEmpty()) {
                double proportion = subset.size() / totalSize;
                if (proportion > 0) {
                    double logValue = Math.log(proportion) / Math.log(2);
                    splitInfo -= proportion * logValue;
                }
            }
        }
        
        // Ensure split info is not negative
        splitInfo = Math.max(0.0, splitInfo);
        
        logger.debug("Split info for attribute {}: {} (subsets: {})", 
            attribute, splitInfo, subsets.size());
        
        return splitInfo;
    }
    
    /**
     * Phân chia dữ liệu theo giá trị của thuộc tính
     */
    private Map<Object, List<TrackDataAi>> partitionDataByAttribute(List<TrackDataAi> data, String attribute) {
        if (!TrackDataAi.isValidAttribute(attribute)) {
            logger.warn("Invalid attribute for partitioning: {}", attribute);
            return new HashMap<>();
        }
        
        Map<Object, List<TrackDataAi>> partitions = data.stream()
            .filter(item -> item.getAttributeValue(attribute) != null)
            .collect(Collectors.groupingBy(item -> item.getAttributeValue(attribute)));
        
        // Đặc biệt logging cho training_goals và training_frequency để đảm bảo phân chia hiệu quả
        if ("training_goals".equals(attribute) || "training_frequency".equals(attribute)) {
            logger.info("📊 Partitioning by {} created {} groups:", attribute, partitions.size());
            for (Map.Entry<Object, List<TrackDataAi>> entry : partitions.entrySet()) {
                Object key = entry.getKey();
                List<TrackDataAi> subset = entry.getValue();
                
                // Phân tích phân bố khóa học trong từng nhóm
                Map<Integer, Long> courseDistribution = subset.stream()
                    .filter(d -> d.getCourse() != null)
                    .collect(Collectors.groupingBy(
                        d -> d.getCourse().getCourseId(), 
                        Collectors.counting()
                    ));
                
                logger.info("  {} = {} (samples: {}, courses: {})", 
                    key, subset.size(), subset.size(), courseDistribution.size());
                
                // Log top courses cho từng nhóm để verify sự khác biệt
                String topCourses = courseDistribution.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> e.getKey() + "(" + e.getValue() + ")")
                    .collect(Collectors.joining(", "));
                logger.debug("    Top courses: {}", topCourses);
            }
        }
        
        logger.debug("Partitioned {} records into {} groups by attribute: {}", 
            data.size(), partitions.size(), attribute);
        return partitions;
    }
    
    /**
     * Tạo leaf node với course_id phổ biến nhất trong data subset
     */
    private TreeNode createMajorityCourseLeafNode(List<TrackDataAi> data) {
        if (data.isEmpty()) {
            return null;
        }
        
        // Tìm course_id phổ biến nhất
        Map<Integer, Long> courseCounts = data.stream()
            .filter(d -> d.getCourse() != null)
            .collect(Collectors.groupingBy(
                d -> d.getCourse().getCourseId(), 
                Collectors.counting()
            ));
        
        if (courseCounts.isEmpty()) {
            return null;
        }
        
        Integer majorityCourseId = courseCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
        
        TreeNode leafNode = new TreeNode(null);
        leafNode.setLeaf(true);
        
        if (majorityCourseId != null) {
            Course course = findCourseById(majorityCourseId);
            if (course != null) {
                leafNode.setCourseDataId(course);
                logger.debug("Created majority leaf node with course ID: {} (count: {})", 
                    majorityCourseId, courseCounts.get(majorityCourseId));
            }
        }
        
        return leafNode;
    }
    
    /**
     * Tìm khóa học dựa trên ID từ danh sách courses
     */
    private Course findCourseById(Integer courseId) {
        if (courseId == null || courses == null) {
            return null;
        }
        
        return courses.stream()
            .filter(course -> course.getCourseId().equals(courseId))
            .findFirst()
            .map(course -> {
                Course newCourse = new Course(courseId);
                newCourse.setCourseName(course.getCourseName());
                newCourse.setDescription(course.getDescription());
                newCourse.setDuration(course.getDuration());
                newCourse.setImage(course.getImage());
                newCourse.setStatus(course.getStatus());
                return newCourse;
            })
            .orElse(null);
    }
    
    /**
     * Duyệt cây quyết định để đưa ra gợi ý khóa học
     */
    public Course traverseDecisionTree(TreeNode root, Map<String, Object> userData) {
        if (root == null) {
            logger.warn("Decision tree root is null");
            return getDefaultCourseBytraining_goals(userData);
        }
        
        // Chuẩn hóa dữ liệu người dùng
        normalizeUserData(userData);
        
        logger.debug("Traversing C4.5 decision tree with user data: {}", userData);
        
        TreeNode currentNode = root;
        int traversalDepth = 0;
        final int MAX_DEPTH = 20; // Tránh infinite loop
        
        while (currentNode != null && !currentNode.isLeaf() && traversalDepth < MAX_DEPTH) {
            String attributeName = currentNode.getAttributeName();
            Object attributeValue = userData.get(attributeName);
            
            logger.debug("Depth {}: Current node attribute: {}, user value: {}", 
                traversalDepth, attributeName, attributeValue);
            
            Map<Object, TreeNode> children = currentNode.getChildren();
            if (children == null || children.isEmpty()) {
                logger.warn("Node has no children, stopping traversal at depth {}", traversalDepth);
                break;
            }
            
            TreeNode nextNode = children.get(attributeValue);
            if (nextNode == null) {
                logger.debug("No exact match for value {}, finding closest match", attributeValue);
                nextNode = findClosestMatch(children, attributeValue, attributeName);
            }
            
            if (nextNode == null) {
                logger.warn("No suitable child node found, stopping traversal at depth {}", traversalDepth);
                break;
            }
            
            currentNode = nextNode;
            traversalDepth++;
        }
        
        // Trả về kết quả
        if (currentNode != null && currentNode.isLeaf() && currentNode.getCourseDataId() != null) {
            Course recommendedCourse = currentNode.getCourseDataId();
            logger.info("✅ C4.5 decision tree recommended course: {} - {} (depth: {})", 
                recommendedCourse.getCourseId(), recommendedCourse.getCourseName(), traversalDepth);
            return recommendedCourse;
        }
        
        // Fallback: sử dụng training goals để đưa ra gợi ý
        logger.warn("Decision tree traversal failed at depth {}, using training goals fallback", traversalDepth);
        return getDefaultCourseBytraining_goals(userData);
    }
    
    /**
     * Tìm nút con gần nhất khi không có exact match
     */
    private TreeNode findClosestMatch(Map<Object, TreeNode> children, Object targetValue, String attributeName) {
        // Đối với training_goals, thử tìm exact match hoặc tương tự
        if ("training_goals".equals(attributeName) && targetValue != null) {
            String targetGoal = targetValue.toString();
            
            // Thử tìm exact match
            for (Object key : children.keySet()) {
                if (key != null && key.toString().equalsIgnoreCase(targetGoal)) {
                    return children.get(key);
                }
            }
            
            // Thử tìm match theo số (ví dụ: "7, Weight Loss" match với "7")
            for (Object key : children.keySet()) {
                if (key != null && targetGoal.contains(key.toString().split(",")[0])) {
                    return children.get(key);
                }
            }
        }
        
        // Đối với các thuộc tính numeric, tìm giá trị gần nhất
        if (targetValue instanceof Number && !children.isEmpty()) {
            double targetNum = ((Number) targetValue).doubleValue();
            Object closestKey = null;
            double minDistance = Double.MAX_VALUE;
            
            for (Object key : children.keySet()) {
                if (key instanceof Number) {
                    double distance = Math.abs(((Number) key).doubleValue() - targetNum);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestKey = key;
                    }
                }
            }
            
            if (closestKey != null) {
                return children.get(closestKey);
            }
        }
        
        // Fallback: trả về child có nhiều training data nhất
        return children.values().stream()
            .max((n1, n2) -> Integer.compare(getNodeDataSize(n1), getNodeDataSize(n2)))
            .orElse(children.values().iterator().next());
    }
    
    /**
     * Ước tính kích thước dữ liệu của node (helper method)
     */
    private int getNodeDataSize(TreeNode node) {
        // Simplified estimation - trong thực tế có thể lưu size trong node
        return 1;
    }
    
    /**
     * Chuẩn hóa dữ liệu người dùng
     */
    private void normalizeUserData(Map<String, Object> userData) {
        // Đảm bảo training_goals có format chuẩn
        Object training_goals = userData.get("training_goals");
        if (training_goals != null) {
            String goals = training_goals.toString();
            // Nếu chỉ có số, thêm mô tả
            if (goals.matches("\\d+")) {
                userData.put("training_goals", mapNumberTotraining_goal(goals));
            }
        }
        
        // Chuẩn hóa training_frequency với mapping chính xác
        Object training_frequency = userData.get("training_frequency");
        if (training_frequency != null) {
            String frequency = training_frequency.toString().trim();
            // Map các giá trị training_frequency về format chuẩn trong database
            String normalizedFrequency = normalizetraining_frequency(frequency);
            userData.put("training_frequency", normalizedFrequency);
            logger.debug("🔄 Normalized training_frequency: {} -> {}", frequency, normalizedFrequency);
        }
        
        // Chuẩn hóa body_type
        Object bodyType = userData.get("body_type");
        if (bodyType == null || bodyType.toString().trim().isEmpty()) {
            userData.put("body_type", "Normal");
        }
        
        // Chuẩn hóa training_history
        Object trainingHistory = userData.get("training_history");
        if (trainingHistory == null || trainingHistory.toString().trim().isEmpty()) {
            userData.put("training_history", "Never");
        }
        
        // Chuẩn hóa BMI ranges
        Object bmi = userData.get("bmi");
        if (bmi instanceof Number) {
            double bmiValue = ((Number) bmi).doubleValue();
            if (bmiValue < 18.5) {
                userData.put("bmi_category", "Underweight");
            } else if (bmiValue < 25) {
                userData.put("bmi_category", "Normal");
            } else if (bmiValue < 30) {
                userData.put("bmi_category", "Overweight");
            } else {
                userData.put("bmi_category", "Obese");
            }
        }
        
        logger.debug("🔧 User data after normalization: training_goals={}, training_frequency={}, body_type={}", 
            userData.get("training_goals"), userData.get("training_frequency"), userData.get("body_type"));
    }
    
    /**
     * Chuẩn hóa training_frequency về format chuẩn
     */
    private String normalizetraining_frequency(String frequency) {
        if (frequency == null || frequency.trim().isEmpty()) {
            return "Moderate";
        }
        
        String freq = frequency.toLowerCase().trim();
        
        // Map các giá trị có thể về format chuẩn
        if (freq.contains("never") || freq.equals("0") || freq.contains("không bao giờ")) {
            return "Never";
        } else if (freq.contains("occasionally") || freq.equals("1") || freq.contains("thỉnh thoảng")) {
            return "Occasionally";
        } else if (freq.contains("moderate") || freq.equals("2") || freq.contains("vừa phải")) {
            return "Moderate";
        } else if (freq.contains("regularly") || freq.equals("3") || freq.contains("thường xuyên")) {
            return "Regularly";
        } else if (freq.contains("advanced") || freq.equals("4") || freq.contains("nâng cao")) {
            return "Advanced";
        }
        
        // Default fallback
        return "Moderate";
    }
    
    /**
     * Map số thành training goal description
     */
    private String mapNumberTotraining_goal(String number) {
        switch (number) {
            case "1": return "1, Strength Training";
            case "2": return "2, Cardiovascular Training";
            case "3": return "3, Flexibility and Balance";
            case "4": return "4, Train strength and endurance";
            case "5": return "5, Mind-Body Wellness";
            case "6": return "6, Functional Training";
            case "7": return "7, Weight Loss";
            default: return "1, Strength Training"; // Default
        }
    }
    
    /**
     * Lấy khóa học mặc định dựa trên training goals với logic thông minh
     * Ưu tiên training_goals và training_frequency
     */
    private Course getDefaultCourseBytraining_goals(Map<String, Object> userData) {
        Object training_goals = userData.get("training_goals");
        String training_frequency = (String) userData.get("training_frequency");
        
        logger.info("🎯 Getting default course for goals: '{}', frequency: '{}'", training_goals, training_frequency);
        
        if (training_goals != null) {
            String goals = training_goals.toString();
            
            // Thử tìm với key trực tiếp trước
            List<Integer> suggestedCourseIds = TRAINING_GOALS_MAPPING.get(goals);
            
            // Nếu không tìm thấy, thử các key alternative
            if (suggestedCourseIds == null || suggestedCourseIds.isEmpty()) {
                String normalizedGoals = normalizetraining_goals(goals);
                suggestedCourseIds = TRAINING_GOALS_MAPPING.get(normalizedGoals);
                logger.debug("Normalized goals '{}' -> '{}', found {} courses", 
                    goals, normalizedGoals, suggestedCourseIds != null ? suggestedCourseIds.size() : 0);
            }
            
            if (suggestedCourseIds != null && !suggestedCourseIds.isEmpty()) {
                logger.info("🎯 Found {} courses for training goals: {}", suggestedCourseIds.size(), goals);
                
                // Tìm khóa học phù hợp dựa trên tất cả thuộc tính, ưu tiên training_frequency
                Course selectedCourse = selectBestCourseFromSuggestions(suggestedCourseIds, userData);
                if (selectedCourse != null) {
                    logger.info("🎯 Selected smart course: {} - {} (goals: {}, frequency: {})", 
                        selectedCourse.getCourseId(), selectedCourse.getCourseName(), goals, training_frequency);
                    return selectedCourse;
                }
            } else {
                logger.warn("⚠️ No courses found for training goals: {}", goals);
            }
        }
        
        // Fallback logic với training_frequency nếu không có training_goals
        if (training_frequency != null) {
            logger.info("🔄 Using training_frequency fallback: {}", training_frequency);
                         Course frequencyBasedCourse = getCourseBasedOnFrequency(training_frequency, userData);
            if (frequencyBasedCourse != null) {
                logger.info("🎯 Selected frequency-based course: {} - {}", 
                    frequencyBasedCourse.getCourseId(), frequencyBasedCourse.getCourseName());
                return frequencyBasedCourse;
            }
        }
        
        // Fallback cuối cùng - course ID 1 với thông báo rõ ràng
        Course defaultCourse = findCourseById(1);
        if (defaultCourse != null) {
            logger.warn("⚠️ Using ultimate fallback course (ID=1): {} - This should rarely happen!", 
                defaultCourse.getCourseName());
            return defaultCourse;
        }
        
        logger.error("❌ No default course available in system!");
        return null;
    }
    
    /**
     * Chuẩn hóa training goals để mapping chính xác
     */
    private String normalizetraining_goals(String training_goals) {
        if (training_goals == null) return "";
        
        String goals = training_goals.trim();
        String lowerGoals = goals.toLowerCase();
        
        // Thử mapping trực tiếp trước
        if (TRAINING_GOALS_MAPPING.containsKey(goals)) {
            return goals;
        }
        
        // Map từ các format khác nhau
        if (lowerGoals.contains("strength") || lowerGoals.contains("1")) {
            return "1, Strength Training";
        } else if (lowerGoals.contains("cardiovascular") || lowerGoals.contains("cardio") || lowerGoals.contains("2")) {
            return "2, Cardiovascular Training";
        } else if (lowerGoals.contains("flexibility") || lowerGoals.contains("balance") || lowerGoals.contains("3")) {
            return "3, Flexibility and Balance";
        } else if (lowerGoals.contains("endurance") || lowerGoals.contains("4")) {
            return "4, Train strength and endurance";
        } else if (lowerGoals.contains("mind") || lowerGoals.contains("wellness") || lowerGoals.contains("5")) {
            return "5, Mind-Body Wellness";
        } else if (lowerGoals.contains("functional") || lowerGoals.contains("6")) {
            return "6, Functional Training";
        } else if (lowerGoals.contains("weight") || lowerGoals.contains("loss") || lowerGoals.contains("7")) {
            return "7, Weight Loss";
        }
        
        // Default fallback to Weight Loss cho safe
        logger.warn("Cannot normalize training goals '{}', using Weight Loss as fallback", training_goals);
        return "7, Weight Loss";
    }
    
    /**
     * Lấy course dựa trên training_frequency khi không có training_goals
     */
    private Course getCourseBasedOnFrequency(String training_frequency, Map<String, Object> userData) {
        List<Integer> frequencyBasedCourses = new ArrayList<>();
        
        switch (training_frequency) {
            case "Never":
            case "Occasionally":
                // Người mới: đề xuất các khóa học cơ bản, weight loss
                frequencyBasedCourses.addAll(Arrays.asList(1, 9, 17, 67, 68, 69)); // Beginner + Weight Loss
                break;
            case "Moderate":
                // Vừa phải: đề xuất mix các loại
                frequencyBasedCourses.addAll(Arrays.asList(18, 26, 34, 42, 50, 70)); // Intermediate mix
                break;
            case "Regularly":
            case "Advanced":
                // Nâng cao: đề xuất các khóa học thách thức
                frequencyBasedCourses.addAll(Arrays.asList(27, 35, 43, 51, 59, 71)); // Advanced courses
                break;
        }
        
        return selectBestCourseFromSuggestions(frequencyBasedCourses, userData);
    }
    
    /**
     * Chọn khóa học tốt nhất từ danh sách gợi ý dựa trên profile người dùng
     * Ưu tiên training_frequency như thuộc tính quan trọng thứ 2
     */
    private Course selectBestCourseFromSuggestions(List<Integer> courseIds, Map<String, Object> userData) {
        String trainingHistory = (String) userData.get("training_history");
        String training_frequency = (String) userData.get("training_frequency");
        String bodyType = (String) userData.get("body_type");
        String training_goals = (String) userData.get("training_goals");
        
        logger.debug("🎯 Selecting course from {} options based on profile: frequency={}, history={}, bodyType={}", 
            courseIds.size(), training_frequency, trainingHistory, bodyType);
        
        // BƯỚC 1: Lọc courses dựa trên training_frequency (thuộc tính quan trọng thứ 2)
        List<Integer> filteredByFrequency = filterCoursesBytraining_frequency(courseIds, training_frequency);
        
        // BƯỚC 2: Từ danh sách đã lọc, chọn course phù hợp nhất với profile
        List<Integer> targetCourseIds = filteredByFrequency.isEmpty() ? courseIds : filteredByFrequency;
        
        for (Integer courseId : targetCourseIds) {
            Course course = findCourseById(courseId);
            if (course != null) {
                // Logic chọn course dựa trên profile tổng hợp
                if (isCourseSuitableForProfile(course, trainingHistory, bodyType, training_frequency)) {
                    logger.info("🎯 Selected course {} - {} based on comprehensive profile matching", 
                        course.getCourseId(), course.getCourseName());
                    return course;
                }
            }
        }
        
        // Nếu không tìm thấy course phù hợp hoàn toàn, ưu tiên courses phù hợp với frequency
        if (!filteredByFrequency.isEmpty()) {
            Course course = findCourseById(filteredByFrequency.get(0));
            if (course != null) {
                logger.info("🎯 Selected course {} - {} based on training frequency priority", 
                    course.getCourseId(), course.getCourseName());
                return course;
            }
        }
        
        // Fallback: Trả về course đầu tiên có sẵn
        for (Integer courseId : courseIds) {
            Course course = findCourseById(courseId);
            if (course != null) {
                logger.warn("⚠️ Using fallback course {} - {} (no perfect profile match)", 
                    course.getCourseId(), course.getCourseName());
                return course;
            }
        }
        
        return null;
    }
    
    /**
     * Lọc courses dựa trên training_frequency (thuộc tính quan trọng thứ 2)
     */
    private List<Integer> filterCoursesBytraining_frequency(List<Integer> courseIds, String training_frequency) {
        if (training_frequency == null) {
            return courseIds;
        }
        
        List<Integer> filtered = new ArrayList<>();
        
        // Mapping frequency với độ khó khóa học
        switch (training_frequency) {
            case "Never":
            case "Occasionally":
                // Người mới hoặc ít tập: chọn courses cơ bản (ID thấp hơn hoặc specific beginner courses)
                filtered = courseIds.stream()
                    .filter(id -> id <= 25 || isBeginnerCourse(id))
                    .collect(Collectors.toList());
                break;
                
            case "Moderate":
                // Tập vừa phải: chọn courses trung bình
                filtered = courseIds.stream()
                    .filter(id -> (id > 15 && id <= 50) || isIntermediateCourse(id))
                    .collect(Collectors.toList());
                break;
                
            case "Regularly":
            case "Advanced":
                // Tập thường xuyên/nâng cao: chọn courses khó hơn
                filtered = courseIds.stream()
                    .filter(id -> id > 30 || isAdvancedCourse(id))
                    .collect(Collectors.toList());
                break;
        }
        
        if (!filtered.isEmpty()) {
            logger.debug("🎯 Filtered {} courses to {} based on training frequency: {}", 
                courseIds.size(), filtered.size(), training_frequency);
        }
        
        return filtered.isEmpty() ? courseIds : filtered;
    }
    
    /**
     * Kiểm tra course có phù hợp cho người mới không
     */
    private boolean isBeginnerCourse(Integer courseId) {
        // Courses ID thấp hoặc trong danh sách beginner courses
        return courseId <= 20 || Arrays.asList(1, 9, 17, 25, 33, 41, 49, 57).contains(courseId);
    }
    
    /**
     * Kiểm tra course có phù hợp cho intermediate không  
     */
    private boolean isIntermediateCourse(Integer courseId) {
        return (courseId > 15 && courseId <= 45) || Arrays.asList(18, 26, 34, 42, 50, 58).contains(courseId);
    }
    
    /**
     * Kiểm tra course có phù hợp cho advanced không
     */
    private boolean isAdvancedCourse(Integer courseId) {
        return courseId > 35 || Arrays.asList(19, 27, 35, 43, 51, 59).contains(courseId);
    }
    
    /**
     * Kiểm tra xem khóa học có phù hợp với profile người dùng không
     * Tích hợp training_frequency như thuộc tính quan trọng thứ 2
     */
    private boolean isCourseSuitableForProfile(Course course, String trainingHistory, String bodyType, String training_frequency) {
        if (course == null) return false;
        
        Integer courseId = course.getCourseId();
        
        // LOGIC 1: Kiểm tra theo training_frequency (thuộc tính quan trọng thứ 2)
        if (training_frequency != null) {
            switch (training_frequency) {
                case "Never":
                case "Occasionally":
                    // Người mới/ít tập nên chọn courses cơ bản
                    if (courseId > 30 && !isBeginnerCourse(courseId)) {
                        logger.debug("Course {} too advanced for frequency: {}", courseId, training_frequency);
                        return false;
                    }
                    break;
                case "Advanced":
                    // Người advanced có thể cần courses thách thức hơn
                    if (courseId <= 15 && !isAdvancedCourse(courseId)) {
                        logger.debug("Course {} too basic for frequency: {}", courseId, training_frequency);
                        return false;
                    }
                    break;
            }
        }
        
        // LOGIC 2: Kiểm tra theo training_history
        if (trainingHistory != null) {
            switch (trainingHistory) {
                case "Never":
                    // Người chưa bao giờ tập cần khóa học rất cơ bản
                    if (courseId > 25) {
                        logger.debug("Course {} too advanced for training history: Never", courseId);
                        return false;
                    }
                    break;
                case "Advanced":
                case "Regularly":
                    // Người có kinh nghiệm có thể handle courses khó hơn
                    // Không giới hạn quá nghiêm ngặt
                    break;
            }
        }
        
        // LOGIC 3: Kiểm tra theo body_type
        if (bodyType != null) {
            switch (bodyType) {
                case "Thin":
                    // Người gầy nên tập trung vào strength training và tăng cơ
                    String courseName = course.getCourseName();
                    if (courseName != null && courseName.toLowerCase().contains("weight loss")) {
                        logger.debug("Weight loss course {} not suitable for Thin body type", courseId);
                        return false;
        }
                    break;
                case "Overweight":
                    // Người thừa cân ưu tiên cardio và weight loss
                    // Cho phép tất cả courses nhưng ưu tiên trong logic trước đó
                    break;
            }
        }
        
        logger.debug("✅ Course {} suitable for profile: frequency={}, history={}, bodyType={}", 
            courseId, training_frequency, trainingHistory, bodyType);
        return true;
    }
} 