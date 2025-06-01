package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.entity.Comment;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CourseType;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerTypeRepository;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICommentRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseTypeRepository;
import com.capstone.wellnessnavigatorgym.service.PaymentStatusService;
import com.capstone.wellnessnavigatorgym.service.FirebaseStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private PaymentStatusService paymentStatusService;
    
    @Autowired
    private IVipPaymentRepository vipPaymentRepository;
    
    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ICustomerTypeRepository customerTypeRepository;

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICommentRepository commentRepository;

    @Autowired
    private ICourseTypeRepository courseTypeRepository;

    @Autowired
    private FirebaseStorageService firebaseStorageService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Test endpoint đơn giản
     */
    @GetMapping("/test-connection")
    public ResponseEntity<?> testConnection() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Connection successful");
        response.put("timestamp", LocalDateTime.now());
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard chính cho admin
     */
    @GetMapping("/dashboard-overview")
    public ResponseEntity<?> getDashboard() {
        try {
            System.out.println("=== DASHBOARD-OVERVIEW ENDPOINT CALLED ===");
            Map<String, Object> response = new HashMap<>();
            
            // Thống kê người dùng
            List<Customer> allCustomers = customerRepository.findAll().stream()
                .filter(c -> c.getIsEnable())
                .collect(Collectors.toList());
            
            System.out.println("Total customers found: " + allCustomers.size());
            
            // Tự động lấy tất cả customer types từ database
            Map<String, Long> userTypeStats = allCustomers.stream()
                .filter(c -> c.getCustomerType() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getCustomerType().getCustomerTypeName(),
                    Collectors.counting()
                ));
            
            // Lấy tất cả customer types từ database để đảm bảo hiển thị đầy đủ
            List<CustomerType> allCustomerTypes = customerTypeRepository.findAllOrderByPriceAsc();
            
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("total", (long) allCustomers.size());
            
            // Thêm thống kê cho tất cả customer types có trong database
            Map<String, Long> customerTypeBreakdown = new HashMap<>();
            for (CustomerType type : allCustomerTypes) {
                String typeName = type.getCustomerTypeName();
                Long count = userTypeStats.getOrDefault(typeName, 0L);
                customerTypeBreakdown.put(typeName.toLowerCase().replace(" ", "_"), count);
                System.out.println("Customer type: " + typeName + " - Count: " + count);
            }
            
            // Backward compatibility - keep old field names
            userStats.put("regular", customerTypeBreakdown.getOrDefault("regular", 0L));
            userStats.put("premium", customerTypeBreakdown.getOrDefault("premium", 0L));
            userStats.put("vip", customerTypeBreakdown.getOrDefault("vip", 0L));
            
            // Add new dynamic customer types
            userStats.put("customerTypeBreakdown", customerTypeBreakdown);
            userStats.put("allCustomerTypes", allCustomerTypes.stream()
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("id", type.getCustomerTypeId());
                    typeInfo.put("name", type.getCustomerTypeName());
                    typeInfo.put("count", userTypeStats.getOrDefault(type.getCustomerTypeName(), 0L));
                    typeInfo.put("price", type.getPrice());
                    return typeInfo;
                })
                .collect(Collectors.toList()));
            
            response.put("userStats", userStats);
            
            // Thống kê khóa học - sử dụng dữ liệu thực từ database
            Long totalCourses = courseRepository.count();
            Long activeCourses = courseRepository.countActiveCourses();
            Long hiddenCourses = courseRepository.countHiddenCourses();
            
            System.out.println("Course stats - Total: " + totalCourses + ", Active: " + activeCourses + ", Hidden: " + hiddenCourses);
            
            Map<String, Object> courseStats = new HashMap<>();
            courseStats.put("active", activeCourses != null ? activeCourses : 0L);
            courseStats.put("hidden", hiddenCourses != null ? hiddenCourses : 0L);
            courseStats.put("total", totalCourses != null ? totalCourses : 0L);
            courseStats.put("newThisWeek", 2L); // Placeholder - có thể implement sau nếu có trường created_date
            courseStats.put("newThisMonth", 8L); // Placeholder - có thể implement sau nếu có trường created_date
            
            response.put("courseStats", courseStats);
            
            // Thống kê đăng ký (không có createDate trong Customer entity)
            Map<String, Object> registrationStats = new HashMap<>();
            registrationStats.put("today", 0L); // Tạm thời set 0
            registrationStats.put("thisWeek", 0L); // Tạm thời set 0  
            registrationStats.put("thisMonth", 0L); // Tạm thời set 0
            
            response.put("registrationStats", registrationStats);
            
            // Thống kê doanh thu
            List<VipPayment> allPayments = vipPaymentRepository.findAll();
            System.out.println("Total payments found: " + allPayments.size());
            
            List<VipPayment> successPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS)
                .collect(Collectors.toList());
            
            System.out.println("Success payments found: " + successPayments.size());
            
            BigDecimal totalRevenue = successPayments.stream()
                .map(VipPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Tính doanh thu theo loại customer
            BigDecimal vipRevenue = successPayments.stream()
                .filter(p -> p.getCustomer() != null && p.getCustomer().getCustomerType() != null && 
                           "VIP".equals(p.getCustomer().getCustomerType().getCustomerTypeName()))
                .map(VipPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal premiumRevenue = successPayments.stream()
                .filter(p -> p.getCustomer() != null && p.getCustomer().getCustomerType() != null && 
                           "Premium".equals(p.getCustomer().getCustomerType().getCustomerTypeName()))
                .map(VipPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> revenueStats = new HashMap<>();
            revenueStats.put("total", totalRevenue.longValue());
            revenueStats.put("vip", vipRevenue.longValue());
            revenueStats.put("premium", premiumRevenue.longValue());
            
            response.put("revenueStats", revenueStats);
            
            // Thống kê đơn hàng
            long pendingOrders = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.PENDING)
                .count();
            
            long successOrders = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS)
                .count();
            
            long failedOrders = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.FAILED)
                .count();
            
            Map<String, Object> orderStats = new HashMap<>();
            orderStats.put("pending", pendingOrders);
            orderStats.put("success", successOrders);
            orderStats.put("failed", failedOrders);
            orderStats.put("total", (long) allPayments.size());
            
            response.put("orderStats", orderStats);
            
            // Hoạt động gần đây - sửa lỗi null pointer
            List<Map<String, Object>> recentActivities = allPayments.stream()
                .filter(payment -> payment.getCustomer() != null) // Lọc payments có customer
                .sorted((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()))
                .limit(10)
                .map(payment -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", payment.getPaymentId());
                    activity.put("userName", payment.getCustomer() != null ? 
                        payment.getCustomer().getCustomerName() : "Unknown Customer");
                    
                    // Kiểm tra course null
                    String description = "Course Payment";
                    if (payment.getCourse() != null && payment.getCourse().getCourseName() != null) {
                        description = payment.getCourse().getCourseName();
                    }
                    activity.put("description", description);
                    
                    activity.put("amount", payment.getAmount().longValue());
                    activity.put("status", payment.getPaymentStatus().name());
                    activity.put("createdAt", payment.getCreatedDate());
                    return activity;
                })
                .collect(Collectors.toList());
            
            response.put("recentActivities", recentActivities);
            
            // Thêm dữ liệu cho monthly chart
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
            for (int i = 0; i < months.length; i++) {
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", months[i]);
                monthData.put("users", 15 + (i * 3)); // Dữ liệu mẫu tăng dần
                monthData.put("revenue", 2.5 + (i * 0.5)); // Dữ liệu doanh thu tăng dần
                monthlyData.add(monthData);
            }
            response.put("monthlyData", monthlyData);
            
            response.put("timestamp", LocalDateTime.now());
            
            System.out.println("Dashboard data prepared successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error in dashboard-overview: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting dashboard data: " + e.getMessage());
        }
    }

    /**
     * Dashboard chính cho admin quản lý thanh toán
     */
    @GetMapping("/payment-management")
    public ResponseEntity<?> getPaymentManagement() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Thống kê tổng quan
            List<VipPayment> allPayments = vipPaymentRepository.findAll();
            long totalPayments = allPayments.size();
            long successPayments = allPayments.stream().mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS ? 1 : 0).sum();
            long failedPayments = allPayments.stream().mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.FAILED ? 1 : 0).sum();
            long pendingPayments = allPayments.stream().mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.PENDING ? 1 : 0).sum();
            
            // Payments quá hạn
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            List<VipPayment> expiredPayments = vipPaymentRepository.findExpiredPendingPayments(fifteenMinutesAgo);
            
            Map<String, Object> overview = new HashMap<>();
            overview.put("totalPayments", totalPayments);
            overview.put("successPayments", successPayments);
            overview.put("failedPayments", failedPayments);
            overview.put("pendingPayments", pendingPayments);
            overview.put("expiredPayments", expiredPayments.size());
            overview.put("successRate", totalPayments > 0 ? (double) successPayments / totalPayments * 100 : 0);
            
            response.put("overview", overview);
            
            // Danh sách payments cần xử lý
            List<Map<String, Object>> urgentPayments = expiredPayments.stream()
                .limit(10)
                .map(payment -> {
                    Map<String, Object> paymentInfo = new HashMap<>();
                    paymentInfo.put("paymentId", payment.getPaymentId());
                    paymentInfo.put("vnpTxnRef", payment.getVnpTxnRef());
                    paymentInfo.put("customerName", payment.getCustomer().getCustomerName());
                    paymentInfo.put("courseName", payment.getCourse().getCourseName());
                    paymentInfo.put("amount", payment.getAmount());
                    paymentInfo.put("createdDate", payment.getCreatedDate());
                    paymentInfo.put("minutesExpired", java.time.Duration.between(payment.getCreatedDate(), LocalDateTime.now()).toMinutes());
                    return paymentInfo;
                })
                .collect(Collectors.toList());
                
            response.put("urgentPayments", urgentPayments);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting payment management data: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê customer types
     */
    @GetMapping("/customer-type-stats")
    public ResponseEntity<?> getCustomerTypeStats() {
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            
            Map<String, Long> typeStats = allCustomers.stream()
                .filter(c -> c.getIsEnable() && c.getCustomerType() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getCustomerType().getCustomerTypeName(),
                    Collectors.counting()
                ));
            
            // Lấy thông tin về customer type thresholds từ database
            List<PaymentStatusService.CustomerTypeThreshold> thresholds = paymentStatusService.getCustomerTypeThresholds();
            
            // Thống kê theo total paid amount
            List<Map<String, Object>> customerDetails = allCustomers.stream()
                .filter(c -> c.getIsEnable() && c.getAccount() != null)
                .limit(20) // Top 20 customers
                .map(customer -> {
                    Map<String, Object> customerInfo = new HashMap<>();
                    customerInfo.put("customerId", customer.getCustomerId());
                    customerInfo.put("customerName", customer.getCustomerName());
                    customerInfo.put("currentType", customer.getCustomerType() != null ? 
                        customer.getCustomerType().getCustomerTypeName() : "No Type");
                    customerInfo.put("currentTypeId", customer.getCustomerType() != null ? 
                        customer.getCustomerType().getCustomerTypeId() : null);
                    
                    BigDecimal totalPaid = paymentStatusService.getTotalPaidAmount(customer.getCustomerId());
                    customerInfo.put("totalPaid", totalPaid);
                    
                    // Suggested type based on paid amount using database thresholds
                    CustomerType suggestedType = paymentStatusService.determineCustomerType(totalPaid);
                    if (suggestedType != null) {
                        customerInfo.put("suggestedType", suggestedType.getCustomerTypeName());
                        customerInfo.put("suggestedTypeId", suggestedType.getCustomerTypeId());
                        
                        // Kiểm tra xem có cần update không
                        boolean needsUpdate = customer.getCustomerType() == null || 
                            !customer.getCustomerType().getCustomerTypeId().equals(suggestedType.getCustomerTypeId());
                        customerInfo.put("needsUpdate", needsUpdate);
                    } else {
                        customerInfo.put("suggestedType", "Unknown");
                        customerInfo.put("needsUpdate", false);
                    }
                    
                    return customerInfo;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("typeStats", typeStats);
            response.put("customerDetails", customerDetails);
            response.put("totalCustomers", allCustomers.stream().filter(c -> c.getIsEnable()).count());
            response.put("thresholds", thresholds);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting customer type stats: " + e.getMessage());
        }
    }

    /**
     * Cập nhật payment status theo payment ID
     */
    @PostMapping("/update-payment-status/{paymentId}")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam String newStatus) {
        try {
            VipPayment payment = vipPaymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            VipPayment.PaymentStatus oldStatus = payment.getPaymentStatus();
            VipPayment.PaymentStatus status = VipPayment.PaymentStatus.valueOf(newStatus.toUpperCase());
            
            payment.setPaymentStatus(status);
            payment.setUpdatedDate(LocalDateTime.now());
            
            if (status == VipPayment.PaymentStatus.SUCCESS) {
                payment.setCompletedDate(LocalDateTime.now());
                payment.setVnpResponseCode("00");
            } else if (status == VipPayment.PaymentStatus.FAILED) {
                payment.setVnpResponseCode("ADMIN_UPDATE");
            }
            
            vipPaymentRepository.save(payment);
            
            // Cập nhật customer type nếu thanh toán thành công
            if (status == VipPayment.PaymentStatus.SUCCESS) {
                paymentStatusService.updateCustomerTypeBasedOnPayments(payment.getCustomer().getCustomerId());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment status updated successfully");
            response.put("paymentId", paymentId);
            response.put("oldStatus", oldStatus);
            response.put("newStatus", status);
            response.put("updatedAt", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating payment status: " + e.getMessage());
        }
    }

    /**
     * System health check
     */
    @GetMapping("/system-health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = new HashMap<>();
            
            // Database connections
            long totalPayments = vipPaymentRepository.count();
            long totalCustomers = customerRepository.count();
            
            health.put("database", "OK");
            health.put("totalPayments", totalPayments);
            health.put("totalCustomers", totalCustomers);
            health.put("scheduledTasks", "ENABLED");
            health.put("timestamp", LocalDateTime.now());
            health.put("uptime", "System running normally");
            
            // Recent activity
            List<VipPayment> recentPayments = vipPaymentRepository.findAll().stream()
                .sorted((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()))
                .limit(5)
                .collect(Collectors.toList());
                
            List<String> recentActivity = recentPayments.stream()
                .map(p -> String.format("Payment %d - %s - %s", 
                    p.getPaymentId(), p.getPaymentStatus(), 
                    p.getCreatedDate().format(DateTimeFormatter.ofPattern("HH:mm:ss"))))
                .collect(Collectors.toList());
                
            health.put("recentActivity", recentActivity);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("database", "ERROR");
            error.put("error", e.getMessage());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Lấy thông tin customer type thresholds
     */
    @GetMapping("/customer-type-thresholds")
    public ResponseEntity<?> getCustomerTypeThresholds() {
        try {
            List<PaymentStatusService.CustomerTypeThreshold> thresholds = paymentStatusService.getCustomerTypeThresholds();
            
            Map<String, Object> response = new HashMap<>();
            response.put("thresholds", thresholds);
            response.put("message", "Customer type thresholds retrieved from database");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting customer type thresholds: " + e.getMessage());
        }
    }
    
    // ==================== CUSTOMER TYPE CRUD OPERATIONS ====================
    
    /**
     * Lấy tất cả customer types
     */
    @GetMapping("/customer-types")
    public ResponseEntity<?> getAllCustomerTypes() {
        try {
            List<CustomerType> customerTypes = customerTypeRepository.findAllOrderByPriceAsc();
            
            // Thêm thống kê số lượng customers cho mỗi type
            List<Map<String, Object>> customerTypesWithStats = customerTypes.stream()
                .map(type -> {
                    Map<String, Object> typeInfo = new HashMap<>();
                    typeInfo.put("customerTypeId", type.getCustomerTypeId());
                    typeInfo.put("customerTypeName", type.getCustomerTypeName());
                    typeInfo.put("price", type.getPrice());
                    
                    // Đếm số customers có type này
                    long customerCount = customerRepository.findAll().stream()
                        .filter(c -> c.getIsEnable() && c.getCustomerType() != null)
                        .filter(c -> c.getCustomerType().getCustomerTypeId().equals(type.getCustomerTypeId()))
                        .count();
                    typeInfo.put("customerCount", customerCount);
                    
                    return typeInfo;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerTypes", customerTypesWithStats);
            response.put("totalTypes", customerTypes.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting customer types: " + e.getMessage());
        }
    }
    
    /**
     * Lấy customer type theo ID
     */
    @GetMapping("/customer-types/{id}")
    public ResponseEntity<?> getCustomerTypeById(@PathVariable Integer id) {
        try {
            CustomerType customerType = customerTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer type not found with ID: " + id));
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", customerType);
            response.put("message", "Customer type retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error getting customer type: " + e.getMessage());
        }
    }
    
    /**
     * Tạo customer type mới
     */
    @PostMapping("/customer-types")
    public ResponseEntity<?> createCustomerType(@RequestBody Map<String, Object> request) {
        try {
            String customerTypeName = (String) request.get("customerTypeName");
            Double price = request.get("price") != null ? 
                Double.valueOf(request.get("price").toString()) : 0.0;
            
            // Validation
            if (customerTypeName == null || customerTypeName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Customer type name is required");
            }
            
            if (price < 0) {
                return ResponseEntity.badRequest().body("Price cannot be negative");
            }
            
            // Kiểm tra tên đã tồn tại chưa
            CustomerType existingType = customerTypeRepository.findByCustomerTypeName(customerTypeName);
            if (existingType != null) {
                return ResponseEntity.badRequest().body("Customer type name already exists");
            }
            
            CustomerType newCustomerType = new CustomerType();
            newCustomerType.setCustomerTypeName(customerTypeName.trim());
            newCustomerType.setPrice(price);
            
            CustomerType savedType = customerTypeRepository.save(newCustomerType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", savedType);
            response.put("message", "Customer type created successfully");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating customer type: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật customer type
     */
    @PutMapping("/customer-types/{id}")
    public ResponseEntity<?> updateCustomerType(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            CustomerType existingType = customerTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer type not found with ID: " + id));
            
            String customerTypeName = (String) request.get("customerTypeName");
            Double price = request.get("price") != null ? 
                Double.valueOf(request.get("price").toString()) : existingType.getPrice();
            
            // Validation
            if (customerTypeName != null && !customerTypeName.trim().isEmpty()) {
                // Kiểm tra tên mới có trùng với type khác không
                CustomerType typeWithSameName = customerTypeRepository.findByCustomerTypeName(customerTypeName);
                if (typeWithSameName != null && !typeWithSameName.getCustomerTypeId().equals(id)) {
                    return ResponseEntity.badRequest().body("Customer type name already exists");
                }
                existingType.setCustomerTypeName(customerTypeName.trim());
            }
            
            if (price < 0) {
                return ResponseEntity.badRequest().body("Price cannot be negative");
            }
            existingType.setPrice(price);
            
            CustomerType updatedType = customerTypeRepository.save(existingType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", updatedType);
            response.put("message", "Customer type updated successfully");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating customer type: " + e.getMessage());
        }
    }
    
    /**
     * Xóa customer type (chỉ cho phép nếu không có customer nào sử dụng)
     */
    @DeleteMapping("/customer-types/{id}")
    public ResponseEntity<?> deleteCustomerType(@PathVariable Integer id) {
        try {
            CustomerType customerType = customerTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer type not found with ID: " + id));
            
            // Kiểm tra xem có customer nào đang sử dụng type này không
            long customersUsingType = customerRepository.findAll().stream()
                .filter(c -> c.getCustomerType() != null)
                .filter(c -> c.getCustomerType().getCustomerTypeId().equals(id))
                .count();
            
            if (customersUsingType > 0) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Cannot delete customer type");
                error.put("message", "There are " + customersUsingType + " customers using this type");
                error.put("customersCount", customersUsingType);
                return ResponseEntity.badRequest().body(error);
            }
            
            customerTypeRepository.delete(customerType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer type deleted successfully");
            response.put("deletedType", customerType);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting customer type: " + e.getMessage());
        }
    }

    /**
     * Cập nhật tất cả customer types dựa trên total payments
     */
    @PostMapping("/update-all-customer-types")
    public ResponseEntity<?> updateAllCustomerTypes() {
        try {
            // Thực hiện cập nhật thủ công
            paymentStatusService.updateAllCustomerTypes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All customer types update process initiated");
            response.put("timestamp", LocalDateTime.now());
            response.put("note", "Check logs for detailed results of customer type updates");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating all customer types: " + e.getMessage());
        }
    }

    // ==================== COURSE MANAGEMENT ENDPOINTS ====================
    
    /**
     * Lấy tất cả courses để debug
     */
    @GetMapping("/courses")
    public ResponseEntity<?> getAllCoursesAdmin() {
        try {
            List<Course> courses = courseRepository.findAll();
            
            // Convert to response format
            List<Map<String, Object>> coursesResponse = courses.stream()
                .map(course -> {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("courseId", course.getCourseId());
                    courseInfo.put("courseName", course.getCourseName());
                    courseInfo.put("description", course.getDescription());
                    courseInfo.put("duration", course.getDuration());
                    courseInfo.put("image", course.getImage());
                    courseInfo.put("status", course.getStatus());
                    courseInfo.put("averageRating", course.getAverageRating());
                    
                    // Course type info
                    if (course.getCourseType() != null) {
                        Map<String, Object> typeInfo = new HashMap<>();
                        typeInfo.put("courseTypeId", course.getCourseType().getCourseTypeId());
                        typeInfo.put("courseTypeName", course.getCourseType().getCourseTypeName());
                        courseInfo.put("courseType", typeInfo);
                    }
                    
                    return courseInfo;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("courses", coursesResponse);
            response.put("totalCourses", courses.size());
            response.put("activeCourses", courses.stream().filter(c -> c.getStatus() != null && c.getStatus()).count());
            response.put("hiddenCourses", courses.stream().filter(c -> c.getStatus() == null || !c.getStatus()).count());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting courses: " + e.getMessage());
        }
    }

    // ==================== COMMENT MANAGEMENT ENDPOINTS ====================
    
    /**
     * Lấy tất cả comments
     */
    @GetMapping("/comments")
    public ResponseEntity<?> getAllComments() {
        try {
            List<Comment> comments = commentRepository.findAll();
            
            // Convert to response format với thông tin đầy đủ
            List<Map<String, Object>> commentsResponse = comments.stream()
                .map(comment -> {
                    Map<String, Object> commentInfo = new HashMap<>();
                    commentInfo.put("commentId", comment.getCommentId());
                    commentInfo.put("commentText", comment.getCommentText());
                    commentInfo.put("createdAt", comment.getCreatedDate());
                    commentInfo.put("updatedAt", comment.getUpdatedDate());
                    commentInfo.put("isReported", comment.getIsReported());
                    commentInfo.put("reportCount", comment.getReportCount());
                    commentInfo.put("reportReason", comment.getReportReason());
                    
                    // Customer info
                    if (comment.getCustomer() != null) {
                        Map<String, Object> customerInfo = new HashMap<>();
                        customerInfo.put("customerId", comment.getCustomer().getCustomerId());
                        customerInfo.put("customerName", comment.getCustomer().getCustomerName());
                        customerInfo.put("customerImg", comment.getCustomer().getCustomerImg());
                        commentInfo.put("customer", customerInfo);
                    }
                    
                    // Exercise info
                    if (comment.getExercise() != null) {
                        Map<String, Object> exerciseInfo = new HashMap<>();
                        exerciseInfo.put("exerciseId", comment.getExercise().getExerciseId());
                        exerciseInfo.put("exerciseName", comment.getExercise().getExerciseName());
                        commentInfo.put("exercise", exerciseInfo);
                    }
                    
                    // Course info
                    if (comment.getCourse() != null) {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("courseId", comment.getCourse().getCourseId());
                        courseInfo.put("courseName", comment.getCourse().getCourseName());
                        commentInfo.put("course", courseInfo);
                    }
                    
                    // Day info
                    if (comment.getDay() != null) {
                        Map<String, Object> dayInfo = new HashMap<>();
                        dayInfo.put("dayId", comment.getDay().getDayId());
                        dayInfo.put("dayName", comment.getDay().getDayName());
                        commentInfo.put("day", dayInfo);
                    }
                    
                    return commentInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(commentsResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting comments: " + e.getMessage());
        }
    }
    
    /**
     * Lấy reported comments
     */
    @GetMapping("/comments/reported")
    public ResponseEntity<?> getReportedComments() {
        try {
            // Lấy comments có isReported = true hoặc reportCount > 0
            List<Comment> allComments = commentRepository.findAll();
            List<Comment> reportedComments = allComments.stream()
                .filter(comment -> comment.getIsReported() != null && comment.getIsReported() ||
                                 comment.getReportCount() != null && comment.getReportCount() > 0)
                .collect(Collectors.toList());
            
            // Convert to response format tương tự như getAllComments
            List<Map<String, Object>> commentsResponse = reportedComments.stream()
                .map(comment -> {
                    Map<String, Object> commentInfo = new HashMap<>();
                    commentInfo.put("commentId", comment.getCommentId());
                    commentInfo.put("commentText", comment.getCommentText());
                    commentInfo.put("createdAt", comment.getCreatedDate());
                    commentInfo.put("isReported", comment.getIsReported());
                    commentInfo.put("reportCount", comment.getReportCount());
                    commentInfo.put("reportReason", comment.getReportReason());
                    
                    // Customer info
                    if (comment.getCustomer() != null) {
                        Map<String, Object> customerInfo = new HashMap<>();
                        customerInfo.put("customerId", comment.getCustomer().getCustomerId());
                        customerInfo.put("customerName", comment.getCustomer().getCustomerName());
                        customerInfo.put("customerImg", comment.getCustomer().getCustomerImg());
                        commentInfo.put("customer", customerInfo);
                    }
                    
                    // Exercise info
                    if (comment.getExercise() != null) {
                        Map<String, Object> exerciseInfo = new HashMap<>();
                        exerciseInfo.put("exerciseId", comment.getExercise().getExerciseId());
                        exerciseInfo.put("exerciseName", comment.getExercise().getExerciseName());
                        commentInfo.put("exercise", exerciseInfo);
                    }
                    
                    // Course info
                    if (comment.getCourse() != null) {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("courseId", comment.getCourse().getCourseId());
                        courseInfo.put("courseName", comment.getCourse().getCourseName());
                        commentInfo.put("course", courseInfo);
                    }
                    
                    // Day info
                    if (comment.getDay() != null) {
                        Map<String, Object> dayInfo = new HashMap<>();
                        dayInfo.put("dayId", comment.getDay().getDayId());
                        dayInfo.put("dayName", comment.getDay().getDayName());
                        commentInfo.put("day", dayInfo);
                    }
                    
                    return commentInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(commentsResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting reported comments: " + e.getMessage());
        }
    }
    
    /**
     * Xóa comment
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer commentId) {
        try {
            Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
            
            commentRepository.delete(comment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");
            response.put("deletedCommentId", commentId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting comment: " + e.getMessage());
        }
    }
    
    /**
     * Dismiss report (placeholder - cần implement report system)
     */
    @PostMapping("/comments/{commentId}/dismiss-report")
    public ResponseEntity<?> dismissCommentReport(@PathVariable Integer commentId) {
        try {
            // Tạm thời return success - có thể implement report dismissal logic sau
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Report dismissed successfully");
            response.put("commentId", commentId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error dismissing report: " + e.getMessage());
        }
    }

    // ==================== REVENUE MANAGEMENT ENDPOINTS ====================
    
    /**
     * Lấy tất cả revenue transactions (successful payments)
     */
    @GetMapping("/revenue-transactions")
    public ResponseEntity<?> getRevenueTransactions() {
        try {
            // Lấy tất cả payments thành công
            List<VipPayment> successfulPayments = vipPaymentRepository
                .findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.SUCCESS);
            
            // Convert to response format
            List<Map<String, Object>> transactionsResponse = successfulPayments.stream()
                .map(payment -> {
                    Map<String, Object> transactionInfo = new HashMap<>();
                    transactionInfo.put("paymentId", payment.getPaymentId());
                    transactionInfo.put("amount", payment.getAmount().doubleValue());
                    transactionInfo.put("paymentStatus", payment.getPaymentStatus().name());
                    transactionInfo.put("createdDate", payment.getCreatedDate());
                    transactionInfo.put("completedDate", payment.getCompletedDate());
                    
                    // Customer info
                    if (payment.getCustomer() != null) {
                        Map<String, Object> customerInfo = new HashMap<>();
                        customerInfo.put("customerId", payment.getCustomer().getCustomerId());
                        customerInfo.put("customerName", payment.getCustomer().getCustomerName());
                        customerInfo.put("customerImg", payment.getCustomer().getCustomerImg());
                        transactionInfo.put("customer", customerInfo);
                    }
                    
                    // Course info
                    if (payment.getCourse() != null) {
                        Map<String, Object> courseInfo = new HashMap<>();
                        courseInfo.put("courseId", payment.getCourse().getCourseId());
                        courseInfo.put("courseName", payment.getCourse().getCourseName());
                        courseInfo.put("courseImage", payment.getCourse().getImage());
                        transactionInfo.put("course", courseInfo);
                    }
                    
                    return transactionInfo;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(transactionsResponse);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting revenue transactions: " + e.getMessage());
        }
    }

    // ===== COURSE CRUD ENDPOINTS =====
    
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<?> getCourseById(@PathVariable Integer courseId) {
        try {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Course not found with ID: " + courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            return ResponseEntity.ok(course);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/courses/{courseId}")
    public ResponseEntity<?> updateCourse(@PathVariable Integer courseId, @RequestBody Map<String, Object> courseData) {
        try {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Course not found with ID: " + courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Update course fields
            if (courseData.containsKey("courseName")) {
                course.setCourseName((String) courseData.get("courseName"));
            }
            if (courseData.containsKey("description")) {
                course.setDescription((String) courseData.get("description"));
            }
            if (courseData.containsKey("duration")) {
                course.setDuration((String) courseData.get("duration"));
            }
            if (courseData.containsKey("status")) {
                course.setStatus((Boolean) courseData.get("status"));
            }
            if (courseData.containsKey("price")) {
                course.setPrice(new BigDecimal(courseData.get("price").toString()));
            }
            if (courseData.containsKey("isVip")) {
                course.setIsVip((Boolean) courseData.get("isVip"));
            }
            if (courseData.containsKey("averageRating")) {
                course.setAverageRating(Double.parseDouble(courseData.get("averageRating").toString()));
            }
            // Handle new fields
            if (courseData.containsKey("weeklySessions")) {
                course.setWeeklySessions((String) courseData.get("weeklySessions"));
            }
            if (courseData.containsKey("recommendedFor")) {
                course.setRecommendedFor((String) courseData.get("recommendedFor"));
            }
            if (courseData.containsKey("difficultyLevel")) {
                course.setDifficultyLevel((String) courseData.get("difficultyLevel"));
            }
            if (courseData.containsKey("image")) {
                course.setImage((String) courseData.get("image"));
            }
            
            // Handle course type update
            if (courseData.containsKey("courseTypeId")) {
                Integer courseTypeId = Integer.parseInt(courseData.get("courseTypeId").toString());
                CourseType courseType = courseTypeRepository.findById(courseTypeId).orElse(null);
                if (courseType != null) {
                    course.setCourseType(courseType);
                }
            }
            
            // Update timestamps
            course.setUpdatedDate(LocalDateTime.now());
            
            courseRepository.save(course);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course updated successfully");
            response.put("courseId", courseId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PutMapping("/courses/{courseId}/status")
    public ResponseEntity<?> toggleCourseStatus(@PathVariable Integer courseId, @RequestBody Map<String, Object> statusData) {
        try {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Course not found with ID: " + courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Boolean newStatus = (Boolean) statusData.get("status");
            course.setStatus(newStatus);
            course.setUpdatedDate(LocalDateTime.now());
            
            courseRepository.save(course);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course status updated successfully");
            response.put("courseId", courseId);
            response.put("newStatus", newStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to update course status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/courses/{courseId}")
    public ResponseEntity<?> deleteCourse(@PathVariable Integer courseId) {
        try {
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Course not found with ID: " + courseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Soft delete by setting status to false
            course.setStatus(false);
            course.setUpdatedDate(LocalDateTime.now());
            courseRepository.save(course);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course deleted successfully (soft delete)");
            response.put("courseId", courseId);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete course: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // ===== COURSE DAYS MANAGEMENT =====
    
    @GetMapping("/courses/{courseId}/days")
    public ResponseEntity<?> getCourseDays(@PathVariable Integer courseId) {
        try {
            // Query to get course days with exercises
            String sql = "SELECT cd.course_day_id, cd.course_id, cd.day_id, d.day_name, cd.status, " +
                        "e.exercise_id, e.exercise_name, e.target, e.body_part, e.instructions " +
                        "FROM course_days cd " +
                        "JOIN days d ON cd.day_id = d.day_id " +
                        "LEFT JOIN exercise_day ed ON d.day_id = ed.day_id AND ed.course_id = cd.course_id " +
                        "LEFT JOIN exercise e ON ed.exercise_id = e.exercise_id " +
                        "WHERE cd.course_id = :courseId AND cd.status = true " +
                        "ORDER BY d.day_id, e.exercise_id";
            
            List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class)
                .setParameter("courseId", courseId)
                .getResultList();
            
            // Group results by day
            Map<Integer, Map<String, Object>> daysMap = new LinkedHashMap<>();
            
            for (Tuple result : results) {
                Integer dayId = ((Number) result.get("day_id")).intValue();
                
                if (!daysMap.containsKey(dayId)) {
                    Map<String, Object> dayInfo = new HashMap<>();
                    dayInfo.put("dayId", dayId);
                    dayInfo.put("dayName", result.get("day_name"));
                    dayInfo.put("courseId", courseId);
                    dayInfo.put("status", result.get("status"));
                    dayInfo.put("exercises", new ArrayList<>());
                    daysMap.put(dayId, dayInfo);
                }
                
                // Add exercise if exists
                if (result.get("exercise_id") != null) {
                    Map<String, Object> exercise = new HashMap<>();
                    exercise.put("exerciseId", result.get("exercise_id"));
                    exercise.put("exerciseName", result.get("exercise_name"));
                    exercise.put("target", result.get("target"));
                    exercise.put("bodyPart", result.get("body_part"));
                    exercise.put("instructions", result.get("instructions"));
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> exercises = (List<Map<String, Object>>) daysMap.get(dayId).get("exercises");
                    exercises.add(exercise);
                }
            }
            
            List<Map<String, Object>> courseDays = new ArrayList<>(daysMap.values());
            
            Map<String, Object> response = new HashMap<>();
            response.put("courseDays", courseDays);
            response.put("totalDays", courseDays.size());
            response.put("courseId", courseId);
            
            return ResponseEntity.ok(courseDays);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch course days: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @PostMapping("/courses/{courseId}/days")
    public ResponseEntity<?> addCourseDay(@PathVariable Integer courseId, @RequestBody Map<String, Object> dayData) {
        try {
            String dayName = (String) dayData.get("dayName");
            
            if (dayName == null || dayName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Day name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // First create a new day
            String insertDaySQL = "INSERT INTO days (day_name) VALUES (:dayName)";
            Query insertDayQuery = entityManager.createNativeQuery(insertDaySQL);
            insertDayQuery.setParameter("dayName", dayName);
            insertDayQuery.executeUpdate();
            
            // Get the newly created day ID
            String getLastIdSQL = "SELECT LAST_INSERT_ID()";
            Number dayId = (Number) entityManager.createNativeQuery(getLastIdSQL).getSingleResult();
            
            // Create course_days association
            String insertCourseDaySQL = "INSERT INTO course_days (course_id, day_id, status) VALUES (:courseId, :dayId, true)";
            Query insertCourseDayQuery = entityManager.createNativeQuery(insertCourseDaySQL);
            insertCourseDayQuery.setParameter("courseId", courseId);
            insertCourseDayQuery.setParameter("dayId", dayId);
            insertCourseDayQuery.executeUpdate();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Day added successfully");
            response.put("dayId", dayId);
            response.put("dayName", dayName);
            response.put("courseId", courseId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add course day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @DeleteMapping("/courses/{courseId}/days/{dayId}")
    public ResponseEntity<?> deleteCourseDay(@PathVariable Integer courseId, @PathVariable Integer dayId) {
        try {
            // Soft delete by setting status to false
            String updateSQL = "UPDATE course_days SET status = false WHERE course_id = :courseId AND day_id = :dayId";
            Query updateQuery = entityManager.createNativeQuery(updateSQL);
            updateQuery.setParameter("courseId", courseId);
            updateQuery.setParameter("dayId", dayId);
            
            int updatedRows = updateQuery.executeUpdate();
            
            if (updatedRows == 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Course day not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course day deleted successfully");
            response.put("dayId", dayId);
            response.put("courseId", courseId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete course day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // ===== COURSE EXERCISES MANAGEMENT =====
    
    @PostMapping("/courses/{courseId}/days/{dayId}/exercises")
    public ResponseEntity<?> addExerciseToDay(@PathVariable Integer courseId, 
                                              @PathVariable Integer dayId, 
                                              @RequestBody Map<String, Object> exerciseData) {
        try {
            Integer exerciseId = null;
            if (exerciseData.containsKey("exerciseId")) {
                exerciseId = Integer.parseInt(exerciseData.get("exerciseId").toString());
            }
            
            if (exerciseId == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Exercise ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Check if exercise exists
            String checkExerciseSQL = "SELECT COUNT(*) FROM exercise WHERE exercise_id = :exerciseId";
            BigInteger exerciseExists = (BigInteger) entityManager.createNativeQuery(checkExerciseSQL)
                .setParameter("exerciseId", exerciseId)
                .getSingleResult();
            
            if (exerciseExists.intValue() == 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Exercise not found with ID: " + exerciseId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            // Check if the combination already exists
            String checkExistingSQL = "SELECT COUNT(*) FROM exercise_day " +
                                      "WHERE course_id = :courseId AND day_id = :dayId AND exercise_id = :exerciseId";
            BigInteger alreadyExists = (BigInteger) entityManager.createNativeQuery(checkExistingSQL)
                .setParameter("courseId", courseId)
                .setParameter("dayId", dayId)
                .setParameter("exerciseId", exerciseId)
                .getSingleResult();
            
            if (alreadyExists.intValue() > 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "This exercise is already added to this day");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Get order number (default to 1 if not provided)
            Integer orderNum = 1;
            if (exerciseData.containsKey("order") && exerciseData.get("order") != null) {
                orderNum = Integer.parseInt(exerciseData.get("order").toString());
            }
            
            // Insert exercise-day relationship
            String insertSQL = "INSERT INTO exercise_day (course_id, day_id, exercise_id, order_num, status) " +
                              "VALUES (:courseId, :dayId, :exerciseId, :orderNum, true)";
            
            Query insertQuery = entityManager.createNativeQuery(insertSQL);
            insertQuery.setParameter("courseId", courseId);
            insertQuery.setParameter("dayId", dayId);
            insertQuery.setParameter("exerciseId", exerciseId);
            insertQuery.setParameter("orderNum", orderNum);
            
            insertQuery.executeUpdate();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exercise added successfully");
            response.put("courseId", courseId);
            response.put("dayId", dayId);
            response.put("exerciseId", exerciseId);
            response.put("orderNum", orderNum);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to add exercise: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @GetMapping("/courses/{courseId}/days/{dayId}/exercises")
    public ResponseEntity<?> getDayExercises(@PathVariable Integer courseId, @PathVariable Integer dayId) {
        try {
            // Query to get exercises for a specific day in a course
            String sql = "SELECT e.exercise_id, e.exercise_name, e.description, e.body_part, e.target, " +
                         "e.equipment, e.gif_url, e.instructions, e.secondary_muscles, ed.order_num " +
                         "FROM exercise e " +
                         "JOIN exercise_day ed ON e.exercise_id = ed.exercise_id " +
                         "WHERE ed.course_id = :courseId AND ed.day_id = :dayId " +
                         "ORDER BY ed.order_num";
            
            List<Map<String, Object>> dayExercises = new ArrayList<>();
            
            List<Object[]> results = entityManager.createNativeQuery(sql)
                .setParameter("courseId", courseId)
                .setParameter("dayId", dayId)
                .getResultList();
            
            for (Object[] result : results) {
                Map<String, Object> exercise = new HashMap<>();
                exercise.put("exerciseId", result[0]);
                exercise.put("exerciseName", result[1]);
                exercise.put("description", result[2]);
                exercise.put("bodyPart", result[3]);
                exercise.put("target", result[4]);
                exercise.put("equipment", result[5]);
                exercise.put("gifUrl", result[6]);
                exercise.put("instructions", result[7]);
                exercise.put("secondaryMuscles", result[8]);
                exercise.put("orderNum", result[9]);
                dayExercises.add(exercise);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("courseId", courseId);
            response.put("dayId", dayId);
            response.put("exercises", dayExercises);
            response.put("count", dayExercises.size());
            
            return ResponseEntity.ok(dayExercises);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve day exercises: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @DeleteMapping("/courses/{courseId}/days/{dayId}/exercises/{exerciseId}")
    public ResponseEntity<?> removeExerciseFromDay(@PathVariable Integer courseId,
                                                   @PathVariable Integer dayId,
                                                   @PathVariable Integer exerciseId) {
        try {
            // Remove exercise from day by setting status to false
            String updateSQL = "UPDATE exercise_day SET status = false WHERE day_id = :dayId AND exercise_id = :exerciseId AND course_id = :courseId";
            Query updateQuery = entityManager.createNativeQuery(updateSQL);
            updateQuery.setParameter("dayId", dayId);
            updateQuery.setParameter("exerciseId", exerciseId);
            updateQuery.setParameter("courseId", courseId);
            
            int updatedRows = updateQuery.executeUpdate();
            
            if (updatedRows == 0) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Exercise not found in this day");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Exercise removed from day successfully");
            response.put("dayId", dayId);
            response.put("exerciseId", exerciseId);
            response.put("courseId", courseId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error removing exercise from day: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to remove exercise from day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // ===== COURSE TYPES ENDPOINT =====
    
    @GetMapping("/exercises")
    public ResponseEntity<?> getAllExercises() {
        try {
            // Create a native query for exercises
            String sql = "SELECT e.* FROM exercise e ORDER BY e.exercise_name";
            List<Map<String, Object>> exercises = new ArrayList<>();
            
            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();
            
            for (Object[] result : results) {
                Map<String, Object> exercise = new HashMap<>();
                exercise.put("exerciseId", result[0]);
                exercise.put("exerciseName", result[1]);
                exercise.put("description", result[2]);
                exercise.put("bodyPart", result[3]);
                exercise.put("target", result[4]);
                exercise.put("equipment", result[5]);
                exercise.put("gifUrl", result[6]);
                exercise.put("instructions", result[7]);
                exercise.put("secondaryMuscles", result[8]);
                exercises.add(exercise);
            }
            
            return ResponseEntity.ok(exercises);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to retrieve exercises: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/course-types")
    public ResponseEntity<?> getCourseTypes() {
        try {
            String sql = "SELECT course_type_id, course_type_name FROM course_type WHERE status = true ORDER BY course_type_id";
            List<Tuple> results = entityManager.createNativeQuery(sql, Tuple.class).getResultList();
            
            List<Map<String, Object>> courseTypes = results.stream()
                .map(result -> {
                    Map<String, Object> type = new HashMap<>();
                    type.put("courseTypeId", result.get("course_type_id"));
                    type.put("courseTypeName", result.get("course_type_name"));
                    return type;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(courseTypes);
            
        } catch (Exception e) {
            logger.error("Error fetching course types: {}", e.getMessage());
            
            // Return default course types if query fails
            List<Map<String, Object>> defaultTypes = new ArrayList<>();
            
            Map<String, Object> type1 = new HashMap<>();
            type1.put("courseTypeId", 1);
            type1.put("courseTypeName", "Strength Training");
            defaultTypes.add(type1);
            
            Map<String, Object> type2 = new HashMap<>();
            type2.put("courseTypeId", 2);
            type2.put("courseTypeName", "Cardiovascular");
            defaultTypes.add(type2);
            
            Map<String, Object> type3 = new HashMap<>();
            type3.put("courseTypeId", 3);
            type3.put("courseTypeName", "Flexibility");
            defaultTypes.add(type3);
            
            Map<String, Object> type4 = new HashMap<>();
            type4.put("courseTypeId", 4);
            type4.put("courseTypeName", "Strength & Endurance");
            defaultTypes.add(type4);
            
            Map<String, Object> type5 = new HashMap<>();
            type5.put("courseTypeId", 5);
            type5.put("courseTypeName", "Mind-Body Wellness");
            defaultTypes.add(type5);
            
            Map<String, Object> type6 = new HashMap<>();
            type6.put("courseTypeId", 6);
            type6.put("courseTypeName", "Functional Training");
            defaultTypes.add(type6);
            
            Map<String, Object> type7 = new HashMap<>();
            type7.put("courseTypeId", 7);
            type7.put("courseTypeName", "Weight Loss");
            defaultTypes.add(type7);
            
            return ResponseEntity.ok(defaultTypes);
        }
    }

    @PostMapping("/courses/upload-image")
    public ResponseEntity<?> uploadCourseImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseType") String courseType,
            @RequestParam("courseName") String courseName) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Please select a file to upload");
                error.put("status", "error");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check file size (5MB limit)
            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "File size must be less than 5MB");
                error.put("status", "error");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Please select an image file");
                error.put("status", "error");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Upload to Firebase using the service
            String imageUrl = firebaseStorageService.uploadCourseImage(file, courseType, courseName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Course image uploaded successfully");
            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to upload course image: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to upload course image: " + e.getMessage());
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 