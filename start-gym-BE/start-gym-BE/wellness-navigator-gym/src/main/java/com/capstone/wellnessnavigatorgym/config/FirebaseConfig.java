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
            } else {
                logger.info("🔧 [FIREBASE_INIT] ✅ Firebase already initialized");
            }
        } catch (Exception e) {
            logger.error("🔧 [FIREBASE_INIT] ❌ Failed to initialize Firebase: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
    
    @Bean
    public Storage storage() {
        try {
            ClassPathResource resource = new ClassPathResource(SERVICE_ACCOUNT_KEY_PATH);
            InputStream serviceAccount = resource.getInputStream();
            
            GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
            
            Storage storage = StorageOptions.newBuilder()
                .setCredentials(credentials)
                .setProjectId(PROJECT_ID)
                .build()
                .getService();
                
            logger.info("✅ Google Cloud Storage initialized successfully");
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
} 