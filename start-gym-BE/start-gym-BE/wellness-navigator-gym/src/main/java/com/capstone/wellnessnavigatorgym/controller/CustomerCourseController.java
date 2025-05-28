package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.course.SimpleCourseDto;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerCourse;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/customer-course")
@CrossOrigin(origins = "http://localhost:3000")
public class CustomerCourseController {

    @Autowired
    private ICustomerCourseService customerCourseService;
    
    @Autowired
    private ICustomerRepository customerRepository;
    
    @Autowired
    private IVipPaymentRepository vipPaymentRepository;

    @GetMapping("/recommended")
    public ResponseEntity<List<SimpleCourseDto>> getRecommendedCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<CustomerCourse> recommendedCourses = customerCourseService.findRecommendedCoursesByUsername(username);
        
        if (recommendedCourses == null || recommendedCourses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        
        List<SimpleCourseDto> simpleCourses = recommendedCourses.stream()
                .map(SimpleCourseDto::fromCustomerCourse)
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
        
        if (simpleCourses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        
        return new ResponseEntity<>(simpleCourses, HttpStatus.OK);
    }
    
    @GetMapping("/debug")
    public ResponseEntity<?> debugRecommendations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("username", username);
        
        try {
            // Get customer info
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer != null) {
                debugInfo.put("customerId", customer.getCustomerId());
                debugInfo.put("customerName", customer.getCustomerName());
                
                // Get customer courses
                List<CustomerCourse> allCourses = customerCourseService.findByCustomerId(customer.getCustomerId());
                debugInfo.put("totalCourses", allCourses.size());
                
                List<CustomerCourse> recommendedCourses = allCourses.stream()
                        .filter(cc -> cc.getRecommendedStatus() != null && cc.getRecommendedStatus())
                        .collect(Collectors.toList());
                debugInfo.put("recommendedCoursesCount", recommendedCourses.size());
                
                // Course details
                List<Map<String, Object>> courseDetails = recommendedCourses.stream()
                        .map(cc -> {
                            Map<String, Object> details = new HashMap<>();
                            if (cc.getCourse() != null) {
                                details.put("courseId", cc.getCourse().getCourseId());
                                details.put("courseName", cc.getCourse().getCourseName());
                                details.put("description", cc.getCourse().getDescription());
                                details.put("duration", cc.getCourse().getDuration());
                                details.put("recommendedStatus", cc.getRecommendedStatus());
                            }
                            return details;
                        })
                        .collect(Collectors.toList());
                debugInfo.put("courseDetails", courseDetails);
            } else {
                debugInfo.put("error", "Customer not found for username: " + username);
            }
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/add-recommended/{courseId}")
    public ResponseEntity<?> addRecommendedCourse(@PathVariable Integer courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            // Get customer info
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found for username: " + username);
            }
            
            // Find course ID
            customerCourseService.saveRecommendedCourseById(courseId, customer.getCustomerId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully added recommended course ID " + courseId + " for customer " + customer.getCustomerName());
            response.put("courseId", courseId);
            response.put("customerId", customer.getCustomerId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error adding recommended course: " + e.getMessage());
        }
    }

    @PostMapping("/join/{courseId}")
    public ResponseEntity<?> joinCourse(@PathVariable Integer courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        System.out.println("=== JOIN COURSE DEBUG ===");
        System.out.println("Course ID: " + courseId);
        System.out.println("Authentication: " + authentication);
        System.out.println("Username: " + username);
        System.out.println("Is authenticated: " + authentication.isAuthenticated());
        System.out.println("Authorities: " + authentication.getAuthorities());
        
        try {
            // Debug: Check if account exists first
            System.out.println("=== CHECKING ACCOUNT ===");
            Optional<com.capstone.wellnessnavigatorgym.entity.Account> accountOpt = 
                customerRepository.findById(1).map(c -> c.getAccount()); // Just to test repository
            System.out.println("Repository working: " + (accountOpt != null));
            
            // Get customer info with detailed logging
            System.out.println("=== LOOKING FOR CUSTOMER ===");
            System.out.println("Searching for customer with username: '" + username + "'");
            Customer customer = customerRepository.findByAccountUserName(username);
            System.out.println("Customer found: " + (customer != null ? customer.getCustomerName() : "null"));
            
            if (customer == null) {
                // Additional debugging - check if account exists
                System.out.println("=== ACCOUNT DEBUG ===");
                try {
                    // Check all customers to see what usernames exist
                    List<Customer> allCustomers = customerRepository.findAll();
                    System.out.println("Total customers in database: " + allCustomers.size());
                    for (Customer c : allCustomers) {
                        if (c.getAccount() != null) {
                            System.out.println("Customer: " + c.getCustomerName() + 
                                " -> Account username: '" + c.getAccount().getUserName() + "'");
                        } else {
                            System.out.println("Customer: " + c.getCustomerName() + " -> No account");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error checking customers: " + e.getMessage());
                }
                
                Map<String, String> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("message", "Please login to join courses");
                System.out.println("Customer not found for username: " + username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Check if customer already joined this course
            boolean alreadyJoined = customerCourseService.isCustomerEnrolledInCourse(customer.getCustomerId(), courseId);
            System.out.println("Already joined: " + alreadyJoined);
            
            if (alreadyJoined) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Already enrolled");
                response.put("message", "You have already joined this course");
                response.put("courseId", courseId);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Add course for customer (not recommended, user choice)
            customerCourseService.addCourseForCustomer(courseId, customer.getCustomerId());
            System.out.println("Course added successfully for customer: " + customer.getCustomerName());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Successfully joined the course!");
            response.put("courseId", courseId);
            response.put("customerId", customer.getCustomerId());
            response.put("customerName", customer.getCustomerName());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid course");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "Error joining course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/my-courses")
    public ResponseEntity<?> getMyCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Customer not found for username: " + username);
            }
            
            // Lấy courses từ customer_course table (recommended + self-enrolled)
            List<CustomerCourse> allCourses = customerCourseService.findByCustomerId(customer.getCustomerId());
            
            // Lấy VIP courses đã mua thành công
            List<VipPayment> successfulVipPayments = vipPaymentRepository.findByCustomer_CustomerIdAndPaymentStatusOrderByCreatedDateDesc(
                customer.getCustomerId(), VipPayment.PaymentStatus.SUCCESS);
            
            // Set để track các course đã có trong customer_course
            Set<Integer> existingCourseIds = allCourses.stream()
                .map(cc -> cc.getCourse().getCourseId())
                .collect(Collectors.toSet());
            
            // Danh sách cuối cùng các courses
            List<Map<String, Object>> coursesDetails = new ArrayList<>();
            
            // Thêm courses từ customer_course table
            coursesDetails.addAll(allCourses.stream()
                .map(cc -> {
                    Map<String, Object> details = new HashMap<>();
                    if (cc.getCourse() != null) {
                        details.put("courseId", cc.getCourse().getCourseId());
                        details.put("courseName", cc.getCourse().getCourseName());
                        details.put("description", cc.getCourse().getDescription());
                        details.put("duration", cc.getCourse().getDuration());
                        details.put("image", cc.getCourse().getImage());
                        details.put("isRecommended", cc.getRecommendedStatus() != null && cc.getRecommendedStatus());
                        details.put("joinType", cc.getRecommendedStatus() != null && cc.getRecommendedStatus() ? "Recommended" : "Self-enrolled");
                        
                        // Kiểm tra xem có phải VIP course đã mua không
                        boolean isVipPurchased = successfulVipPayments.stream()
                            .anyMatch(vip -> vip.getCourse().getCourseId().equals(cc.getCourse().getCourseId()));
                        
                        // Đánh dấu isVip nếu course này là VIP và đã mua
                        details.put("isVip", isVipPurchased);
                        details.put("price", cc.getCourse().getPrice());
                        
                        // Thêm thông tin VIP course nếu đã mua
                        if (isVipPurchased) {
                            details.put("joinType", "VIP Purchased");
                        }
                    }
                    return details;
                })
                .collect(Collectors.toList()));
            
            // Thêm VIP courses đã mua nhưng chưa có trong customer_course
            successfulVipPayments.stream()
                .filter(vip -> !existingCourseIds.contains(vip.getCourse().getCourseId()))
                .forEach(vip -> {
                    Map<String, Object> details = new HashMap<>();
                    details.put("courseId", vip.getCourse().getCourseId());
                    details.put("courseName", vip.getCourse().getCourseName());
                    details.put("description", vip.getCourse().getDescription());
                    details.put("duration", vip.getCourse().getDuration());
                    details.put("image", vip.getCourse().getImage());
                    details.put("isRecommended", false);
                    details.put("joinType", "VIP Purchased");
                    details.put("isVip", true);
                    details.put("price", vip.getAmount());
                    details.put("purchaseDate", vip.getCreatedDate());
                    
                    coursesDetails.add(details);
                });
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerName", customer.getCustomerName());
            response.put("totalCourses", coursesDetails.size());
            response.put("courses", coursesDetails);
            response.put("vipCoursesCount", successfulVipPayments.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error getting courses: " + e.getMessage());
        }
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> deleteCourseFromCustomer(@PathVariable Integer courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        try {
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("message", "Please login to delete courses");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            // Check if customer is enrolled in this course
            boolean isEnrolled = customerCourseService.isCustomerEnrolledInCourse(customer.getCustomerId(), courseId);
            if (!isEnrolled) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Course not found");
                error.put("message", "You are not enrolled in this course");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Delete the course enrollment
            customerCourseService.deleteCourseFromCustomer(courseId, customer.getCustomerId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Course successfully removed from your list!");
            response.put("courseId", courseId);
            response.put("customerId", customer.getCustomerId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "Error removing course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/debug-customer/{username}")
    public ResponseEntity<?> debugCustomer(@PathVariable String username) {
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("username", username);
        
        try {
            // Check account
            System.out.println("=== DEBUG CUSTOMER ENDPOINT ===");
            System.out.println("Looking for username: " + username);
            
            // Try to find customer
            Customer customer = customerRepository.findByAccountUserName(username);
            debugInfo.put("customerFound", customer != null);
            
            if (customer != null) {
                debugInfo.put("customerId", customer.getCustomerId());
                debugInfo.put("customerName", customer.getCustomerName());
                debugInfo.put("customerEmail", customer.getCustomerEmail());
                debugInfo.put("isEnabled", customer.getIsEnable());
                
                if (customer.getAccount() != null) {
                    debugInfo.put("accountId", customer.getAccount().getAccountId());
                    debugInfo.put("accountUsername", customer.getAccount().getUserName());
                    debugInfo.put("accountEmail", customer.getAccount().getEmail());
                    debugInfo.put("accountEnabled", customer.getAccount().getIsEnable());
                } else {
                    debugInfo.put("account", "null");
                }
            } else {
                // List all customers and their accounts
                List<Customer> allCustomers = customerRepository.findAll();
                debugInfo.put("totalCustomers", allCustomers.size());
                
                List<Map<String, Object>> customerList = new ArrayList<>();
                for (Customer c : allCustomers) {
                    Map<String, Object> customerInfo = new HashMap<>();
                    customerInfo.put("customerId", c.getCustomerId());
                    customerInfo.put("customerName", c.getCustomerName());
                    customerInfo.put("customerEnabled", c.getIsEnable());
                    
                    if (c.getAccount() != null) {
                        customerInfo.put("accountUsername", c.getAccount().getUserName());
                        customerInfo.put("accountEmail", c.getAccount().getEmail());
                        customerInfo.put("accountEnabled", c.getAccount().getIsEnable());
                    } else {
                        customerInfo.put("account", "null");
                    }
                    customerList.add(customerInfo);
                }
                debugInfo.put("allCustomers", customerList);
            }
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            e.printStackTrace();
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/fix-vanba-account")
    public ResponseEntity<?> fixVanbaAccount() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Find customer "Văn Ba" (ID = 3)
            Optional<Customer> customerOpt = customerRepository.findById(3);
            if (!customerOpt.isPresent()) {
                result.put("error", "Customer with ID 3 not found");
                return ResponseEntity.badRequest().body(result);
            }
            
            Customer customer = customerOpt.get();
            
            // Find account "vanba" from existing customers
            List<Customer> allCustomers = customerRepository.findAll();
            com.capstone.wellnessnavigatorgym.entity.Account vanbaAccount = null;
            
            // Check if there's already an account with username "vanba"
            for (Customer c : allCustomers) {
                if (c.getAccount() != null && "vanba".equals(c.getAccount().getUserName())) {
                    vanbaAccount = c.getAccount();
                    // Remove this account from the current customer to avoid conflict
                    c.setAccount(null);
                    customerRepository.save(c);
                    break;
                }
            }
            
            if (vanbaAccount != null) {
                // Link the existing account to customer "Văn Ba"
                customer.setAccount(vanbaAccount);
                customerRepository.save(customer);
                
                result.put("success", true);
                result.put("message", "Successfully linked existing 'vanba' account to customer 'Văn Ba'");
                result.put("customerId", customer.getCustomerId());
                result.put("customerName", customer.getCustomerName());
                result.put("accountUsername", vanbaAccount.getUserName());
            } else {
                result.put("error", "No account with username 'vanba' found to link");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @PostMapping("/create-account-for-customer/{customerId}")
    public ResponseEntity<?> createAccountForCustomer(@PathVariable Integer customerId, 
                                                     @RequestParam String username,
                                                     @RequestParam String email,
                                                     @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        result.put("customerId", customerId);
        result.put("username", username);
        
        try {
            // Find the customer
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (!customerOpt.isPresent()) {
                result.put("error", "Customer not found with ID: " + customerId);
                return ResponseEntity.badRequest().body(result);
            }
            
            Customer customer = customerOpt.get();
            
            if (customer.getAccount() != null) {
                result.put("error", "Customer already has an account");
                return ResponseEntity.badRequest().body(result);
            }
            
            // Create new account
            com.capstone.wellnessnavigatorgym.entity.Account newAccount = 
                new com.capstone.wellnessnavigatorgym.entity.Account();
            newAccount.setUserName(username);
            newAccount.setEmail(email);
            newAccount.setEncryptPassword(password); // Should be encoded in real scenario
            newAccount.setIsEnable(true);
            
            // Set default USER role
            com.capstone.wellnessnavigatorgym.entity.Role userRole = 
                new com.capstone.wellnessnavigatorgym.entity.Role(1, "ROLE_USER");
            Set<com.capstone.wellnessnavigatorgym.entity.Role> roles = new HashSet<>();
            roles.add(userRole);
            newAccount.setRoles(roles);
            
            // Save account first
            // Note: We need AccountRepository here, but for quick fix, let's use a different approach
            
            // Link account to customer
            customer.setAccount(newAccount);
            customerRepository.save(customer);
            
            result.put("success", true);
            result.put("message", "Successfully created account for customer: " + customer.getCustomerName());
            result.put("customerName", customer.getCustomerName());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }

    @GetMapping("/debug-my-courses")
    public ResponseEntity<?> debugMyCourses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Map<String, Object> debug = new HashMap<>();
        debug.put("username", username);
        
        try {
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                debug.put("error", "Customer not found for username: " + username);
                return ResponseEntity.ok(debug);
            }
            
            debug.put("customerId", customer.getCustomerId());
            debug.put("customerName", customer.getCustomerName());
            
            // Lấy courses từ customer_course table
            List<CustomerCourse> allCourses = customerCourseService.findByCustomerId(customer.getCustomerId());
            debug.put("totalCoursesInCustomerCourse", allCourses.size());
            
            // Lấy VIP courses đã mua thành công
            List<VipPayment> successfulVipPayments = vipPaymentRepository.findByCustomer_CustomerIdAndPaymentStatusOrderByCreatedDateDesc(
                customer.getCustomerId(), VipPayment.PaymentStatus.SUCCESS);
            debug.put("totalSuccessfulVipPayments", successfulVipPayments.size());
            
            // Debug VIP payments
            List<Map<String, Object>> vipPaymentDetails = successfulVipPayments.stream().map(vip -> {
                Map<String, Object> details = new HashMap<>();
                details.put("paymentId", vip.getPaymentId());
                details.put("courseId", vip.getCourse().getCourseId());
                details.put("courseName", vip.getCourse().getCourseName());
                details.put("amount", vip.getAmount());
                details.put("createdDate", vip.getCreatedDate());
                return details;
            }).collect(Collectors.toList());
            debug.put("vipPaymentDetails", vipPaymentDetails);
            
            // Debug courses in customer_course
            List<Map<String, Object>> courseDetails = allCourses.stream().map(cc -> {
                Map<String, Object> details = new HashMap<>();
                if (cc.getCourse() != null) {
                    details.put("courseId", cc.getCourse().getCourseId());
                    details.put("courseName", cc.getCourse().getCourseName());
                    details.put("isRecommended", cc.getRecommendedStatus());
                    details.put("isVipCourse", cc.getCourse().getIsVip());
                    
                    // Check if this course has successful VIP payment
                    boolean hasVipPayment = successfulVipPayments.stream()
                        .anyMatch(vip -> vip.getCourse().getCourseId().equals(cc.getCourse().getCourseId()));
                    details.put("hasVipPayment", hasVipPayment);
                }
                return details;
            }).collect(Collectors.toList());
            debug.put("courseDetails", courseDetails);
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            debug.put("error", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(debug);
        }
    }
} 