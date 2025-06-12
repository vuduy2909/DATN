package com.capstone.wellnessnavigatorgym.security.userprinciple;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.repository.IAccountRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class UserDetailService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailService.class);

    @Autowired
    IAccountRepository accountRepository;
    
    @Autowired
    ICustomerRepository customerRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("=== LOGIN ATTEMPT ===");
        logger.info("Attempting login for username: {}", username);
        
        Account account = accountRepository.findAccountByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found->username or password" + username));
        
        logger.info("Account found: ID={}, Username={}, Email={}, Enabled={}", 
                   account.getAccountId(), account.getUserName(), account.getEmail(), account.getIsEnable());
        
        // ===== ENHANCED BAN CHECK LOGIC =====
        // Check account is_enable status - handle bit(1) from database properly
        Boolean accountIsEnable = account.getIsEnable();
        boolean accountEnabled = (accountIsEnable != null) ? accountIsEnable.booleanValue() : true;
        
        logger.info("🔍 Account enabled check: raw_value={}, parsed_boolean={}", accountIsEnable, accountEnabled);
        
        if (!accountEnabled) {
            logger.warn("🚫 ACCOUNT IS DISABLED for username: {} (account.is_enable = {})", username, accountIsEnable);
            throw new RuntimeException("Account is banned. Please contact support.");
        }
        
        // Check if associated customer is banned - EVERY user account MUST have a customer
        Customer customer = customerRepository.findByAccountUserName(username);
        if (customer != null) {
            Boolean customerIsEnable = customer.getIsEnable();
            boolean customerEnabled = (customerIsEnable != null) ? customerIsEnable.booleanValue() : true;
            
            logger.info("Customer found: ID={}, Name={}, Email={}, Enabled={} (raw={})", 
                       customer.getCustomerId(), customer.getCustomerName(), 
                       customer.getCustomerEmail(), customerEnabled, customerIsEnable);
            
            logger.info("🔍 Customer enabled check: raw_value={}, parsed_boolean={}", customerIsEnable, customerEnabled);
            
            if (!customerEnabled) {
                logger.warn("🚫 CUSTOMER IS BANNED for username: {}, Customer ID: {}, Customer Name: {} (customer.is_enable = {})", 
                           username, customer.getCustomerId(), customer.getCustomerName(), customerIsEnable);
                throw new RuntimeException("Account is banned. Please contact support.");
            }
        } else {
            // In this system, every account should have an associated customer
            // If no customer found, this might be an admin account or data inconsistency
            logger.warn("⚠️ No customer found for username: {} - this might be admin account or data issue", username);
            
            // Check if this is an admin account by checking roles
            boolean isAdmin = account.getRoles().stream()
                .anyMatch(role -> role.getRoleName().contains("ADMIN"));
            
            if (!isAdmin) {
                logger.error("🚫 Non-admin account without customer record: {}", username);
                throw new RuntimeException("Account configuration error. Please contact support.");
            } else {
                logger.info("✅ Admin account without customer record is allowed: {}", username);
            }
        }
        
        // Final validation: Double check both account and customer are enabled
        if (customer != null) {
            boolean finalAccountCheck = (account.getIsEnable() != null) ? account.getIsEnable().booleanValue() : true;
            boolean finalCustomerCheck = (customer.getIsEnable() != null) ? customer.getIsEnable().booleanValue() : true;
            
            if (!finalAccountCheck || !finalCustomerCheck) {
                logger.error("🚫 FINAL BAN CHECK FAILED - Account enabled: {}, Customer enabled: {}", finalAccountCheck, finalCustomerCheck);
                throw new RuntimeException("Account is banned. Please contact support.");
            }
            
            logger.info("✅ FINAL BAN CHECK PASSED - Account enabled: {}, Customer enabled: {}", finalAccountCheck, finalCustomerCheck);
        }
        
        logger.info("✅ Login validation successful for username: {}", username);
        return UserPrinciple.build(account);
    }
}
