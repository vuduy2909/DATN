package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.service.IAccountService;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import com.capstone.wellnessnavigatorgym.service.FirebaseStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/v1/debug")
@CrossOrigin(origins = "*")
public class DebugController {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugController.class);
    
    @Autowired
    private IAccountService accountService;
    
    @Autowired
    private ICustomerService customerService;
    
    @Autowired
    private FirebaseStorageService firebaseStorageService;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Test user ban status comprehensively
     */
    @GetMapping("/test-ban/{username}")
    public ResponseEntity<?> testUserBan(@PathVariable String username) {
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("timestamp", LocalDateTime.now());
        
        try {
            // 1. Check Account
            Optional<Account> accountOpt = accountService.findAccountByUserName(username);
            if (!accountOpt.isPresent()) {
                result.put("accountFound", false);
                result.put("error", "Account not found");
                return ResponseEntity.ok(result);
            }
            
            Account account = accountOpt.get();
            result.put("accountFound", true);
            result.put("accountId", account.getAccountId());
            result.put("accountEnabled", account.getIsEnable());
            result.put("accountEmail", account.getEmail());
            
            // 2. Check Customer
            Customer customer = customerService.findByAccountUserName(username);
            if (customer != null) {
                result.put("customerFound", true);
                result.put("customerId", customer.getCustomerId());
                result.put("customerName", customer.getCustomerName());
                result.put("customerEnabled", customer.getIsEnable());
                result.put("customerEmail", customer.getCustomerEmail());
            } else {
                result.put("customerFound", false);
            }
            
            // 3. Final ban status
            boolean accountBanned = !account.getIsEnable();
            boolean customerBanned = customer != null ? !customer.getIsEnable() : false;
            boolean finalBanned = accountBanned || customerBanned;
            
            result.put("accountBanned", accountBanned);
            result.put("customerBanned", customerBanned);
            result.put("finalBanStatus", finalBanned);
            
            // 4. Login simulation
            if (finalBanned) {
                result.put("loginAllowed", false);
                result.put("redirectTo", "/contact");
                result.put("reason", accountBanned ? "Account disabled" : "Customer banned");
            } else {
                result.put("loginAllowed", true);
                result.put("redirectTo", account.getRoles().toString().contains("ADMIN") ? "/admin" : "/dashboard");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", "Debug failed: " + e.getMessage());
            logger.error("Debug ban test failed for {}: {}", username, e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Test Firebase connectivity
     */
    @GetMapping("/test-firebase")
    public ResponseEntity<?> testFirebase() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        
        try {
            // Test Firebase availability
            boolean available = firebaseStorageService.isAvailable();
            result.put("firebaseAvailable", available);
            
            if (available) {
                result.put("status", "Firebase Storage is connected");
                result.put("bucketName", "Available"); // Don't expose sensitive info
            } else {
                result.put("status", "Firebase Storage is NOT available");
                result.put("issue", "Check Firebase configuration");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", "Firebase test failed: " + e.getMessage());
            result.put("status", "error");
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Test admin comments query
     */
    @GetMapping("/test-comments")
    public ResponseEntity<?> testComments() {
        Map<String, Object> result = new HashMap<>();
        result.put("timestamp", LocalDateTime.now());
        
        try {
            // Simple count query first
            String countQuery = "SELECT COUNT(*) FROM comments";
            Number totalComments = (Number) entityManager.createNativeQuery(countQuery).getSingleResult();
            result.put("totalComments", totalComments.intValue());
            
            if (totalComments.intValue() > 0) {
                // Test full query structure
                String testQuery = "SELECT " +
                    "c.comment_id, c.comment_text, c.customer_id, " +
                    "cu.customer_name, e.exercise_name, course.course_name, d.day_name " +
                    "FROM comments c " +
                    "LEFT JOIN customer cu ON c.customer_id = cu.customer_id " +
                    "LEFT JOIN exercise e ON c.exercise_id = e.exercise_id " +
                    "LEFT JOIN course ON c.course_id = course.course_id " +
                    "LEFT JOIN days d ON c.day_id = d.day_id " +
                    "LIMIT 1";
                
                @SuppressWarnings("unchecked")
                List<Object[]> sampleResult = entityManager.createNativeQuery(testQuery).getResultList();
                
                if (!sampleResult.isEmpty()) {
                    Object[] row = sampleResult.get(0);
                    Map<String, Object> sampleComment = new HashMap<>();
                    sampleComment.put("commentId", row[0]);
                    sampleComment.put("commentText", row[1]);
                    sampleComment.put("customerId", row[2]);
                    sampleComment.put("customerName", row[3]);
                    sampleComment.put("exerciseName", row[4]);
                    sampleComment.put("courseName", row[5]);
                    sampleComment.put("dayName", row[6]);
                    
                    result.put("sampleComment", sampleComment);
                    result.put("queryStatus", "Success");
                } else {
                    result.put("queryStatus", "No data returned");
                }
            } else {
                result.put("queryStatus", "No comments in database");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", "Comments test failed: " + e.getMessage());
            result.put("queryStatus", "Error");
            logger.error("Comments debug failed: {}", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
    
    /**
     * Test timezone handling
     */
    @GetMapping("/test-timezone")
    public ResponseEntity<?> testTimezone() {
        Map<String, Object> result = new HashMap<>();
        
        // Current system time
        result.put("systemTime", LocalDateTime.now());
        result.put("systemTimeZone", java.util.TimeZone.getDefault().getID());
        
        // Database time test
        try {
            String timeQuery = "SELECT NOW() as db_time";
            Object dbTime = entityManager.createNativeQuery(timeQuery).getSingleResult();
            result.put("databaseTime", dbTime);
            result.put("databaseTimeZone", "Should be UTC from DB");
        } catch (Exception e) {
            result.put("databaseTimeError", e.getMessage());
        }
        
        // Java time vs DB time difference
        result.put("explanation", "System and DB should both be UTC for consistency");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Health check for all major components
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", LocalDateTime.now());
        
        // Database connectivity
        try {
            String dbTest = "SELECT 1";
            entityManager.createNativeQuery(dbTest).getSingleResult();
            health.put("database", "OK");
        } catch (Exception e) {
            health.put("database", "ERROR: " + e.getMessage());
        }
        
        // Comments table check
        try {
            String commentCheck = "SELECT COUNT(*) FROM comments LIMIT 1";
            Number count = (Number) entityManager.createNativeQuery(commentCheck).getSingleResult();
            health.put("commentsTable", "OK (" + count + " records)");
        } catch (Exception e) {
            health.put("commentsTable", "ERROR: " + e.getMessage());
        }
        
        // Account/Customer tables check
        try {
            String userCheck = "SELECT COUNT(*) FROM account a JOIN customer c ON a.account_id = c.account_id LIMIT 1";
            Number userCount = (Number) entityManager.createNativeQuery(userCheck).getSingleResult();
            health.put("userTables", "OK (" + userCount + " linked records)");
        } catch (Exception e) {
            health.put("userTables", "ERROR: " + e.getMessage());
        }
        
        health.put("overallStatus", health.values().stream().allMatch(v -> v.toString().startsWith("OK")) ? "HEALTHY" : "ISSUES_DETECTED");
        
        return ResponseEntity.ok(health);
    }
} 