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
    
    // Mapping training goals với các khóa học phù hợp
    private static final Map<String, List<Integer>> TRAINING_GOALS_MAPPING = new HashMap<String, List<Integer>>() {{
        put("1, Strength Training", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 15, 16, 17, 18, 25, 26, 27, 28));
        put("2, Cardiovascular Training", Arrays.asList(9, 10, 11, 12, 13, 14, 19, 20, 21, 22, 29, 30, 31, 32));
        put("3, Flexibility and Balance", Arrays.asList(33, 34, 35, 36, 37, 38, 39, 40, 47, 48, 49, 50));
        put("4, Train strength and endurance", Arrays.asList(23, 24, 25, 26, 27, 28, 41, 42, 43, 44, 51, 52));
        put("5, Mind-Body Wellness", Arrays.asList(45, 46, 47, 48, 49, 50, 57, 58, 59, 60));
        put("6, Functional Training", Arrays.asList(53, 54, 55, 56, 61, 62, 63, 64, 65, 66));
        put("7, Weight Loss", Arrays.asList(67, 68, 69, 70, 71, 72, 73, 29, 30, 31, 32, 9, 10, 11, 12));
    }};
    
    // Threshold minimum cho entropy để tạo leaf node
    private static final double MIN_ENTROPY_THRESHOLD = 0.1;
    private static final int MIN_SAMPLES_FOR_SPLIT = 10;
    
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
     * Xây dựng cây quyết định C4.5 được cải thiện
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
            return getDefaultCourseByTrainingGoals(userData);
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
        return getDefaultCourseByTrainingGoals(userData);
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
        Object trainingGoals = userData.get("training_goals");
        if (trainingGoals != null) {
            String goals = trainingGoals.toString();
            // Nếu chỉ có số, thêm mô tả
            if (goals.matches("\\d+")) {
                userData.put("training_goals", mapNumberToTrainingGoal(goals));
            }
        }
        
        // Chuẩn hóa training_frequency với mapping chính xác
        Object trainingFrequency = userData.get("training_frequency");
        if (trainingFrequency != null) {
            String frequency = trainingFrequency.toString().trim();
            // Map các giá trị training_frequency về format chuẩn trong database
            String normalizedFrequency = normalizeTrainingFrequency(frequency);
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
    private String normalizeTrainingFrequency(String frequency) {
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
    private String mapNumberToTrainingGoal(String number) {
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
     */
    private Course getDefaultCourseByTrainingGoals(Map<String, Object> userData) {
        Object trainingGoals = userData.get("training_goals");
        
        if (trainingGoals != null) {
            String goals = trainingGoals.toString();
            List<Integer> suggestedCourseIds = TRAINING_GOALS_MAPPING.get(goals);
            
            if (suggestedCourseIds != null && !suggestedCourseIds.isEmpty()) {
                // Tìm khóa học phù hợp dựa trên body_type và experience level
                Course selectedCourse = selectBestCourseFromSuggestions(suggestedCourseIds, userData);
                if (selectedCourse != null) {
                    logger.info("🎯 Selected smart default course: {} - {} based on training goals and profile", 
                        selectedCourse.getCourseId(), selectedCourse.getCourseName());
                    return selectedCourse;
                }
            }
        }
        
        // Fallback cuối cùng - course ID 1 với thông báo rõ ràng
        Course defaultCourse = findCourseById(1);
        if (defaultCourse != null) {
            logger.warn("⚠️  Using ultimate fallback default course (ID=1): {} - This should rarely happen!", 
                defaultCourse.getCourseName());
            return defaultCourse;
        }
        
        logger.error("❌ No default course available in system!");
        return null;
    }
    
    /**
     * Chọn khóa học tốt nhất từ danh sách gợi ý dựa trên profile người dùng
     */
    private Course selectBestCourseFromSuggestions(List<Integer> courseIds, Map<String, Object> userData) {
        // Ưu tiên dựa trên kinh nghiệm tập luyện
        String trainingHistory = (String) userData.get("training_history");
        String bodyType = (String) userData.get("body_type");
        
        for (Integer courseId : courseIds) {
            Course course = findCourseById(courseId);
            if (course != null) {
                // Logic chọn course dựa trên profile
                if (isCourseSuitableForProfile(course, trainingHistory, bodyType)) {
                    return course;
                }
            }
        }
        
        // Nếu không tìm thấy course phù hợp, trả về course đầu tiên có sẵn
        for (Integer courseId : courseIds) {
            Course course = findCourseById(courseId);
            if (course != null) {
                return course;
            }
        }
        
        return null;
    }
    
    /**
     * Kiểm tra xem khóa học có phù hợp với profile người dùng không
     */
    private boolean isCourseSuitableForProfile(Course course, String trainingHistory, String bodyType) {
        // Logic đơn giản để đánh giá tính phù hợp
        // Có thể mở rộng thêm logic phức tạp hơn
        
        if ("Never".equals(trainingHistory)) {
            // Người mới bắt đầu nên chọn khóa học cơ bản (ID thấp hơn)
            return course.getCourseId() <= 20;
        } else if ("Advanced".equals(trainingHistory) || "Regularly".equals(trainingHistory)) {
            // Người có kinh nghiệm có thể chọn khóa học nâng cao
            return course.getCourseId() > 20;
        }
        
        return true; // Chấp nhận tất cả nếu không có tiêu chí cụ thể
    }
} 