package com.capstone.wellnessnavigatorgym.utils;

import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BuildDecisionTree {

    private static final Logger logger = LoggerFactory.getLogger(BuildDecisionTree.class);
    private DecisionTree decisionTree;
    private List<Course> courses; // Danh sách này có thể được nạp từ cơ sở dữ liệu
    
    // Đường dẫn XML mặc định để đảm bảo nhất quán
    private static final String DEFAULT_XML_PATH = "D:/DA-CNPM-FINAL/CODE/start-gym-BE/start-gym-BE/wellness-navigator-gym/src/main/resources/static/DataTraining.xml";

    public BuildDecisionTree() {
    }

    public BuildDecisionTree(List<TrackDataAi> trackDataAis) {
        this.decisionTree = new DecisionTree(trackDataAis);
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public TreeNode buildDecisionTree() {
        List<TrackDataAi> data = decisionTree.getTrackDataAiData();
        if (data.isEmpty()) {
            logger.warn("No training data available to build decision tree");
            return null;
        }
        
        logger.info("Building decision tree with {} data points", data.size());
        TreeNode root = buildDecisionTreeRecursive(data, getAttributeNames());
        logger.info("Decision tree built successfully");
        return root;
    }

    private TreeNode buildDecisionTreeRecursive(List<TrackDataAi> data, List<String> attributeNames) {
        if (data.isEmpty()) {
            logger.debug("Empty dataset encountered during tree building");
            return new TreeNode(null); // Xử lý trường hợp tập dữ liệu rỗng
        }

        if (attributeNames.isEmpty()) {
            logger.debug("No more attributes to split on, creating leaf node");
            return createLeafNodeBasedOnMajority(data); // Tạo nút lá dựa trên đa số nếu hết thuộc tính
        }

        // Tìm thuộc tính tốt nhất bằng cách sử dụng Gain Ratio
        List<String> sortedAttributes = findBestAttributeUsingGainRatio(data, attributeNames);
        if (sortedAttributes.isEmpty()) {
            logger.debug("No suitable attributes found, creating leaf node");
            return createLeafNodeBasedOnMajority(data); // Tạo nút lá nếu không có thuộc tính nào tốt để chọn
        }

        String bestAttribute = sortedAttributes.get(0); // Lấy thuộc tính tốt nhất từ danh sách đã sắp xếp
        logger.debug("Best attribute selected: {}", bestAttribute);
        
        TreeNode node = new TreeNode(bestAttribute);
        double gainRatio = decisionTree.calculateGainRatio(bestAttribute);
        node.setGainRatio(gainRatio);
        logger.debug("Gain ratio for attribute {}: {}", bestAttribute, gainRatio);

        Map<Object, List<TrackDataAi>> subsets = decisionTree.getSubsetsByAttributeValue(bestAttribute);
        subsets.forEach((attributeValue, subset) -> {
            TreeNode childNode;
            if (subset.isEmpty()) {
                childNode = createLeafNodeBasedOnMajority(data); // Tạo nút lá nếu tập con rỗng
            } else {
                List<String> remainingAttributes = new ArrayList<>(attributeNames);
                remainingAttributes.remove(bestAttribute); // Loại bỏ thuộc tính đã sử dụng khỏi danh sách
                childNode = buildDecisionTreeRecursive(subset, remainingAttributes); // Gọi đệ quy với tập con và thuộc tính còn lại
            }
            node.getChildren().put(attributeValue, childNode);
        });

        return node;
    }

    /**
     * Phương thức này trả về danh sách tất cả các thuộc tính được sử dụng để xây dựng cây quyết định
     * Loại bỏ waist_circumference và chest_circumference vì chỉ dùng để đánh giá hiệu quả sau tập
     */
    private List<String> getAttributeNames() {
        return Arrays.asList(
            "age",
            "gender", 
            "bmi",
            "training_goals",
            "training_history",
            "training_frequency",
            "height",
            "weight",
            "body_type",
            "occupation",
            "work_environment"
        );
    }

    private TreeNode createLeafNodeBasedOnMajority(List<TrackDataAi> data) {
        if (data.isEmpty()) {
            logger.warn("Empty data set for leaf node creation");
            return new TreeNode(null); // Xử lý tập dữ liệu rỗng
        }

        // Tìm CourseID phổ biến nhất trong dữ liệu
        Map<Integer, Long> courseIdCounts = data.stream()
                .filter(d -> d.getCourse() != null)
                .collect(Collectors.groupingBy(d -> d.getCourse().getCourseId(), Collectors.counting()));
        
        if (courseIdCounts.isEmpty()) {
            logger.warn("No course IDs found in data for majority calculation");
            return new TreeNode(null);
        }

        Integer majorityCourseId = courseIdCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Tạo nút lá với CourseID phổ biến nhất
        TreeNode leafNode = new TreeNode(null);
        leafNode.setLeaf(true);
        
        if (majorityCourseId != null) {
            Course majorityCourse = findCourseById(majorityCourseId);
            if (majorityCourse != null) {
                logger.debug("Leaf node created with course ID: {}", majorityCourse.getCourseId());
                leafNode.setCourseDataId(majorityCourse);
            } else {
                logger.warn("Could not find course with ID: {} in courses list", majorityCourseId);
            }
        } else {
            logger.warn("No majority course ID found for leaf node");
        }

        return leafNode;
    }

    public Course findCourseById(Integer courseId) {
        if (courseId == null || courses == null) {
            logger.warn("Course ID is null or courses list is not initialized");
            return null;
        }
        
        return courses.stream()
                .filter(course -> course.getCourseId().equals(courseId))
                .findFirst()
                .map(course -> {
                    // Return a new Course with just the ID to avoid detached entity issues
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

    private List<String> findBestAttributeUsingGainRatio(List<TrackDataAi> data, List<String> attributeNames) {
        Map<String, Double> gainRatios = new HashMap<>();
        
        for (String attribute : attributeNames) {
            if (!TrackDataAi.isValidAttribute(attribute)) {
                logger.debug("Skip invalid attribute: {}", attribute);
                continue;
            }
            try {
                double gainRatio = decisionTree.calculateGainRatio(attribute);
                gainRatios.put(attribute, gainRatio);
                logger.debug("Gain ratio for {}: {}", attribute, gainRatio);
            } catch (Exception e) {
                logger.error("Error calculating gain ratio for attribute {}: {}", attribute, e.getMessage());
            }
        }
        
        return gainRatios.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Xuất cây quyết định ra file XML
     */
    public void exportTreeToXml(TreeNode root, String filePath) {
        // Sử dụng đường dẫn mặc định nếu không cung cấp
        if (filePath == null || filePath.isEmpty()) {
            filePath = DEFAULT_XML_PATH;
            logger.info("Using default XML path: {}", DEFAULT_XML_PATH);
        }
        
        try {
            // Create JAXBContext for TreeNode
            JAXBContext context = JAXBContext.newInstance(TreeNode.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Tạo thư mục chứa nếu chưa tồn tại
            File file = new File(filePath);
            File directory = file.getParentFile();
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    logger.warn("Could not create directory: {}", directory.getAbsolutePath());
                }
            }

            // Serialize the entire tree
            marshaller.marshal(root, file);
            logger.info("Decision tree exported successfully to: {}", filePath);
        } catch (JAXBException e) {
            logger.error("Error exporting decision tree to XML: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to export decision tree: " + e.getMessage(), e);
        }
    }

    /**
     * Nhập cây quyết định từ file XML
     */
    public TreeNode importTreeFromXml(String filePath) {
        // Sử dụng đường dẫn mặc định nếu không cung cấp
        if (filePath == null || filePath.isEmpty()) {
            filePath = DEFAULT_XML_PATH;
            logger.info("Using default XML path: {}", DEFAULT_XML_PATH);
        }
        
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                logger.error("XML file does not exist: {}", filePath);
                
                // Thử tìm file trong thư mục static
                String dir = file.getParent();
                File directory = new File(dir);
                if (directory.exists() && directory.isDirectory()) {
                    logger.info("Searching for XML files in directory: {}", dir);
                    File[] xmlFiles = directory.listFiles((d, name) -> name.toLowerCase().endsWith(".xml"));
                    if (xmlFiles != null && xmlFiles.length > 0) {
                        File alternativeFile = xmlFiles[0];
                        logger.info("Found alternative XML file: {}", alternativeFile.getAbsolutePath());
                        return importTreeFromXml(alternativeFile.getAbsolutePath());
                    }
                }
                
                return null;
            }
            
            JAXBContext context = JAXBContext.newInstance(TreeNode.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Đọc cây từ file XML
            TreeNode root = (TreeNode) unmarshaller.unmarshal(file);
            logger.info("Decision tree imported successfully from: {}", filePath);
            return root;
        } catch (JAXBException e) {
            logger.error("Error importing decision tree from XML: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Duyệt cây quyết định để đề xuất khóa học
     */
    public Course traverseDecisionTree(TreeNode currentNode, Map<String, Object> userData) {
        if (currentNode == null) {
            logger.error("Null node encountered during tree traversal");
            return getDefaultCourse();
        }
        
        try {
            // Apply default values at the beginning of traversal
            addDefaultValuesToUserData(userData);
            
            // Log thông tin người dùng để debug sau khi thêm giá trị mặc định
            logger.debug("Traversing decision tree with user data (after defaults): {}", userData);
            
            while (!currentNode.isLeaf()) {
                String attributeName = currentNode.getAttributeName();
                Object attributeValue = userData.get(attributeName);
                
                logger.debug("Current node attribute: {}, user value: {}", attributeName, attributeValue);

                // Kiểm tra xem nút hiện tại có con không
                Map<Object, TreeNode> children = currentNode.getChildren();
                if (children == null || children.isEmpty()) {
                    logger.warn("Current node for attribute '{}' has no children, treating as leaf node", attributeName);
                    // Xử lý nút không có con như nút lá và trả về khóa học mặc định
                    return getDefaultCourse();
                }

                // Kiểm tra và xử lý nếu giá trị thuộc tính là null
                if (attributeValue == null) {
                    logger.warn("Missing attribute value for: {}", attributeName);
                    currentNode = findMostCommonChildNode(children);
                    logger.debug("Selected most common child node for missing attribute");
                } else {
                    TreeNode nextNode = children.get(attributeValue);
                    if (nextNode == null) {
                        logger.warn("No matching path for attribute {} with value {}", attributeName, attributeValue);
                        
                        // Đặc biệt xử lý cho body_type
                        if (attributeName.equals("body_type")) {
                            logger.info("Special handling for body_type attribute");
                            for (Map.Entry<Object, TreeNode> entry : children.entrySet()) {
                                logger.info("Available body_type option: {}", entry.getKey());
                                // Lấy giá trị đầu tiên có sẵn
                                nextNode = entry.getValue();
                                break;
                            }
                        } else {
                            // Đối với các thuộc tính khác, tìm node gần nhất
                            nextNode = findClosestMatchingNode(currentNode, attributeValue);
                        }
                        
                        logger.debug("Selected closest matching node: {}", nextNode != null ? "found" : "not found");
                        
                        if (nextNode == null && !children.isEmpty()) {
                            // Nếu vẫn không tìm thấy nút khớp, sử dụng nút con đầu tiên
                            nextNode = children.values().iterator().next();
                            logger.debug("Fallback to first child node");
                        }
                    }
                    currentNode = nextNode;
                }
                
                if (currentNode == null) {
                    logger.error("Traversal failed - reached null node");
                    return getDefaultCourse();
                }
            }

            // Trả về courseId từ nút lá
            Course recommendedCourse = currentNode.isLeaf() ? currentNode.getCourseDataId() : null;
            
            // Nếu không tìm thấy khóa học được đề xuất, trả về khóa học mặc định
            if (recommendedCourse == null) {
                recommendedCourse = getDefaultCourse();
                logger.info("No recommendation found in leaf node. Using default course: {}", 
                            recommendedCourse != null ? recommendedCourse.getCourseId() : "none");
            }
            
            logger.info("Decision tree traversal complete. Recommended course: {}", 
                       recommendedCourse != null ? recommendedCourse.getCourseId() : "none");
            return recommendedCourse;
        } catch (Exception e) {
            logger.error("Error during decision tree traversal: {}", e.getMessage(), e);
            return getDefaultCourse();
        }
    }
    
    /**
     * Tìm nút con phổ biến nhất khi không có giá trị thuộc tính
     */
    private TreeNode findMostCommonChildNode(Map<Object, TreeNode> children) {
        // Đơn giản chọn nút con đầu tiên
        return children.values().iterator().next();
    }
    
    /**
     * Tìm nút con gần nhất khi không có nút con chính xác
     */
    private TreeNode findClosestMatchingNode(TreeNode currentNode, Object attributeValue) {
        // Nếu không có giá trị thuộc tính khớp chính xác, hãy thử tìm giá trị gần nhất
        Map<Object, TreeNode> children = currentNode.getChildren();
        if (children.isEmpty()) {
            logger.warn("Node has no children to select from");
            return null;
        }
        
        // Đối với body_type, xử lý đặc biệt để đảm bảo luôn tìm được nhánh phù hợp
        if (currentNode.getAttributeName().equals("body_type")) {
            logger.info("Finding alternative body type match for value: {}", attributeValue);
            
            // Thử tìm các giá trị phổ biến theo thứ tự ưu tiên
            String[] bodyTypes = {"Normal", "Average", "Medium", "Skinny Fat", "Fit", "Athletic", "Overweight", "Thin", "Obese"};
            
            for (String bodyType : bodyTypes) {
                if (children.containsKey(bodyType)) {
                    logger.info("Selected alternative body type: {}", bodyType);
                    return children.get(bodyType);
                }
            }
            
            // Nếu không tìm thấy, trả về nút con đầu tiên
            logger.info("No matching body type found, using first available type");
            return children.values().iterator().next();
        }
        
        // Đối với các thuộc tính khác, chỉ đơn giản trả về nút con đầu tiên
        if (!children.isEmpty()) {
            return children.values().iterator().next();
        }
        return null;
    }
    
    /**
     * Helper method to add default values to missing user data attributes
     */
    private void addDefaultValuesToUserData(Map<String, Object> userData) {
        // Body type default
        if (!userData.containsKey("body_type") || userData.get("body_type") == null) {
            userData.put("body_type", "Normal");
            logger.info("Added default value 'Normal' for body_type attribute");
        }
        

        
        // Training goals default if missing
        if (!userData.containsKey("training_goals") || userData.get("training_goals") == null) {
            userData.put("training_goals", "General Fitness");
            logger.info("Added default value 'General Fitness' for training_goals attribute");
        }
        
        // Other potential defaults
        if (!userData.containsKey("training_history") || userData.get("training_history") == null) {
            userData.put("training_history", "Beginner");
            logger.info("Added default value 'Beginner' for training_history attribute");
        }
        
        if (!userData.containsKey("work_environment") || userData.get("work_environment") == null) {
            userData.put("work_environment", "Sedentary");
            logger.info("Added default value 'Sedentary' for work_environment attribute");
        }
    }
    
    /**
     * Helper method to get a default course
     */
    private Course getDefaultCourse() {
        if (courses != null && !courses.isEmpty()) {
            Course originalCourse = courses.get(0);
            // Create a new Course with just the ID and basic properties to avoid detached entity issues
            Course defaultCourse = new Course(originalCourse.getCourseId());
            defaultCourse.setCourseName(originalCourse.getCourseName());
            defaultCourse.setDescription(originalCourse.getDescription());
            defaultCourse.setDuration(originalCourse.getDuration());
            defaultCourse.setImage(originalCourse.getImage());
            defaultCourse.setStatus(originalCourse.getStatus());
            logger.info("Returning default course: {}", defaultCourse.getCourseId());
            return defaultCourse;
        }
        
        logger.error("No courses available in the system to use as default!");
        // Tạo một khóa học "giả" để tránh trả về null
        Course fallbackCourse = new Course();
        fallbackCourse.setCourseId(1);
        fallbackCourse.setCourseName("Khóa học mặc định cho người mới bắt đầu");
        fallbackCourse.setDescription("Khóa học này được đề xuất khi không tìm thấy khóa học phù hợp khác");
        return fallbackCourse;
    }

    /**
     * Phương thức tiện ích để xây dựng và xuất cây quyết định
     */
    public TreeNode buildAndExportDecisionTree(String filePath) {
        logger.info("Building and exporting decision tree to {}", filePath);
        TreeNode root = buildDecisionTree();
        if (root != null) {
            exportTreeToXml(root, filePath);
        } else {
            logger.error("Failed to build decision tree");
        }
        return root;
    }
}
