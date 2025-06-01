package com.capstone.wellnessnavigatorgym.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
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
    private boolean firebaseInitialized = false;
    
    // Primary bucket name to try first
    private static final String PRIMARY_BUCKET = "capstone-project-2909.firebasestorage.app";
    // Alternative bucket name to try if the primary fails
    private static final String ALTERNATIVE_BUCKET = "capstone-project-2909.appspot.com";
    
    @PostConstruct
    public void initializeFirebase() {
        try {
            // Check if Firebase app is already initialized
            if (FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource("config/capstone-project-firebase-key.json");
                InputStream serviceAccount = resource.getInputStream();
                
                // Try primary bucket name first
                try {
                    initializeWithBucket(serviceAccount, PRIMARY_BUCKET);
                } catch (Exception e) {
                    logger.warn("⚠️ Failed to initialize with primary bucket {}: {}", PRIMARY_BUCKET, e.getMessage());
                    
                    // If primary fails, try alternative
                    try {
                        // Need to get a fresh input stream since the first one was consumed
                        serviceAccount = resource.getInputStream();
                        initializeWithBucket(serviceAccount, ALTERNATIVE_BUCKET);
                    } catch (Exception e2) {
                        logger.error("❌ Failed to initialize with alternative bucket {}: {}", ALTERNATIVE_BUCKET, e2.getMessage());
                        throw e2; // Re-throw to be caught by outer catch
                    }
                }
            } else {
                firebaseInitialized = true;
                logger.info("✅ Firebase already initialized");
            }
        } catch (Exception e) {
            firebaseInitialized = false;
            logger.error("❌ Failed to initialize Firebase: {}", e.getMessage());
            logger.error("Firebase functionality will be disabled", e);
            // Don't rethrow the exception - allow the application to start without Firebase
        }
    }
    
    private void initializeWithBucket(InputStream serviceAccount, String bucketName) throws IOException {
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setStorageBucket(bucketName)
                .build();
        
        FirebaseApp.initializeApp(options);
        firebaseInitialized = true;
        logger.info("✅ Firebase initialized successfully with bucket: {}", bucketName);
    }
    
    @Bean
    public StorageClient storageClient() {
        if (!firebaseInitialized) {
            logger.warn("⚠️ Firebase not initialized - StorageClient will be null");
            return null;
        }
        
        try {
            StorageClient client = StorageClient.getInstance();
            // Test connectivity
            client.bucket(); 
            logger.info("✅ StorageClient created and tested successfully");
            return client;
        } catch (Exception e) {
            logger.error("❌ Failed to create or test StorageClient: {}", e.getMessage());
            return null;
        }
    }
} 