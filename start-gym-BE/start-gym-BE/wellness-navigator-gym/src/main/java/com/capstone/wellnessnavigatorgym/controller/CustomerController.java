package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.customer.CustomerInfo;
import com.capstone.wellnessnavigatorgym.dto.customer.CustomerUserDetailDto;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import com.capstone.wellnessnavigatorgym.utils.ConverterMaxCode;
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

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @PostMapping("/upload-avatar")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            if (file.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Please select a file to upload");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // File size validation (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "File size exceeds 5MB limit");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // File type validation
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Only image files are allowed");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String username = authentication.getName();
            
            // Create upload directory if it doesn't exist
            String uploadDir = "uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String newFileName = "avatar_" + username + "_" + System.currentTimeMillis() + fileExtension;
            
            // Save file
            Path filePath = uploadPath.resolve(newFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update customer avatar in database
            String avatarUrl = "/uploads/avatars/" + newFileName;
            customerService.updateCustomerAvatar(username, avatarUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("avatarUrl", avatarUrl);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to upload avatar: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
