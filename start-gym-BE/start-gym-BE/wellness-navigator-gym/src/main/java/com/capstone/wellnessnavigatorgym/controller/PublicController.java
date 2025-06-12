package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.repository.IAccountRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);
    
    @Autowired
    private IAccountRepository accountRepository;
    
    @Autowired
    private ICustomerRepository customerRepository;

    /**
     * Check if a user is banned before attempting login
     */
    @GetMapping("/check-banned-status")
    public ResponseEntity<?> checkBannedStatus(@RequestParam String username) {
        try {
            logger.info("=== CHECKING BANNED STATUS FOR: {} ===", username);
            
            // First find the account
            Optional<Account> accountOpt = accountRepository.findAccountByUserName(username);
            if (!accountOpt.isPresent()) {
                // User doesn't exist
                Map<String, Object> response = new HashMap<>();
                response.put("exists", false);
                response.put("isBanned", false);
                response.put("message", "User not found");
                return ResponseEntity.ok(response);
            }
            
            Account account = accountOpt.get();
            
            // Check account enabled status - handle bit(1) from database
            Boolean accountIsEnable = account.getIsEnable();
            boolean accountEnabled;
            if (accountIsEnable == null) {
                accountEnabled = true; // Default enabled if null
            } else {
                accountEnabled = accountIsEnable.booleanValue();
            }
            logger.info("Account {} enabled status: {} (raw: {})", username, accountEnabled, accountIsEnable);
            
            // Also check customer enabled status if exists
            Customer customer = customerRepository.findByAccountUserName(username);
            boolean customerEnabled = true;
            String customerName = null;
            
            if (customer != null) {
                Boolean customerIsEnable = customer.getIsEnable();
                if (customerIsEnable == null) {
                    customerEnabled = true; // Default enabled if null
                } else {
                    customerEnabled = customerIsEnable.booleanValue();
                }
                customerName = customer.getCustomerName();
                logger.info("Customer {} enabled status: {} (raw: {})", customerName, customerEnabled, customerIsEnable);
            }
            
            // User is banned if either account or customer is disabled (bit value = 0)
            boolean isBanned = !accountEnabled || !customerEnabled;
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", true);
            response.put("isBanned", isBanned);
            response.put("accountEnabled", accountEnabled);
            response.put("customerEnabled", customerEnabled);
            response.put("customerName", customerName);
            response.put("username", username);
            
            if (isBanned) {
                logger.warn("🚫 User {} is BANNED - Account enabled: {}, Customer enabled: {}", 
                    username, accountEnabled, customerEnabled);
                response.put("message", "User is banned");
            } else {
                logger.info("✅ User {} is NOT banned", username);
                response.put("message", "User is active");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error checking banned status for {}: {}", username, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("exists", false);
            errorResponse.put("isBanned", false);
            errorResponse.put("error", "Error checking status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 