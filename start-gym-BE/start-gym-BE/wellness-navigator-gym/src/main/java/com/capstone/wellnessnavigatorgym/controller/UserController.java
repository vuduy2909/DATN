package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.Role;
import com.capstone.wellnessnavigatorgym.service.IAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class UserController {

    @Autowired
    private IAccountService accountService;

    @GetMapping("/users/info")
    public ResponseEntity<?> getUserInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Get account information
            Optional<Account> accountOpt = accountService.findAccountByUserName(username);
            if (!accountOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
            }
            Account account = accountOpt.get();
            
            // Get user roles
            List<String> roles = account.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());
            
            // Create response
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", account.getAccountId());
            userInfo.put("username", account.getUserName());
            userInfo.put("email", account.getEmail());
            userInfo.put("roles", roles.isEmpty() ? null : roles.get(0)); // Return first role for compatibility
            userInfo.put("allRoles", roles); // Return all roles
            
            return ResponseEntity.ok(userInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting user info: " + e.getMessage());
        }
    }
} 