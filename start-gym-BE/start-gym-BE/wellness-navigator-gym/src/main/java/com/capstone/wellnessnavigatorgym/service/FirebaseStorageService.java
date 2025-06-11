package com.capstone.wellnessnavigatorgym.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.capstone.wellnessnavigatorgym.config.FirebaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Firebase Storage Service for managing file uploads with specific folder structure
 * 
 * Folder Structure:
 * - images/Avatar/ - User profile avatars (format: avatar_{username}.ext)
 * - images/Course/{courseType}/ - Course images by category
 * - images/Exercise/{muscleGroup}/ - Exercise images by muscle group
 * - videos/Exercise/{muscleGroup}/ - Exercise videos by muscle group
 */
@Service
public class FirebaseStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);
    
    @Autowired
    private Storage storage;
    
    private String bucketName;
    
    @PostConstruct
    public void init() {
        this.bucketName = FirebaseConfig.getBucketName();
        logger.info("🔥 FirebaseStorageService initialized with bucket: {}", bucketName);
        
        // Test Firebase connectivity
        try {
            if (storage != null) {
                logger.info("✅ Firebase Storage connection established successfully");
            } else {
                logger.error("❌ Firebase Storage is null - check configuration");
            }
        } catch (Exception e) {
            logger.error("❌ Firebase Storage connection test failed: {}", e.getMessage());
        }
    }
    
    public boolean isAvailable() {
        return storage != null;
    }
    
    /**
     * Upload user avatar with naming format: avatar_{username}.ext
     * Stored in: images/Avatar/
     */
    public String uploadAvatar(MultipartFile file, String userName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!isAvailable()) {
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        
        // Format: avatar_{username}.ext
        String fileName = "avatar_" + userName + fileExtension;
        String filePath = "images/Avatar/" + fileName;
        
        // Delete existing avatar if exists
        deleteExistingFile(filePath);
        
        // Upload new avatar
        String downloadUrl = uploadFileToFirebase(file, filePath);
        logger.info("✅ Avatar uploaded successfully for user: {} -> {}", userName, downloadUrl);
        
        return downloadUrl;
    }
    
    /**
     * Upload course image to categorized folders
     * Stored in: images/Course/{courseType}/
     */
    public String uploadCourseImage(MultipartFile file, String courseType, String courseName) throws IOException {
        return uploadCourseImage(file, courseType, courseName, null);
    }
    
    public String uploadCourseImage(MultipartFile file, String courseType, String courseName, String oldImageUrl) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!isAvailable()) {
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        // Delete old image if provided
        if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
            try {
                deleteFileByUrl(oldImageUrl);
                logger.info("✅ Deleted old course image: {}", oldImageUrl);
            } catch (Exception e) {
                logger.warn("⚠️ Failed to delete old course image: {}, error: {}", oldImageUrl, e.getMessage());
            }
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        
        // Sanitize course name for filename
        String sanitizedName = courseName.replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim()
            .replaceAll("\\s+", "_")
            .toLowerCase();
            
        String fileName = sanitizedName + fileExtension;
        String filePath = "images/Course/" + courseType + "/" + fileName;
        
        String downloadUrl = uploadFileToFirebase(file, filePath);
        logger.info("✅ Course image uploaded successfully: {} -> {}", filePath, downloadUrl);
        
        return downloadUrl;
    }
    
    /**
     * Upload exercise image
     * Stored in: images/Exercise/{muscleGroup}/
     */
    public String uploadExerciseImage(MultipartFile file, String muscleGroup, String exerciseName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!isAvailable()) {
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        
        // Sanitize exercise name for filename
        String sanitizedName = exerciseName.replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim()
            .replaceAll("\\s+", "_")
            .toLowerCase();
            
        String fileName = sanitizedName + fileExtension;
        String filePath = "images/Exercise/" + muscleGroup + "/" + fileName;
        
        String downloadUrl = uploadFileToFirebase(file, filePath);
        logger.info("✅ Exercise image uploaded successfully: {} -> {}", filePath, downloadUrl);
        
        return downloadUrl;
    }
    
    /**
     * Upload exercise video
     * Stored in: videos/Exercise/{muscleGroup}/
     */
    public String uploadExerciseVideo(MultipartFile file, String muscleGroup, String exerciseName) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!isAvailable()) {
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        
        // Sanitize exercise name for filename
        String sanitizedName = exerciseName.replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim()
            .replaceAll("\\s+", "_")
            .toLowerCase();
            
        String fileName = sanitizedName + fileExtension;
        String filePath = "videos/Exercise/" + muscleGroup + "/" + fileName;
        
        String downloadUrl = uploadFileToFirebase(file, filePath);
        logger.info("✅ Exercise video uploaded successfully: {} -> {}", filePath, downloadUrl);
        
        return downloadUrl;
    }
    
    /**
     * Core method to upload file to Firebase Storage
     */
    private String uploadFileToFirebase(MultipartFile file, String filePath) throws IOException {
        try {
            logger.info("🔄 Starting Firebase upload - File: {}, Path: {}, Bucket: {}", 
                file.getOriginalFilename(), filePath, bucketName);
            
            // First check if bucket exists
            try {
                storage.get(bucketName);
                logger.info("✅ Bucket {} verified to exist", bucketName);
            } catch (Exception e) {
                logger.error("❌ Bucket {} does not exist or is not accessible: {}", bucketName, e.getMessage());
                
                // Try to list available buckets for debugging
                try {
                    logger.info("🔍 Attempting to list available buckets...");
                    storage.list().iterateAll().forEach(bucket -> {
                        logger.info("📁 Available bucket: {}", bucket.getName());
                    });
                } catch (Exception listEx) {
                    logger.warn("Could not list buckets: {}", listEx.getMessage());
                }
                
                throw new IOException("Firebase Storage bucket not accessible: " + e.getMessage());
            }
            
            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .setCacheControl("public, max-age=31536000") // 1 year cache
                .build();
            
            logger.info("📤 Uploading {} bytes to {}", file.getSize(), filePath);
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            if (blob == null) {
                throw new IOException("Failed to create blob - blob is null");
            }
            
            // Generate public URL instead of signed URL to avoid authentication issues
            String publicUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                bucketName, java.net.URLEncoder.encode(filePath, "UTF-8"));
            
            logger.info("✅ File uploaded successfully to Firebase Storage: {} -> {}", filePath, publicUrl);
            return publicUrl;
            
        } catch (Exception e) {
            logger.error("❌ Failed to upload file to Firebase Storage: {}", e.getMessage(), e);
            
            // Log detailed error information
            if (e.getMessage().contains("does not exist")) {
                logger.error("🔧 Bucket issue - check service account permissions and bucket name");
                logger.error("🔧 Current bucket: {}", bucketName);
                logger.error("🔧 Project ID: {}", FirebaseConfig.getProjectId());
            } else if (e.getMessage().contains("permission") || e.getMessage().contains("forbidden")) {
                logger.error("🔧 Permission issue - check service account has Storage Admin role");
            }
            
            throw new IOException("Firebase upload failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete file by Firebase Storage path
     */
    public void deleteFile(String filePath) {
        try {
            if (!isAvailable()) {
                logger.warn("Firebase Storage not available, cannot delete file: {}", filePath);
                return;
            }
            
            BlobId blobId = BlobId.of(bucketName, filePath);
            boolean deleted = storage.delete(blobId);
            
            if (deleted) {
                logger.info("✅ File deleted from Firebase Storage: {}", filePath);
            } else {
                logger.warn("⚠️ File not found or already deleted: {}", filePath);
            }
        } catch (Exception e) {
            logger.error("❌ Failed to delete file from Firebase Storage: {}", e.getMessage());
        }
    }
    
    /**
     * Delete file by Firebase download URL
     */
    public void deleteFileByUrl(String downloadUrl) {
        try {
            if (downloadUrl == null || !downloadUrl.contains("firebasestorage.googleapis.com")) {
                logger.warn("Invalid Firebase Storage URL: {}", downloadUrl);
                return;
            }
            
            // Extract file path from URL
            String filePath = extractFilePathFromUrl(downloadUrl);
            if (filePath != null) {
                deleteFile(filePath);
            }
        } catch (Exception e) {
            logger.error("❌ Failed to delete file by URL: {}", e.getMessage());
        }
    }
    
    /**
     * Delete existing file if it exists (used before uploading new version)
     */
    private void deleteExistingFile(String filePath) {
        try {
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob existingBlob = storage.get(blobId);
            
            if (existingBlob != null && existingBlob.exists()) {
                storage.delete(blobId);
                logger.info("✅ Deleted existing file: {}", filePath);
            }
        } catch (Exception e) {
            logger.debug("File doesn't exist or couldn't be deleted: {}", filePath);
        }
    }
    
    /**
     * Extract file path from Firebase Storage download URL
     */
    private String extractFilePathFromUrl(String downloadUrl) {
        try {
            // Format: https://firebasestorage.googleapis.com/v0/b/{bucket}/o/{path}?{params}
            if (downloadUrl.contains("/o/")) {
                String pathPart = downloadUrl.split("/o/")[1];
                if (pathPart.contains("?")) {
                    pathPart = pathPart.split("\\?")[0];
                }
                return java.net.URLDecoder.decode(pathPart, "UTF-8");
            }
        } catch (Exception e) {
            logger.error("Failed to extract file path from URL: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return ".jpg"; // Default extension
    }
    
    /**
     * Get the mapping for course types
     */
    public String getCourseTypeFolder(Long courseTypeId) {
        if (courseTypeId == null) return "1_Strength";
        
        switch (courseTypeId.intValue()) {
            case 1: return "1_Strength";
            case 2: return "2_Cardiovascular";
            case 3: return "3_Flexibility";
            case 4: return "4_StrengthEndur";
            case 5: return "5_MindBody";
            case 6: return "6_Functional";
            case 7: return "7_WeightLoss";
            default: return "1_Strength";
        }
    }
}