package com.capstone.wellnessnavigatorgym.utils;

import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.Exercise;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class BuildDecisionTree {

    private DecisionTree decisionTree;
    private List<Course> courses; // Danh sách này có thể được nạp từ cơ sở dữ liệu

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

        return buildDecisionTreeRecursive(data, getAttributeNames());
    }

    private TreeNode buildDecisionTreeRecursive(List<TrackDataAi> data, List<String> attributeNames) {
        if (data.isEmpty()) {
            return new TreeNode(null); // Xử lý trường hợp tập dữ liệu rỗng
        }

        if (attributeNames.isEmpty()) {
            return createLeafNodeBasedOnMajority(data); // Tạo nút lá dựa trên đa số nếu hết thuộc tính
        }

        List<String> sortedAttributes = findBestAttributeUsingGainRatio(data, attributeNames);
        if (sortedAttributes.isEmpty()) {
            return createLeafNodeBasedOnMajority(data); // Tạo nút lá nếu không có thuộc tính nào tốt để chọn
        }

        String bestAttribute = sortedAttributes.get(0); // Lấy thuộc tính tốt nhất từ danh sách đã sắp xếp
        TreeNode node = new TreeNode(bestAttribute);

        Map<Object, List<TrackDataAi>> subsets = decisionTree.getSubsetsByAttributeValue(bestAttribute);
        subsets.forEach((attributeValue, subset) -> {
            TreeNode childNode;
            if (subset.isEmpty()) {
                childNode = createLeafNodeBasedOnMajority(data); // Tạo nút lá nếu tập con rỗng
            } else {
                List<String> remainingAttributes = new ArrayList<>(sortedAttributes);
                remainingAttributes.remove(bestAttribute); // Loại bỏ thuộc tính đã sử dụng khỏi danh sách
                childNode = buildDecisionTreeRecursive(subset, remainingAttributes); // Gọi đệ quy với tập con và thuộc tính còn lại
            }
            node.getChildren().put(attributeValue, childNode);
        });

        return node;
    }

    //        private boolean allExercisesHaveSameCourseID(List<Exercise> data) {
//            return data.stream().map(exercise -> exercise.getCourse().getCourse_id())
//                    .distinct().limit(2).count() == 1;
//        }
    private List<String> getAttributeNames() {
        return Arrays.asList(
                "activity_level",
                "age",
                "gender",
                "bmi",
                "training_goals",
                "training_history"
        );
    }

    private TreeNode createLeafNodeBasedOnCourseId(List<TrackDataAi> data) {
        if (data.isEmpty()) {
            return new TreeNode(null); // Handle empty data
        }
        Integer majorityCourseId = data.stream()
                .collect(Collectors.groupingBy(exercise -> exercise.getCourse().getCourseId(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        Course majorityCourse = findCourseById(majorityCourseId);
        System.out.println("Majority Course ID: " + majorityCourseId);
        if (majorityCourse != null) {
            // In ra để debug
            System.out.println("Majority Course ID for leaf node: " + majorityCourse.getCourseId());
            TreeNode leafNode = new TreeNode(null);
            leafNode.setLeaf(true);
            leafNode.setCourseDataId(majorityCourse);
            return leafNode;
        } else {
            // In ra để debug
            System.out.println("No majority course ID found for leaf node.");
            return new TreeNode(null); // Có thể bạn cần xử lý khác ở đây
        }
    }

    public Course findCourseById(Integer courseId) {
        if (courseId == null || courses == null) {
            System.out.println("Course ID is null or courses list is not initialized.");
            return null;
        }
        for (Course course : courses) {
            if (course.getCourseId() == courseId) {
                return course;
            }
        }
        return null; // Trả về null nếu không tìm thấy khóa học
    }


    private List<String> findBestAttributeUsingGainRatio(List<TrackDataAi> data, List<String> attributeNames) {
        Map<String, Double> gainRatios = new HashMap<>();
        for (String attribute : attributeNames) {
            double gainRatio = decisionTree.calculateGainRatio(attribute);
            gainRatios.put(attribute, gainRatio);
        }
        return gainRatios.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    private TreeNode createLeafNodeBasedOnMajority(List<TrackDataAi> data) {
        if (data.isEmpty()) {
            return new TreeNode(null); // Xử lý tập dữ liệu rỗng
        }

        // Tìm CourseID phổ biến nhất trong dữ liệu
        Integer majorityCourseId = data.stream()
                .collect(Collectors.groupingBy(exercise -> exercise.getCourse().getCourseId(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // Tạo nút lá với CourseID phổ biến nhất
        TreeNode leafNode = new TreeNode(null);
        leafNode.setLeaf(true);
        if (majorityCourseId != null) {
            Course majorityCourse = findCourseById(majorityCourseId);
            leafNode.setCourseDataId(majorityCourse); // Thiết lập courseId cho nút lá
        }

        return leafNode;
    }

    public void exportTreeToXml(TreeNode root, String filePath) {
        try {
            // Create JAXBContext for TreeNode
            JAXBContext context = JAXBContext.newInstance(TreeNode.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Serialize the entire tree
            marshaller.marshal(root, new File(filePath));
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public TreeNode importTreeFromXml(String filePath) {
        try {
            JAXBContext context = JAXBContext.newInstance(TreeNode.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Đọc cây từ file XML
            TreeNode root = (TreeNode) unmarshaller.unmarshal(new File(filePath));
            return root;
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Course traverseDecisionTree(TreeNode currentNode, Map<String, Object> userData) {
        while (!currentNode.isLeaf()) {
            Object attributeValue = userData.get(currentNode.getAttributeName());

            // Kiểm tra nhanh và chuyển sang nút con tương ứng
            if (attributeValue == null) {
                // In ra lỗi chỉ khi cần thiết cho mục đích debug
                System.out.println("Missing attribute: " + currentNode.getAttributeName());
                return null;
            }

            currentNode = currentNode.getChildren().get(attributeValue);
            if (currentNode == null) {
                // In ra lỗi chỉ khi cần thiết cho mục đích debug
                System.out.println("No path for attribute value: " + attributeValue);
                return null;
            }
        }

        // Trả về courseId từ nút lá
        return currentNode.isLeaf() ? currentNode.getCourseDataId() : null;
    }
}
