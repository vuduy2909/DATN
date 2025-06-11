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
        
        // Check if account is enabled
        if (!account.getIsEnable()) {
            logger.warn("Account is disabled for username: {}", username);
            throw new UsernameNotFoundException("Account is disabled: " + username);
        }
        
        // Check if associated customer (if any) is banned
        Customer customer = customerRepository.findByAccountUserName(username);
        if (customer != null) {
            logger.info("Customer found: ID={}, Name={}, Email={}, Enabled={}", 
                       customer.getCustomerId(), customer.getCustomerName(), 
                       customer.getCustomerEmail(), customer.getIsEnable());
            
            if (!customer.getIsEnable()) {
                logger.warn("Customer is banned for username: {}, Customer ID: {}, Customer Name: {}", 
                           username, customer.getCustomerId(), customer.getCustomerName());
                throw new UsernameNotFoundException("Customer account is banned: " + username);
            }
        } else {
            logger.info("No customer found for username: {} (likely admin account)", username);
        }
        
        logger.info("Login successful for username: {}", username);
        return UserPrinciple.build(account);
    }
}
