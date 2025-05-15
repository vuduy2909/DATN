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
    private static final String DEFAULT_XML_PATH = "D:/DA-CNPM-FINAL/CODE/kyo-gym-BE/kyo-gym-BE/wellness-navigator-gym/src/main/resources/static/DataTraining.xml";

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
     * Cập nhật để bao gồm các thuộc tính mới từ TrackDataAi
     */
    private List<String> getAttributeNames() {
        return Arrays.asList(
            "activity_level",
            "age",
            "gender", 
            "bmi",
            "training_goals",
            "training_history",
            "height",
            "weight", 
            "body_type",      // Đảm bảo nhất quán với tên trường trong controller
            "occupation",
            "work_environment",
            "health_issues",
            "chest_circumference",
            "waist_circumference"
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
            return null;
        }
        
        try {
            // Log thông tin người dùng để debug
            logger.debug("Traversing decision tree with user data: {}", userData);
            
            while (!currentNode.isLeaf()) {
                String attributeName = currentNode.getAttributeName();
                Object attributeValue = userData.get(attributeName);
                
                logger.debug("Current node attribute: {}, user value: {}", attributeName, attributeValue);

                // Kiểm tra nhanh và chuyển sang nút con tương ứng
                if (attributeValue == null) {
                    logger.warn("Missing attribute value for: {}", attributeName);
                    // Nếu không có giá trị, thử chọn nhánh phổ biến nhất
                    Map<Object, TreeNode> children = currentNode.getChildren();
                    if (children.isEmpty()) {
                        logger.error("Current node has no children and attribute value is missing");
                        return null;
                    }
                    currentNode = findMostCommonChildNode(children);
                    logger.debug("Selected most common child node for missing attribute");
                } else {
                    TreeNode nextNode = currentNode.getChildren().get(attributeValue);
                    if (nextNode == null) {
                        logger.warn("No matching path for attribute {} with value {}", attributeName, attributeValue);
                        // Nếu không có nhánh khớp, chọn nhánh gần nhất có thể
                        nextNode = findClosestMatchingNode(currentNode, attributeValue);
                        logger.debug("Selected closest matching node: {}", nextNode != null ? "found" : "not found");
                    }
                    currentNode = nextNode;
                }
                
                if (currentNode == null) {
                    logger.error("Traversal failed - reached null node");
                    return null;
                }
            }

            // Trả về courseId từ nút lá
            Course recommendedCourse = currentNode.isLeaf() ? currentNode.getCourseDataId() : null;
            logger.info("Decision tree traversal complete. Recommended course: {}", 
                       recommendedCourse != null ? recommendedCourse.getCourseId() : "none");
            return recommendedCourse;
        } catch (Exception e) {
            logger.error("Error during decision tree traversal: {}", e.getMessage(), e);
            return null;
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
        // Đơn giản chọn nút con đầu tiên
        if (!currentNode.getChildren().isEmpty()) {
            return currentNode.getChildren().values().iterator().next();
        }
        return null;
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
