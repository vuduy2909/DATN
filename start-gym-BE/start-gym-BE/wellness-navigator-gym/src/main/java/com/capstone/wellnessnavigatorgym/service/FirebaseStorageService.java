package com.capstone.wellnessnavigatorgym.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FirebaseStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);
    private static final String LOCAL_UPLOAD_PATH = "uploads/";
    
    @PostConstruct
    public void init() {
        logger.info("📁 FirebaseStorageService initialized with local storage only");
        logger.warn("⚠️ Firebase Storage disabled due to dependency conflicts");
        
        // Create base upload directory for local storage
        try {
            Path basePath = Paths.get(LOCAL_UPLOAD_PATH);
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                logger.info("📁 Created base upload directory: {}", LOCAL_UPLOAD_PATH);
            }
        } catch (IOException e) {
            logger.error("❌ Failed to create base upload directory: {}", e.getMessage());
        }
    }
    
    public boolean isAvailable() {
        // Firebase Storage is not available, using local storage only
        return false;
    }
    
    public String uploadAvatar(MultipartFile file, String userName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            fileExtension = ".jpg";
        }
        
        String fileName = "avatar_" + userName + fileExtension;
        
        // Local storage only
        String localDir = LOCAL_UPLOAD_PATH + "avatars/";
        Path localPath = Paths.get(localDir);
        if (!Files.exists(localPath)) {
            Files.createDirectories(localPath);
        }
        
        Path filePath = localPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        String localUrl = "/uploads/avatars/" + fileName;
        logger.info("✅ Avatar saved to local storage: {}", filePath.toAbsolutePath());
        
        return localUrl;
    }
    
    public String uploadCourseImage(MultipartFile file, String courseType, String courseName) throws IOException {
        return uploadCourseImage(file, courseType, courseName, null);
    }
    
    public String uploadCourseImage(MultipartFile file, String courseType, String courseName, String oldImageUrl) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
            try {
                deleteFile(oldImageUrl);
                logger.info("✅ Deleted old course image: {}", oldImageUrl);
            } catch (Exception e) {
                logger.warn("⚠️ Failed to delete old course image: {}, error: {}", oldImageUrl, e.getMessage());
            }
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            fileExtension = ".jpg";
        }
        
        String sanitizedName = courseName.replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim()
            .replaceAll("\\s+", "_");
            
        String fileName = sanitizedName + fileExtension;
        
        // Local storage only
        String localDir = LOCAL_UPLOAD_PATH + "courses/" + courseType + "/";
        Path localPath = Paths.get(localDir);
        if (!Files.exists(localPath)) {
            Files.createDirectories(localPath);
        }
        
        Path filePath = localPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        String localUrl = "/uploads/courses/" + courseType + "/" + fileName;
        logger.info("✅ Course image saved to local storage: {}", filePath.toAbsolutePath());
        
        return localUrl;
    }
    
    public String uploadExerciseImage(MultipartFile file, String muscleGroup, String exerciseName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            fileExtension = ".jpg";
        }
        
        String baseFileName = exerciseName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString().substring(0, 8);
        String fileName = baseFileName + fileExtension;
        
        // Local storage only
        String localDir = LOCAL_UPLOAD_PATH + "exercises/" + muscleGroup + "/";
        Path localPath = Paths.get(localDir);
        if (!Files.exists(localPath)) {
            Files.createDirectories(localPath);
        }
        
        Path filePath = localPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        String localUrl = "/uploads/exercises/" + muscleGroup + "/" + fileName;
        logger.info("✅ Exercise image saved to local storage: {}", filePath.toAbsolutePath());
        
        return localUrl;
    }
    
    public void deleteFile(String filePath) {
        // Local file deletion only
        try {
            if (filePath.startsWith("/uploads/")) {
                Path path = Paths.get(filePath.substring(1)); // Remove leading slash
                if (Files.exists(path)) {
                    Files.delete(path);
                    logger.info("✅ File deleted from local storage: {}", path);
                }
            }
        } catch (Exception e) {
            logger.error("❌ Failed to delete local file: {}", e.getMessage());
        }
    }
} 