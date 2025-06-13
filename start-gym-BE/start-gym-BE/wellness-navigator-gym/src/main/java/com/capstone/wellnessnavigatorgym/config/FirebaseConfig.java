package com.capstone.wellnessnavigatorgym.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    private static final String SERVICE_ACCOUNT_KEY_PATH = "config/capstone-project-firebase-key.json";
    // CRITICAL FIX: Use actual Firebase bucket name from console (.firebasestorage.app)
    private static final String BUCKET_NAME = "capstone-project-2909.firebasestorage.app";
    private static final String PROJECT_ID = "capstone-project-2909";
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            logger.info("🔧 [FIREBASE_INIT] Starting Firebase initialization...");
            
            if (FirebaseApp.getApps().isEmpty()) {
                logger.info("🔧 [FIREBASE_INIT] Loading service account from: {}", SERVICE_ACCOUNT_KEY_PATH);
                ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT_KEY_PATH);
                
                if (!resource.exists()) {
                    logger.error("🔧 [FIREBASE_INIT] ❌ Service account file not found: {}", SERVICE_ACCOUNT_KEY_PATH);
                    throw new RuntimeException("Service account file not found");
                }
                
                InputStream serviceAccount = resource.getInputStream();
                logger.info("🔧 [FIREBASE_INIT] Service account file loaded successfully");
                
                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(BUCKET_NAME)
                    .setProjectId(PROJECT_ID)
                    .build();
                
                FirebaseApp.initializeApp(options);
                logger.info("🔧 [FIREBASE_INIT] ✅ Firebase initialized successfully");
                logger.info("🔧 [FIREBASE_INIT] Project ID: {}", PROJECT_ID);
                logger.info("🔧 [FIREBASE_INIT] Storage bucket: {}", BUCKET_NAME);
                
                // Verify bucket accessibility
                testBucketAccess();
                
            } else {
                logger.info("🔧 [FIREBASE_INIT] ✅ Firebase already initialized");
            }
        } catch (Exception e) {
            logger.error("🔧 [FIREBASE_INIT] ❌ Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
    
    /**
     * Test if we can access the Firebase Storage bucket
     */
    private void testBucketAccess() {
        try {
            Storage tempStorage = createStorageInstance();
            com.google.cloud.storage.Bucket bucket = tempStorage.get(BUCKET_NAME);
            
            if (bucket != null && bucket.exists()) {
                logger.info("🔧 [FIREBASE_INIT] ✅ Bucket access verified: {}", BUCKET_NAME);
                logger.info("🔧 [FIREBASE_INIT] Bucket location: {}", bucket.getLocation());
                logger.info("🔧 [FIREBASE_INIT] Bucket storage class: {}", bucket.getStorageClass());
            } else {
                logger.warn("🔧 [FIREBASE_INIT] ⚠️ Bucket not found or not accessible: {}", BUCKET_NAME);
            }
        } catch (Exception e) {
            logger.warn("🔧 [FIREBASE_INIT] ⚠️ Bucket access test failed: {}", e.getMessage());
        }
    }
    
    /**
     * Create Storage instance with proper configuration
     */
    private Storage createStorageInstance() throws IOException {
            ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT_KEY_PATH);
            InputStream serviceAccount = resource.getInputStream();
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            
        return StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(PROJECT_ID)
            // Set connection timeout (30 seconds)
            .setTransportOptions(
                com.google.cloud.http.HttpTransportOptions.newBuilder()
                    .setConnectTimeout(30000) // 30 seconds
                    .setReadTimeout(60000)    // 60 seconds  
                    .build()
            )
            // Configure retry settings
            .setRetrySettings(
                com.google.api.gax.retrying.RetrySettings.newBuilder()
                    .setMaxAttempts(3)
                    .setInitialRetryDelay(org.threeten.bp.Duration.ofSeconds(1))
                    .setMaxRetryDelay(org.threeten.bp.Duration.ofSeconds(10))
                    .setRetryDelayMultiplier(2.0)
                    .setTotalTimeout(org.threeten.bp.Duration.ofMinutes(2))
                    .build()
            )
                .build()
                .getService();
    }
    
    @Bean
    public Storage storage() {
        try {
            Storage storage = createStorageInstance();
            logger.info("✅ Google Cloud Storage initialized with timeout settings");
            logger.info("🔧 Connection timeout: 30s, Read timeout: 60s, Max retries: 3");
            return storage;
                
        } catch (IOException e) {
            logger.error("❌ Failed to initialize Google Cloud Storage", e);
            throw new RuntimeException("Google Cloud Storage initialization failed", e);
        }
    }
    
    public static String getBucketName() {
        return BUCKET_NAME;
    }
    
    public static String getProjectId() {
        return PROJECT_ID;
    }
    
    /**
     * Get Firebase Storage URL format for download tokens
     */
    public static String getFirebaseStorageBaseUrl() {
        return "https://firebasestorage.googleapis.com/v0/b/" + BUCKET_NAME + "/o/";
    }
    
    /**
     * Get Google Cloud Storage URL format for public files
     */
    public static String getGcsBaseUrl() {
        return "https://storage.googleapis.com/" + BUCKET_NAME + "/";
    }
} 