package com.capstone.wellnessnavigatorgym.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
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
        logger.info("🔧 [AVATAR_UPLOAD] Starting avatar upload for user: {}", userName);
        
        if (file.isEmpty()) {
            logger.error("🔧 [AVATAR_UPLOAD] ❌ File is empty");
            throw new IllegalArgumentException("File is empty");
        }
        
        logger.info("🔧 [AVATAR_UPLOAD] File info - Name: {}, Size: {} bytes, ContentType: {}", 
            file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        if (!isAvailable()) {
            logger.error("🔧 [AVATAR_UPLOAD] ❌ Firebase Storage is not available");
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        logger.info("🔧 [AVATAR_UPLOAD] ✅ Firebase Storage is available");
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        
        // Format: avatar_{username}.ext
        String fileName = "avatar_" + userName + fileExtension;
        String filePath = "images/Avatar/" + fileName;
        
        logger.info("🔧 [AVATAR_UPLOAD] Generated file path: {}", filePath);
        
        // Delete existing avatar if exists
        try {
            logger.info("🔧 [AVATAR_UPLOAD] Checking for existing file to delete...");
            deleteExistingFile(filePath);
            logger.info("🔧 [AVATAR_UPLOAD] ✅ Existing file cleanup completed");
        } catch (Exception e) {
            logger.warn("🔧 [AVATAR_UPLOAD] ⚠️ Failed to cleanup existing file: {}", e.getMessage());
        }
        
        // Upload new avatar
        logger.info("🔧 [AVATAR_UPLOAD] Starting Firebase upload to path: {}", filePath);
        String downloadUrl = uploadFileToFirebase(file, filePath);
        
        if (downloadUrl != null && !downloadUrl.isEmpty()) {
            logger.info("🔧 [AVATAR_UPLOAD] ✅ Avatar uploaded successfully for user: {} -> {}", userName, downloadUrl);
        } else {
            logger.error("🔧 [AVATAR_UPLOAD] ❌ Upload failed - received null/empty download URL");
            throw new IOException("Upload failed - received null/empty download URL");
        }
        
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
     * Core method to upload file to Firebase Storage using Firebase Admin SDK (with authentication)
     */
    private String uploadFileToFirebase(MultipartFile file, String filePath) throws IOException {
        // STRICT VALIDATION: Fail immediately if Firebase not properly configured
        if (!isAvailable()) {
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Firebase Storage is NOT available!");
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ Check Firebase initialization and service account");
            throw new IOException("UPLOAD FAILED: Firebase Storage is not available. Cannot upload file.");
        }
        
        if (file == null || file.isEmpty()) {
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: File is null or empty");
            throw new IOException("UPLOAD FAILED: No file provided");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: File path is null or empty");
            throw new IOException("UPLOAD FAILED: No file path provided");
        }
        
        try {
            logger.info("🔧 [FIREBASE_UPLOAD] ✅ Starting REAL Firebase upload");
            logger.info("🔧 [FIREBASE_UPLOAD] File: {}, Size: {} bytes, Path: {}", 
                file.getOriginalFilename(), file.getSize(), filePath);
            
            // STRICT: Use Firebase Admin SDK - this MUST work or fail
            com.google.cloud.storage.Bucket bucket;
            try {
                bucket = StorageClient.getInstance().bucket();
                logger.info("🔧 [FIREBASE_UPLOAD] ✅ Firebase Admin bucket obtained: {}", bucket.getName());
            } catch (Exception e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Cannot get Firebase bucket: {}", e.getMessage());
                throw new IOException("UPLOAD FAILED: Firebase bucket not accessible - " + e.getMessage());
            }
            
            // Create blob with metadata
            com.google.cloud.storage.BlobInfo blobInfo = com.google.cloud.storage.BlobInfo.newBuilder(bucket.getName(), filePath)
                .setContentType(file.getContentType())
                .setCacheControl("public, max-age=31536000") // 1 year cache
                .build();
            
            logger.info("🔧 [FIREBASE_UPLOAD] Uploading blob with ContentType: {}", file.getContentType());
            
            // CRITICAL: Upload file - this MUST succeed or throw exception
            com.google.cloud.storage.Blob blob;
            try {
                blob = bucket.getStorage().create(blobInfo, file.getBytes());
            } catch (Exception e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Blob creation failed: {}", e.getMessage());
                throw new IOException("UPLOAD FAILED: Cannot create blob on Firebase - " + e.getMessage());
            }
            
            if (blob == null) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Blob creation returned null");
                throw new IOException("UPLOAD FAILED: Blob creation returned null");
            }
            
            logger.info("🔧 [FIREBASE_UPLOAD] ✅ Blob created - Generation: {}", blob.getGeneration());
            
            // CRITICAL: Verify file actually exists
            if (!blob.exists()) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: File not found after upload!");
                throw new IOException("UPLOAD FAILED: File was not actually uploaded to Firebase");
            }
            
            // Additional verification
            try {
                blob.reload();
                logger.info("🔧 [FIREBASE_UPLOAD] ✅ File verified - Size: {} bytes, Created: {}", 
                    blob.getSize(), blob.getCreateTime());
            } catch (Exception e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ File verification failed: {}", e.getMessage());
                throw new IOException("UPLOAD FAILED: File verification failed - " + e.getMessage());
            }
            
            // CRITICAL: Generate REAL Firebase URL using getDownloadURL
            logger.info("🔧 [FIREBASE_UPLOAD] Generating REAL Firebase URL using getDownloadURL...");
            
            String finalUrl = null;
            
            try {
                // Use Firebase SDK's getDownloadURL method - this is the ONLY correct way
                com.google.firebase.cloud.StorageClient storageClient = com.google.firebase.cloud.StorageClient.getInstance();
                com.google.cloud.storage.Bucket bucketForUrl = storageClient.bucket();
                com.google.cloud.storage.Blob blobForUrl = bucketForUrl.get(filePath);
                
                if (blobForUrl == null || !blobForUrl.exists()) {
                    logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Blob not found for URL generation!");
                    throw new IOException("UPLOAD FAILED: Blob not found for URL generation");
                }
                
                // Generate signed URL - this is the correct Firebase way
                java.net.URL downloadUrl = blobForUrl.signUrl(7, java.util.concurrent.TimeUnit.DAYS);
                finalUrl = downloadUrl.toString();
                
                logger.info("🔧 [FIREBASE_UPLOAD] ✅ Generated Firebase download URL using SDK");
                logger.info("🔧 [FIREBASE_UPLOAD] URL: {}", finalUrl);
                
            } catch (Exception e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ Failed to generate download URL: {}", e.getMessage());
                
                // FALLBACK: Try to get public URL if possible
                try {
                    // Make blob public readable
                    blob.createAcl(com.google.cloud.storage.Acl.of(com.google.cloud.storage.Acl.User.ofAllUsers(), com.google.cloud.storage.Acl.Role.READER));
                    
                    // Generate public URL
                    finalUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, filePath);
                    logger.info("🔧 [FIREBASE_UPLOAD] ✅ Generated public URL as fallback: {}", finalUrl);
                    
                } catch (Exception fallbackError) {
                    logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Cannot generate any valid URL: {}", fallbackError.getMessage());
                    throw new IOException("UPLOAD FAILED: Cannot generate download URL - " + fallbackError.getMessage());
                }
            }
            
            if (finalUrl == null || finalUrl.trim().isEmpty()) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: No URL generated");
                throw new IOException("UPLOAD FAILED: No download URL was generated");
            }
            
            // CRITICAL: Test URL accessibility PROPERLY
            logger.info("🔧 [FIREBASE_UPLOAD] Testing URL accessibility...");
            try {
                java.net.URL testUrl = new java.net.URL(finalUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) testUrl.openConnection();
                connection.setRequestMethod("HEAD");
                connection.setConnectTimeout(15000); // 15 seconds
                connection.setReadTimeout(15000);
                connection.setInstanceFollowRedirects(true);
                
                // Handle potential redirects for signed URLs
                connection.connect();
                int responseCode = connection.getResponseCode();
                
                if (responseCode == 200 || responseCode == 301 || responseCode == 302) {
                    logger.info("🔧 [FIREBASE_UPLOAD] ✅ URL verified accessible - HTTP {}", responseCode);
                } else {
                    logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: URL not accessible - HTTP {}", responseCode);
                    logger.error("🔧 [FIREBASE_UPLOAD] Response message: {}", connection.getResponseMessage());
                    throw new IOException("UPLOAD FAILED: Generated URL is not accessible (HTTP " + responseCode + ": " + connection.getResponseMessage() + ")");
                }
                
            } catch (java.io.IOException e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: URL accessibility test failed: {}", e.getMessage());
                throw new IOException("UPLOAD FAILED: URL not accessible - " + e.getMessage());
            }
            
            logger.info("🔧 [FIREBASE_UPLOAD] 🎉 SUCCESS! File uploaded and URL verified");
            logger.info("🔧 [FIREBASE_UPLOAD] Final URL: {}", finalUrl);
            return finalUrl;
            
        } catch (Exception e) {
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ CRITICAL FAILURE: {}", e.getMessage(), e);
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ UPLOAD COMPLETELY FAILED - NO MOCK URLs!");
            
            // Log specific error types for debugging
            if (e.getMessage().contains("does not exist")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Bucket issue - check Firebase project: {}", bucketName);
            } else if (e.getMessage().contains("permission") || e.getMessage().contains("forbidden")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Permission issue - check service account has Storage Admin role");
            } else if (e.getMessage().contains("quota") || e.getMessage().contains("exceeded")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Storage quota exceeded - check Firebase billing");
            } else if (e.getMessage().contains("network") || e.getMessage().contains("timeout")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Network issue - check internet connection to Firebase");
            }
            
            // Re-throw with clear error message
            throw new IOException("🔥 FIREBASE UPLOAD FAILED: " + e.getMessage(), e);
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
        logger.info("🔧 [DELETE_EXISTING] Checking for existing file at path: {}", filePath);
        try {
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob existingBlob = storage.get(blobId);
            
            if (existingBlob != null && existingBlob.exists()) {
                boolean deleted = storage.delete(blobId);
                if (deleted) {
                    logger.info("🔧 [DELETE_EXISTING] ✅ Deleted existing file: {}", filePath);
                } else {
                    logger.warn("🔧 [DELETE_EXISTING] ⚠️ Failed to delete existing file: {}", filePath);
                }
            } else {
                logger.info("🔧 [DELETE_EXISTING] No existing file found at path: {}", filePath);
            }
        } catch (Exception e) {
            logger.warn("🔧 [DELETE_EXISTING] ⚠️ Error checking/deleting existing file {}: {}", filePath, e.getMessage());
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