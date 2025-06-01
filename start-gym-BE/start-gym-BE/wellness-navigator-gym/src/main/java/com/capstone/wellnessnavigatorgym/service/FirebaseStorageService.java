package com.capstone.wellnessnavigatorgym.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FirebaseStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);
    // Primary bucket name format
    private static final String BUCKET_NAME = "capstone-project-2909.firebasestorage.app";
    // Alternative bucket name format (as fallback)
    private static final String ALT_BUCKET_NAME = "capstone-project-2909.appspot.com";
    private static final String LOCAL_UPLOAD_PATH = "uploads/";
    
    @Autowired(required = false)
    private StorageClient storageClient;
    
    @PostConstruct
    public void init() {
        if (storageClient != null) {
            try {
                // Test if we can get the bucket
                Bucket bucket = storageClient.bucket();
                logger.info("✅ Firebase Storage initialized successfully with bucket: {}", bucket.getName());
            } catch (IllegalArgumentException e) {
                if (e.getMessage().contains("does not exist")) {
                    // Log more detailed bucket information to help diagnose the issue
                    logger.error("❌ Firebase Storage bucket not found: {}", e.getMessage());
                    logger.info("ℹ️ Make sure your firebase configuration matches one of these formats:");
                    logger.info("   - Primary format: {}", BUCKET_NAME);
                    logger.info("   - Alternative format: {}", ALT_BUCKET_NAME);
                }
                logger.error("❌ Failed to initialize Firebase Storage: {}", e.getMessage());
                logger.error("💡 Stack trace:", e);
                storageClient = null;
            } catch (Exception e) {
                logger.error("❌ Failed to initialize Firebase Storage: {}", e.getMessage());
                logger.error("💡 Stack trace:", e);
                storageClient = null;
            }
        } else {
            logger.warn("❌ StorageClient is null - Firebase Storage will not be available");
        }
        
        // Create local storage directories regardless
        try {
            createLocalDirectories();
        } catch (IOException e) {
            logger.error("❌ Failed to create local storage directories: {}", e.getMessage());
        }
    }
    
    private void createLocalDirectories() throws IOException {
        // Create base directories for local fallback
        String[] paths = {
            LOCAL_UPLOAD_PATH + "avatars",
            LOCAL_UPLOAD_PATH + "courses/1_Strength",
            LOCAL_UPLOAD_PATH + "courses/2_Cardiovascular",
            LOCAL_UPLOAD_PATH + "courses/3_Flexibility",
            LOCAL_UPLOAD_PATH + "exercises/Abs",
            LOCAL_UPLOAD_PATH + "exercises/Back",
            LOCAL_UPLOAD_PATH + "exercises/Legs"
        };
        
        for (String path : paths) {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
                logger.info("📁 Created local storage directory: {}", path);
            }
        }
    }
    
    /**
     * Check if Firebase Storage is available
     * @return true if Firebase Storage is available
     */
    public boolean isAvailable() {
        if (storageClient == null) {
            return false;
        }
        
        try {
            // Try to access the bucket as a test
            storageClient.bucket();
            return true;
        } catch (Exception e) {
            logger.warn("⚠️ Firebase Storage check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Upload avatar to Firebase Storage with local fallback
     * @param file The image file to upload
     * @param userName The username to use in the file path
     * @return The Storage URL of the uploaded file
     */
    public String uploadAvatar(MultipartFile file, String userName) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Get file extension
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            fileExtension = ".jpg"; // Default extension
        }
        
        // Create filename: avatar_username.ext
        String fileName = "avatar_" + userName + fileExtension;
        
        // Try Firebase first
        if (isAvailable()) {
            try {
                // Create file path for Firebase: images/Avatar/filename
                String firebasePath = "images/Avatar/" + fileName;
                
                // Upload to Firebase Storage
                Bucket bucket = storageClient.bucket();
                bucket.create(firebasePath, file.getBytes(), file.getContentType());
                
                // Generate download URL
                String downloadUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                    BUCKET_NAME, firebasePath.replace("/", "%2F"));
                
                logger.info("✅ Avatar uploaded successfully to Firebase Storage: {}", firebasePath);
                return downloadUrl;
            } catch (Exception e) {
                logger.error("❌ Firebase upload failed, falling back to local storage: {}", e.getMessage());
                // Fall through to local storage
            }
        }
        
        // Local storage fallback
        String localDir = LOCAL_UPLOAD_PATH + "avatars/";
        Path localPath = Paths.get(localDir);
        if (!Files.exists(localPath)) {
            Files.createDirectories(localPath);
        }
        
        Path filePath = localPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return local URL
        String localUrl = "/uploads/avatars/" + fileName;
        logger.info("✅ Avatar saved to local storage: {}", filePath.toAbsolutePath());
        
        return localUrl;
    }
    
    /**
     * Upload course image to Firebase Storage with local fallback
     * @param file The image file to upload
     * @param courseType The course type (1_Strength, 2_Cardiovascular, etc.)
     * @param courseName The course name for filename
     * @return The Storage URL of the uploaded file
     */
    public String uploadCourseImage(MultipartFile file, String courseType, String courseName) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Get file extension
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            fileExtension = ".jpg";
        }
        
        // Create safe filename
        String baseFileName = courseName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString().substring(0, 8);
        String fileName = baseFileName + fileExtension;
        
        // Try Firebase first
        if (isAvailable()) {
            try {
                // Create file path for Firebase: images/Course/courseType/filename
                String firebasePath = "images/Course/" + courseType + "/" + fileName;
                
                // Upload to Firebase Storage
                Bucket bucket = storageClient.bucket();
                bucket.create(firebasePath, file.getBytes(), file.getContentType());
                
                // Generate download URL
                String downloadUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                    BUCKET_NAME, firebasePath.replace("/", "%2F"));
                
                logger.info("✅ Course image uploaded to Firebase Storage: {}", firebasePath);
                return downloadUrl;
            } catch (Exception e) {
                logger.error("❌ Firebase upload failed, falling back to local storage: {}", e.getMessage());
                // Fall through to local storage
            }
        }
        
        // Local storage fallback
        String localDir = LOCAL_UPLOAD_PATH + "courses/" + courseType + "/";
        Path localPath = Paths.get(localDir);
        if (!Files.exists(localPath)) {
            Files.createDirectories(localPath);
        }
        
        Path filePath = localPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return local URL
        String localUrl = "/uploads/courses/" + courseType + "/" + fileName;
        logger.info("✅ Course image saved to local storage: {}", filePath.toAbsolutePath());
        
        return localUrl;
    }
    
    /**
     * Upload exercise image to Firebase Storage with local fallback
     * @param file The image file to upload
     * @param muscleGroup The muscle group (Abs, Back, Biceps, etc.)
     * @param exerciseName The exercise name for filename
     * @return The Storage URL of the uploaded file
     */
    public String uploadExerciseImage(MultipartFile file, String muscleGroup, String exerciseName) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Get file extension
        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } else {
            fileExtension = ".jpg";
        }
        
        // Create safe filename
        String baseFileName = exerciseName.replaceAll("[^a-zA-Z0-9]", "_") + "_" + UUID.randomUUID().toString().substring(0, 8);
        String fileName = baseFileName + fileExtension;
        
        // Try Firebase first
        if (isAvailable()) {
            try {
                // Create file path: images/Exercise/muscleGroup/filename
                String firebasePath = "images/Exercise/" + muscleGroup + "/" + fileName;
                
                // Upload to Firebase Storage
                Bucket bucket = storageClient.bucket();
                bucket.create(firebasePath, file.getBytes(), file.getContentType());
                
                // Generate download URL
                String downloadUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                    BUCKET_NAME, firebasePath.replace("/", "%2F"));
                
                logger.info("✅ Exercise image uploaded to Firebase Storage: {}", firebasePath);
                return downloadUrl;
            } catch (Exception e) {
                logger.error("❌ Firebase upload failed, falling back to local storage: {}", e.getMessage());
                // Fall through to local storage
            }
        }
        
        // Local storage fallback
        String localDir = LOCAL_UPLOAD_PATH + "exercises/" + muscleGroup + "/";
        Path localPath = Paths.get(localDir);
        if (!Files.exists(localPath)) {
            Files.createDirectories(localPath);
        }
        
        Path filePath = localPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return local URL
        String localUrl = "/uploads/exercises/" + muscleGroup + "/" + fileName;
        logger.info("✅ Exercise image saved to local storage: {}", filePath.toAbsolutePath());
        
        return localUrl;
    }
    
    /**
     * Delete file from Firebase Storage or local storage
     * @param filePath The path to the file in storage
     */
    public void deleteFile(String filePath) {
        // Check if this is a Firebase URL
        if (filePath != null && filePath.contains("firebasestorage.googleapis.com")) {
            if (isAvailable()) {
                try {
                    // Extract path from Firebase URL
                    String path = filePath.substring(filePath.indexOf("/o/") + 3, filePath.indexOf("?alt=media"));
                    path = path.replace("%2F", "/");
                    
                    // Delete from Firebase
                    Bucket bucket = storageClient.bucket();
                    bucket.get(path).delete();
                    logger.info("✅ File deleted from Firebase Storage: {}", path);
                    return;
                } catch (Exception e) {
                    logger.error("❌ Failed to delete file from Firebase Storage: {}", e.getMessage());
                }
            } else {
                logger.warn("⚠️ Cannot delete Firebase file - Firebase Storage not available");
            }
        } else if (filePath != null && filePath.startsWith("/uploads/")) {
            try {
                // Handle local file deletion
                String localFilePath = filePath.substring(1); // Remove leading slash
                Path path = Paths.get(localFilePath);
                Files.deleteIfExists(path);
                logger.info("✅ File deleted from local storage: {}", path);
            } catch (IOException e) {
                logger.error("❌ Failed to delete file from local storage: {}", e.getMessage());
            }
        }
    }
} 