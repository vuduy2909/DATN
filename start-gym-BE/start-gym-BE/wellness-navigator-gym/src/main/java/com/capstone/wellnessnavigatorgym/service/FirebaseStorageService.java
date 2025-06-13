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
        try {
            bucketName = FirebaseConfig.getBucketName();
            logger.info("🔧 Firebase Storage Service initialized with bucket: {}", bucketName);
            
            // Test initial connection
            if (isAvailable()) {
                logger.info("🔧 ✅ Firebase Storage connection verified");
            } else {
                logger.warn("🔧 ⚠️ Firebase Storage not initially available - will retry on first use");
            }
            
        } catch (Exception e) {
            logger.error("🔧 ❌ Firebase Storage Service initialization failed: {}", e.getMessage());
        }
    }
    
    public boolean isAvailable() {
        try {
            if (storage == null) {
                logger.warn("🔧 Storage bean is null");
                return false;
            }
            
            // Test connection with timeout
            storage.get(bucketName); // This will test connectivity
            return true;
            
        } catch (Exception e) {
            logger.warn("🔧 Firebase Storage availability check failed: {}", e.getMessage());
        return false;
    }
    }
    
    /**
     * Retry wrapper for Firebase operations with exponential backoff
     */
    private <T> T executeWithRetry(java.util.function.Supplier<T> operation, String operationName) throws IOException {
        int maxRetries = 3;
        int baseDelay = 1000; // 1 second
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.info("🔧 [RETRY] Executing {} - Attempt {}/{}", operationName, attempt, maxRetries);
                T result = operation.get();
                if (attempt > 1) {
                    logger.info("🔧 [RETRY] ✅ {} succeeded on attempt {}", operationName, attempt);
                }
                return result;
                
            } catch (Exception e) {
                logger.warn("🔧 [RETRY] ❌ {} failed on attempt {}: {}", operationName, attempt, e.getMessage());
                
                if (attempt == maxRetries) {
                    throw new IOException("Firebase operation failed after " + maxRetries + " attempts: " + e.getMessage(), e);
                }
                
                // Exponential backoff delay
                try {
                    int delay = baseDelay * (int) Math.pow(2, attempt - 1);
                    logger.info("🔧 [RETRY] Waiting {}ms before retry...", delay);
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Retry interrupted", ie);
                }
            }
        }
        
        throw new IOException("Unexpected retry loop exit");
    }
    
    /**
     * Upload user avatar with naming format: avatar_{username}.ext
     * Stored in: images/Avatar/
     * Deletes any existing avatar for the user (all formats)
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
        
        // Delete any existing avatar for this user (all formats)
        try {
            logger.info("🔧 [AVATAR_UPLOAD] Deleting any existing avatar for user: {}", userName);
            deleteExistingAvatar(userName);
            logger.info("🔧 [AVATAR_UPLOAD] ✅ Existing avatar cleanup completed");
        } catch (Exception e) {
            logger.warn("🔧 [AVATAR_UPLOAD] ⚠️ Failed to cleanup existing avatar: {}", e.getMessage());
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
        logger.info("🔧 [COURSE_IMG_UPLOAD] Starting course image upload - courseType: {}, courseName: {}", courseType, courseName);
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!isAvailable()) {
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        
        // Sanitize course name for filename - keep original name format
        String sanitizedName = courseName.replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim()
            .replaceAll("\\s+", " "); // Keep spaces as spaces for course name
            
        String fileName = sanitizedName + fileExtension;
        String filePath = "images/Course/" + courseType + "/" + fileName;
        
        logger.info("🔧 [COURSE_IMG_UPLOAD] Generated file path: {}", filePath);
        
        // Delete any existing course image for this course (all formats)
        try {
            logger.info("🔧 [COURSE_IMG_UPLOAD] Deleting any existing course image for: {}", courseName);
            deleteExistingCourseImage(courseType, courseName);
            logger.info("🔧 [COURSE_IMG_UPLOAD] ✅ Existing course image cleanup completed");
        } catch (Exception e) {
            logger.warn("🔧 [COURSE_IMG_UPLOAD] ⚠️ Failed to cleanup existing course image: {}", e.getMessage());
        }
        
        // Also delete old image by URL if provided (fallback)
        if (oldImageUrl != null && !oldImageUrl.trim().isEmpty()) {
            try {
                deleteFileByUrl(oldImageUrl);
                logger.info("🔧 [COURSE_IMG_UPLOAD] ✅ Deleted old course image by URL: {}", oldImageUrl);
            } catch (Exception e) {
                logger.warn("🔧 [COURSE_IMG_UPLOAD] ⚠️ Failed to delete old course image by URL: {}, error: {}", oldImageUrl, e.getMessage());
        }
        }
        
        // Upload new course image
        logger.info("🔧 [COURSE_IMG_UPLOAD] Starting Firebase upload to path: {}", filePath);
        String downloadUrl = uploadFileToFirebase(file, filePath);
        
        if (downloadUrl != null && !downloadUrl.isEmpty()) {
            logger.info("🔧 [COURSE_IMG_UPLOAD] ✅ Course image uploaded successfully: {} -> {}", filePath, downloadUrl);
        } else {
            logger.error("🔧 [COURSE_IMG_UPLOAD] ❌ Upload failed - received null/empty download URL");
            throw new IOException("Upload failed - received null/empty download URL");
        }
        
        return downloadUrl;
    }
    
    /**
     * Upload course image by courseId - for editing existing courses
     * This method fetches course info and handles old image deletion automatically
     */
    public String uploadCourseImageById(MultipartFile file, Integer courseId) throws IOException {
        logger.info("🔧 [COURSE_IMG_BY_ID] Starting course image upload for courseId: {}", courseId);
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (!isAvailable()) {
            throw new RuntimeException("Firebase Storage is not available");
        }
        
        // Note: We need to inject Course repository or get course info from controller
        // For now, this method will be called from controller with course details
        throw new UnsupportedOperationException("Use uploadCourseImage with courseType and courseName from controller");
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
     * Core method to upload file to Firebase Storage using unified Firebase Admin SDK approach
     * Following Firebase best practices from official documentation
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
            logger.info("🔧 [FIREBASE_UPLOAD] ✅ Starting Firebase upload using unified approach");
            logger.info("🔧 [FIREBASE_UPLOAD] File: {}, Size: {} bytes, Path: {}", 
                file.getOriginalFilename(), file.getSize(), filePath);
            // UNIFIED APPROACH: Use Google Cloud Storage SDK consistently
            // Generate download token for Firebase Storage access
            String downloadToken = java.util.UUID.randomUUID().toString();
            // Create blob info with proper Firebase Storage metadata using helper method
            com.google.cloud.storage.BlobInfo blobInfo = createFirebaseStorageBlobInfo(
                filePath, file.getContentType(), downloadToken);
            logger.info("🔧 [FIREBASE_UPLOAD] Uploading blob with ContentType: {} and download token", file.getContentType());
            // CRITICAL: Upload file with retry mechanism - this MUST succeed or throw exception
            com.google.cloud.storage.Blob blob;
            try {
                blob = executeWithRetry(() -> {
                    try {
                        return storage.create(blobInfo, file.getBytes());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, "Upload file to Firebase Storage");
            } catch (Exception e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Blob creation failed: {}", e.getMessage());
                throw new IOException("UPLOAD FAILED: Cannot create blob on Firebase Storage - " + e.getMessage());
        }
            if (blob == null) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Blob creation returned null");
                throw new IOException("UPLOAD FAILED: Blob creation returned null");
            }
            logger.info("🔧 [FIREBASE_UPLOAD] ✅ Blob created - Generation: {}", blob.getGeneration());
            // CRITICAL: Verify file actually exists
            if (!blob.exists()) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: File not found after upload!");
                throw new IOException("UPLOAD FAILED: File was not actually uploaded to Firebase Storage");
            }
            // Additional verification - reload to get latest state
            try {
                blob = blob.reload();
                logger.info("🔧 [FIREBASE_UPLOAD] ✅ File verified - Size: {} bytes, Created: {}", 
                    blob.getSize(), blob.getCreateTime());
            } catch (Exception e) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ File verification failed: {}", e.getMessage());
                throw new IOException("UPLOAD FAILED: File verification failed - " + e.getMessage());
            }
            // LUÔN trả về URL đúng chuẩn Firebase Storage (có token)
            String firebaseUrl = generateFirebaseStorageUrl(filePath, downloadToken);
            logger.info("🔧 [FIREBASE_UPLOAD] ✅ Generated Firebase Storage URL: {}", firebaseUrl);
            // Check URL length - must fit in database VARCHAR(500)
            if (firebaseUrl.length() > 500) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: URL too long - {} chars", firebaseUrl.length());
                throw new IOException("UPLOAD FAILED: Generated URL too long (" + firebaseUrl.length() + " chars) - max 500 chars");
            }
            // CRITICAL: Test URL accessibility using helper method
            if (!validateUrlAccessibility(firebaseUrl)) {
                logger.error("🔧 [FIREBASE_UPLOAD] ❌ FATAL: Generated URL is not accessible");
                throw new IOException("UPLOAD FAILED: Generated URL is not accessible");
            }
            logger.info("🔧 [FIREBASE_UPLOAD] 🎉 SUCCESS! File uploaded and URL verified");
            logger.info("🔧 [FIREBASE_UPLOAD] Final URL: {}", firebaseUrl);
            return firebaseUrl;
        } catch (Exception e) {
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ CRITICAL FAILURE: {}", e.getMessage(), e);
            logger.error("🔧 [FIREBASE_UPLOAD] ❌ UPLOAD COMPLETELY FAILED - NO MOCK URLs!");
            // Enhanced error diagnosis following Firebase troubleshooting guide
            if (e.getMessage().contains("does not exist") || e.getMessage().contains("bucket")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Bucket issue - check Firebase project: {}", bucketName);
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Verify bucket name format: should be 'project-id.appspot.com'");
            } else if (e.getMessage().contains("permission") || e.getMessage().contains("forbidden") || e.getMessage().contains("401") || e.getMessage().contains("403")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Permission issue - check service account has Storage Admin role");
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Verify service account key file is valid and not expired");
            } else if (e.getMessage().contains("quota") || e.getMessage().contains("exceeded")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Storage quota exceeded - check Firebase billing and limits");
            } else if (e.getMessage().contains("network") || e.getMessage().contains("timeout") || e.getMessage().contains("connect")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Network issue - check internet connection to Firebase Storage");
            } else if (e.getMessage().contains("token") || e.getMessage().contains("credential")) {
                logger.error("🔧 [FIREBASE_UPLOAD] 💡 Authentication issue - check service account credentials");
    }
            // Re-throw with clear error message
            throw new IOException("🔥 FIREBASE STORAGE UPLOAD FAILED: " + e.getMessage(), e);
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
    
    /**
     * Generate Firebase Storage download URL following best practices
     * Supports both download token URLs and public URLs
     */
    private String generateFirebaseStorageUrl(String filePath, String downloadToken) {
        try {
            if (downloadToken != null && !downloadToken.trim().isEmpty()) {
                // Method 1: Firebase Storage URL with download token (Recommended for private/secure access)
                String encodedPath = java.net.URLEncoder.encode(filePath, "UTF-8");
                String url = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                    bucketName, encodedPath, downloadToken);
                
                logger.info("🔧 [URL_GEN] Generated Firebase Storage URL with token: {} chars", url.length());
                return url;
            } else {
                // Method 2: Google Cloud Storage public URL (Fallback for public access)
                String url = String.format("https://storage.googleapis.com/%s/%s", bucketName, filePath);
                
                logger.info("🔧 [URL_GEN] Generated GCS public URL: {} chars", url.length());
                return url;
            }
        } catch (Exception e) {
            logger.error("🔧 [URL_GEN] Failed to generate Firebase Storage URL: {}", e.getMessage());
            throw new RuntimeException("URL generation failed: " + e.getMessage());
        }
    }
    
    /**
     * Validate Firebase Storage URL accessibility with proper HTTP handling
     */
    private boolean validateUrlAccessibility(String url) {
        try {
            logger.info("🔧 [URL_TEST] Testing URL accessibility: {}", url);
            
            java.net.URL testUrl = new java.net.URL(url);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) testUrl.openConnection();
            
            // Set proper request method and headers for Firebase Storage
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(15000); // 15 seconds
            connection.setReadTimeout(15000);    // 15 seconds
            connection.setInstanceFollowRedirects(true);
            
            // Add standard headers
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("User-Agent", "WellnessNavigatorGym/1.0");
            connection.setRequestProperty("Cache-Control", "no-cache");
            
            connection.connect();
            int responseCode = connection.getResponseCode();
            
            // Check for successful response codes
            boolean isAccessible = (responseCode == 200 || responseCode == 301 || responseCode == 302);
            
            if (isAccessible) {
                logger.info("🔧 [URL_TEST] ✅ URL accessible - HTTP {}", responseCode);
                
                // Log additional response info if available
                String contentType = connection.getContentType();
                String contentLength = connection.getHeaderField("Content-Length");
                
                if (contentType != null) {
                    logger.info("🔧 [URL_TEST] Content-Type: {}", contentType);
                }
                if (contentLength != null) {
                    logger.info("🔧 [URL_TEST] Content-Length: {} bytes", contentLength);
                }
            } else {
                logger.error("🔧 [URL_TEST] ❌ URL not accessible - HTTP {} {}", 
                    responseCode, connection.getResponseMessage());
            }
            
            connection.disconnect();
            return isAccessible;
            
        } catch (Exception e) {
            logger.error("🔧 [URL_TEST] ❌ URL accessibility test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Create blob with Firebase Storage metadata following best practices
     */
    private com.google.cloud.storage.BlobInfo createFirebaseStorageBlobInfo(String filePath, String contentType, String downloadToken) {
        com.google.cloud.storage.BlobId blobId = com.google.cloud.storage.BlobId.of(bucketName, filePath);
        
        com.google.cloud.storage.BlobInfo.Builder blobInfoBuilder = com.google.cloud.storage.BlobInfo.newBuilder(blobId)
            .setContentType(contentType)
            .setCacheControl("public, max-age=31536000"); // 1 year cache
        
        // Add Firebase Storage specific metadata
        java.util.Map<String, String> metadata = new java.util.HashMap<>();
        
        if (downloadToken != null && !downloadToken.trim().isEmpty()) {
            metadata.put("firebaseStorageDownloadTokens", downloadToken);
        }
        
        // Add custom metadata for tracking
        metadata.put("uploadedBy", "WellnessNavigatorGym");
        metadata.put("uploadedAt", java.time.Instant.now().toString());
        
        blobInfoBuilder.setMetadata(metadata);
        
        return blobInfoBuilder.build();
    }
    
    /**
     * Delete any existing avatar for a user (all formats)
     */
    private void deleteExistingAvatar(String userName) {
        logger.info("🔧 [DELETE_AVATAR] Searching for existing avatar files for user: {}", userName);
        
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        String avatarFolder = "images/Avatar/";
        
        for (String ext : extensions) {
            String fileName = "avatar_" + userName + ext;
            String filePath = avatarFolder + fileName;
            
            try {
                BlobId blobId = BlobId.of(bucketName, filePath);
                Blob existingBlob = storage.get(blobId);
                
                if (existingBlob != null && existingBlob.exists()) {
                    boolean deleted = storage.delete(blobId);
                    if (deleted) {
                        logger.info("🔧 [DELETE_AVATAR] ✅ Deleted existing avatar: {}", filePath);
                    } else {
                        logger.warn("🔧 [DELETE_AVATAR] ⚠️ Failed to delete existing avatar: {}", filePath);
                    }
                }
            } catch (Exception e) {
                logger.warn("🔧 [DELETE_AVATAR] ⚠️ Error checking/deleting avatar {}: {}", filePath, e.getMessage());
            }
        }
    }
    
    /**
     * Delete existing course image for a specific course
     */
    private void deleteExistingCourseImage(String courseTypeName, String courseName) {
        logger.info("🔧 [DELETE_COURSE_IMG] Searching for existing course image: courseType={}, courseName={}", 
            courseTypeName, courseName);
        
        String[] extensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        
        // Sanitize course name for filename - keep original name format matching uploadCourseImage
        String sanitizedName = courseName.replaceAll("[^a-zA-Z0-9\\s]", "")
            .trim()
            .replaceAll("\\s+", " ");
        
        String courseFolder = "images/Course/" + courseTypeName + "/";
        
        for (String ext : extensions) {
            String fileName = sanitizedName + ext;
            String filePath = courseFolder + fileName;
            
            try {
                BlobId blobId = BlobId.of(bucketName, filePath);
                Blob existingBlob = storage.get(blobId);
                
                if (existingBlob != null && existingBlob.exists()) {
                    boolean deleted = storage.delete(blobId);
                    if (deleted) {
                        logger.info("🔧 [DELETE_COURSE_IMG] ✅ Deleted existing course image: {}", filePath);
                    } else {
                        logger.warn("🔧 [DELETE_COURSE_IMG] ⚠️ Failed to delete existing course image: {}", filePath);
                    }
                }
            } catch (Exception e) {
                logger.warn("🔧 [DELETE_COURSE_IMG] ⚠️ Error checking/deleting course image {}: {}", filePath, e.getMessage());
            }
        }
    }
} 