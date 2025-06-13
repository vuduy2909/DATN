package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.customer.CustomerInfo;
import com.capstone.wellnessnavigatorgym.dto.customer.CustomerUserDetailDto;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import com.capstone.wellnessnavigatorgym.service.FirebaseStorageService;
import com.capstone.wellnessnavigatorgym.utils.ConverterMaxCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api/v1/customer")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @GetMapping("/list")
    public ResponseEntity<Page<Customer>> findAllCustomer(@RequestParam(value = "type", required = false) Optional<String> type,
                                                          @RequestParam(value = "name", required = false) Optional<String> name,
                                                          @RequestParam(value = "address", required = false) Optional<String> address,
                                                          @RequestParam(value = "phone", required = false) Optional<String> phone,
                                                          @RequestParam(value = "page", required = false) Optional<Integer> page,
                                                          @RequestParam(value = "size", required = false) Optional<Integer> size,
                                                          @RequestParam(value = "sort", required = false) Optional<String> sort) {
        String searchType = type.orElse("");
        String searchName = name.orElse("");
        String searchAddress = address.orElse("");
        String searchPhone = phone.orElse("");
        int pages = page.orElse(0);
        int pageSize = size.orElse(100);
        String sortName = sort.orElse("customer_name");
        Page<Customer> searchCustomer = this.customerService.searchCustomers(searchType, searchName, searchAddress,
                searchPhone, PageRequest.of(pages, pageSize, Sort.by(sortName)));
        if (searchCustomer.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(searchCustomer, HttpStatus.OK);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> saveCustomer(@Valid @RequestBody CustomerInfo customerInfo, BindingResult bindingResult) {
        new CustomerInfo().validate(customerInfo, bindingResult);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                    });
            return ResponseEntity.badRequest().body(errors);
        } else {
            Customer customerLimit = customerService.customerLimit();
            customerInfo.setCustomerCode(ConverterMaxCode.generateNextId(customerLimit.getCustomerCode()));
            customerService.saveCustomer(customerInfo);
        }
        return new ResponseEntity<>(new MessageResponse("New customer successfully created!"), HttpStatus.CREATED);
    }


    @GetMapping("/list/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Integer id) {
        return new ResponseEntity<>(customerService.findById(id), HttpStatus.OK);
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateCustomer(@Valid @PathVariable Integer id, @RequestBody CustomerInfo customerInfo, BindingResult bindingResult) {
        new CustomerInfo().validate(customerInfo, bindingResult);
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(
                    error -> {
                        String fieldName = error.getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                    });
            return ResponseEntity.badRequest().body(errors);
        } else {
            customerService.updateCustomer(customerInfo, id);
        }
        return new ResponseEntity<>(new MessageResponse("The customer has successfully edited!"), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer id) {
        customerService.deleteById(id);
        return new ResponseEntity<>(new MessageResponse("Customer has successfully deleted!"), HttpStatus.OK);
    }

    @GetMapping("/detail")
    public ResponseEntity<CustomerUserDetailDto> getDetail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        CustomerUserDetailDto customerUserDetailDto = customerService.findUserDetailByUsername(username);

        if (customerUserDetailDto == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(customerUserDetailDto, HttpStatus.OK);
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Customer> getCustomerProfile(Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerService.findByCustomerEmail(username);
            
            if (customer == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(customer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCustomerProfile(@RequestBody Map<String, Object> profileData, Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerService.findByCustomerEmail(username);
            
            if (customer == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Customer not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Update fields
            if (profileData.containsKey("customerName")) {
                customer.setCustomerName((String) profileData.get("customerName"));
            }
            if (profileData.containsKey("customerPhone")) {
                customer.setCustomerPhone((String) profileData.get("customerPhone"));
            }
            if (profileData.containsKey("customerAddress")) {
                customer.setCustomerAddress((String) profileData.get("customerAddress"));
            }
            
            customerService.saveCustomer(customer);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> updateAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "username", required = false) String username,
            Authentication authentication
    ) {
        try {
            // Get username from authentication if not provided
            if (username == null || username.trim().isEmpty()) {
                if (authentication != null) {
                    username = authentication.getName();
                } else {
                    logger.warn("❌ No username provided and no authentication found");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
                }
            }
            
            logger.info("🔵 Avatar update request - Username: {}, File: {}", username, file.getOriginalFilename());
            
            // Validate file
            if (file.isEmpty()) {
                logger.warn("❌ Empty file received");
                return ResponseEntity.badRequest().body("File is empty");
            }
            
            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                logger.warn("❌ File too large: {} bytes", file.getSize());
                return ResponseEntity.badRequest().body("File too large. Maximum size is 5MB");
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warn("❌ Invalid file type: {}", contentType);
                return ResponseEntity.badRequest().body("Invalid file type. Only images are allowed");
            }
            
            // Find customer by username
            Customer customer = customerService.findByAccountUserName(username);
            if (customer == null) {
                logger.warn("❌ Customer not found: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found");
            }
            
            String currentAvatarUrl = customer.getCustomerImg();
            logger.info("🔵 Current avatar URL: {}", currentAvatarUrl);
            
            logger.info("🔵 Starting Firebase upload (will auto-delete old avatar)...");
            
            // Upload new avatar to Firebase (automatically deletes old avatar)
            String newAvatarUrl = firebaseStorageService.uploadAvatar(file, username);
            
            if (newAvatarUrl == null || newAvatarUrl.isEmpty()) {
                logger.error("❌ Failed to upload avatar to Firebase");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to upload avatar");
            }
            
            logger.info("✅ Firebase upload successful: {}", newAvatarUrl);
            
            // Update customer avatar URL in database
            try {
                customerService.updateCustomerAvatar(username, newAvatarUrl);
                logger.info("✅ Database updated successfully with new avatar URL");
            } catch (Exception e) {
                logger.error("❌ Failed to update customer avatar in database: {}", e.getMessage());
                // Try to delete uploaded file from Firebase since DB update failed
                try {
                    firebaseStorageService.deleteFileByUrl(newAvatarUrl);
                } catch (Exception ex) {
                    logger.warn("⚠️ Failed to cleanup uploaded file after DB error: {}", ex.getMessage());
                }
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to update avatar in database");
            }
            
            // Note: Old avatar cleanup is now handled automatically by uploadAvatar method
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar updated successfully");
            response.put("avatarUrl", newAvatarUrl);
            response.put("timestamp", System.currentTimeMillis());
            
            logger.info("✅ Avatar update completed successfully for user: {}", username);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("❌ Error updating avatar for user {}: {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        try {
            String oldPassword = passwordData.get("oldPassword");
            String newPassword = passwordData.get("newPassword");
            
            if (oldPassword == null || newPassword == null || 
                oldPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Old password and new password are required");
            }
            
            // Enhanced password validation
            if (newPassword.length() < 8) {
                return ResponseEntity.badRequest().body("New password must be at least 8 characters long");
            }
            
            if (!newPassword.matches(".*[a-z].*")) {
                return ResponseEntity.badRequest().body("New password must contain at least one lowercase letter");
            }
            
            if (!newPassword.matches(".*[A-Z].*")) {
                return ResponseEntity.badRequest().body("New password must contain at least one uppercase letter");
            }
            
            if (!newPassword.matches(".*\\d.*")) {
                return ResponseEntity.badRequest().body("New password must contain at least one number");
            }
            
            if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                return ResponseEntity.badRequest().body("New password must contain at least one special character");
            }
            
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Verify old password
            if (!customerService.verifyCurrentPassword(username, oldPassword)) {
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
            
            // Update password
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            customerService.updatePassword(username, encodedNewPassword);
            
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to change password: " + e.getMessage());
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> profileData) {
        try {
            // Get current user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            // Validate required fields
            String email = profileData.get("customerEmail");
            String phone = profileData.get("customerPhone");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Phone number is required");
            }
            
            // Validate email format
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                return ResponseEntity.badRequest().body("Invalid email format");
            }
            
            // Validate phone format (10-11 digits)
            if (!phone.matches("^\\d{10,11}$")) {
                return ResponseEntity.badRequest().body("Phone number must be 10-11 digits");
            }
            
            // Validate date of birth if provided (must be >16 years old)
            String dateOfBirth = profileData.get("customerDateOfBirth");
            if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) {
                try {
                    java.time.LocalDate birthDate = java.time.LocalDate.parse(dateOfBirth);
                    java.time.LocalDate today = java.time.LocalDate.now();
                    int age = java.time.Period.between(birthDate, today).getYears();
                    
                    if (age < 16) {
                        return ResponseEntity.badRequest().body("You must be at least 16 years old");
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Invalid date format. Use YYYY-MM-DD");
                }
            }
            
            // Update profile
            customerService.updateCustomerProfile(username, profileData);
            
            return ResponseEntity.ok(new MessageResponse("Profile updated successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to update profile: " + e.getMessage());
        }
    }

    /**
     * Reset password with additional security checks
     */
    @PostMapping("/password/reset")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request, Authentication authentication) {
        try {
            String username = authentication.getName();
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String confirmPassword = request.get("confirmPassword");
            
            // Validate required fields
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Current password is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "New password is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (!newPassword.equals(confirmPassword)) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password confirmation does not match");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Validate password strength
            if (newPassword.length() < 8) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password must be at least 8 characters long");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (!newPassword.matches(".*[A-Z].*")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password must contain at least one uppercase letter");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (!newPassword.matches(".*[a-z].*")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password must contain at least one lowercase letter");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (!newPassword.matches(".*\\d.*")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password must contain at least one number");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            if (!newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Password must contain at least one special character");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Customer customer = customerService.findActiveCustomerByAccountUserName(username);
            if (customer == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Customer account not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Verify current password
            if (!passwordEncoder.matches(currentPassword, customer.getAccount().getPassword())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Current password is incorrect");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Verify new password is different from current
            if (passwordEncoder.matches(newPassword, customer.getAccount().getPassword())) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "New password must be different from current password");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Update password
            customer.getAccount().setPassword(passwordEncoder.encode(newPassword));
            customerService.saveCustomer(customer);

            Map<String, String> successResponse = new HashMap<>();
            successResponse.put("message", "Password reset successfully");
            return ResponseEntity.ok(successResponse);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to reset password: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
