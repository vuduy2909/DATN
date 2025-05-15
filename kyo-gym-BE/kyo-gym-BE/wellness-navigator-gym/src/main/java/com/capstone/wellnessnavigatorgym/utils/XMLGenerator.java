package com.capstone.wellnessnavigatorgym.utils;

import com.capstone.wellnessnavigatorgym.dto.tree.TreeNode;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.TrackDataAi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tiện ích tạo và xử lý file XML cho cây quyết định
 */
@Component
public class XMLGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(XMLGenerator.class);
    
    private static final String DEFAULT_XML_PATH = "D:/DA-CNPM-FINAL/CODE/kyo-gym-BE/kyo-gym-BE/wellness-navigator-gym/src/main/resources/static/DataTraining.xml";
    
    /**
     * Tạo file XML từ dữ liệu huấn luyện
     * @param trackDataList danh sách dữ liệu huấn luyện
     * @param filePath đường dẫn file XML
     * @return true nếu tạo thành công, false nếu thất bại
     */
    public static boolean generateEnhancedXML(List<TrackDataAi> trackDataList, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            filePath = DEFAULT_XML_PATH;
        }
        
        if (trackDataList == null || trackDataList.isEmpty()) {
            logger.error("No training data available to build decision tree");
            return false;
        }
        
        try {
            // Xây dựng cây quyết định
            logger.info("Building decision tree from {} training records", trackDataList.size());
            BuildDecisionTree builder = new BuildDecisionTree(trackDataList);
            
            // Lấy course từ dữ liệu
            List<Course> courses = trackDataList.stream()
                    .filter(data -> data.getCourse() != null)
                    .map(TrackDataAi::getCourse)
                    .distinct()
                    .collect(Collectors.toList());
            
            logger.info("Found {} distinct courses in training data", courses.size());
            builder.setCourses(courses);
            
            // Xây dựng cây
            TreeNode root = builder.buildDecisionTree();
            if (root == null) {
                logger.error("Failed to build decision tree");
                return false;
            }
            
            // Xuất ra file XML
            exportTreeToXml(root, filePath);
            logger.info("Successfully generated enhanced XML file at: {}", filePath);
            return true;
            
        } catch (Exception e) {
            logger.error("Error generating enhanced XML: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Xuất cây quyết định ra file XML
     */
    public static void exportTreeToXml(TreeNode root, String filePath) throws JAXBException {
        File file = new File(filePath);
        File directory = file.getParentFile();
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (!created) {
                logger.warn("Could not create directory: {}", directory.getAbsolutePath());
            }
        }
        
        // Tạo JAXBContext và Marshaller
        JAXBContext context = JAXBContext.newInstance(TreeNode.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        
        // Xuất cây ra file
        marshaller.marshal(root, file);
        logger.info("Decision tree exported successfully to: {}", filePath);
    }
    
    /**
     * Kiểm tra và sửa lỗi file XML
     * @param filePath đường dẫn file XML cần kiểm tra
     * @return true nếu file hợp lệ hoặc đã sửa thành công, false nếu không sửa được
     */
    public static boolean validateAndFixXML(String filePath) {
        File file = new File(filePath);
        
        if (!file.exists()) {
            logger.warn("XML file does not exist: {}", filePath);
            // Thử tìm bất kỳ file XML nào trong thư mục
            File directory = file.getParentFile();
            if (directory.exists() && directory.isDirectory()) {
                File[] xmlFiles = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xml"));
                if (xmlFiles != null && xmlFiles.length > 0) {
                    // Tìm thấy file XML, kiểm tra và sử dụng
                    try {
                        JAXBContext context = JAXBContext.newInstance(TreeNode.class);
                        for (File xmlFile : xmlFiles) {
                            try {
                                context.createUnmarshaller().unmarshal(xmlFile);
                                logger.info("Found valid XML file: {}", xmlFile.getAbsolutePath());
                                // Sao chép file này sang vị trí mong muốn
                                if (!xmlFile.getAbsolutePath().equals(filePath)) {
                                    logger.info("Copying {} to {}", xmlFile.getAbsolutePath(), filePath);
                                    java.nio.file.Files.copy(
                                            xmlFile.toPath(),
                                            new File(filePath).toPath(),
                                            java.nio.file.StandardCopyOption.REPLACE_EXISTING
                                    );
                                }
                                return true;
                            } catch (Exception e) {
                                logger.warn("Invalid XML file: {}", xmlFile.getAbsolutePath());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error validating XML files: {}", e.getMessage(), e);
                    }
                }
            }
            return false;
        }
        
        // Kiểm tra file có đúng định dạng XML không
        try {
            JAXBContext context = JAXBContext.newInstance(TreeNode.class);
            context.createUnmarshaller().unmarshal(file);
            logger.info("XML file is valid: {}", filePath);
            return true;
        } catch (Exception e) {
            logger.error("Invalid XML file: {}. Error: {}", filePath, e.getMessage());
            return false;
        }
    }
} 