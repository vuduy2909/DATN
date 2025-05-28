package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerTypeRepository;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import com.capstone.wellnessnavigatorgym.service.PaymentStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    private PaymentStatusService paymentStatusService;
    
    @Autowired
    private IVipPaymentRepository vipPaymentRepository;
    
    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ICustomerTypeRepository customerTypeRepository;

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
            
            Map<String, Long> userTypeStats = allCustomers.stream()
                .filter(c -> c.getCustomerType() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getCustomerType().getCustomerTypeName(),
                    Collectors.counting()
                ));
            
            Map<String, Object> userStats = new HashMap<>();
            userStats.put("total", (long) allCustomers.size());
            userStats.put("regular", userTypeStats.getOrDefault("Regular", 0L));
            userStats.put("premium", userTypeStats.getOrDefault("Premium", 0L));
            userStats.put("vip", userTypeStats.getOrDefault("VIP", 0L));
            
            response.put("userStats", userStats);
            
            // Thống kê khóa học (cần implement sau khi có Course entity)
            Map<String, Object> courseStats = new HashMap<>();
            courseStats.put("active", 24); // Tạm thời hardcode
            courseStats.put("hidden", 3);
            courseStats.put("newThisWeek", 2);
            courseStats.put("newThisMonth", 8);
            
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
} 