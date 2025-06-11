package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.entity.Comment;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.CourseType;
import com.capstone.wellnessnavigatorgym.entity.Exercise;
import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerTypeRepository;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICommentRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseTypeRepository;
import com.capstone.wellnessnavigatorgym.repository.IExerciseRepository;
import com.capstone.wellnessnavigatorgym.repository.IAccountRepository;
import com.capstone.wellnessnavigatorgym.service.PaymentStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
    private IExerciseRepository exerciseRepository;

    @Autowired
    private IAccountRepository accountRepository;

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
    public ResponseEntity<?> getDashboard(
            @RequestParam(value = "recentActivitiesPage", defaultValue = "0") int recentActivitiesPage,
            @RequestParam(value = "recentActivitiesSize", defaultValue = "10") int recentActivitiesSize) {
        try {
            System.out.println("=== DASHBOARD-OVERVIEW ENDPOINT CALLED ===");
            Map<String, Object> response = new HashMap<>();
            
            // Helper method to calculate percentage change
            java.util.function.BiFunction<Long, Long, String> calculatePercentChange = (current, previous) -> {
                if (previous == 0) {
                    return current > 0 ? "Mới" : "0%";
                }
                double percent = ((double)(current - previous) / previous) * 100;
                if (percent > 0) {
                    return "+" + String.format("%.0f", percent) + "%";
                } else if (percent < 0) {
                    return String.format("%.0f", percent) + "%";
                } else {
                    return "0%";
                }
            };
            
            // Thống kê người dùng - so sánh tháng này vs tháng trước
            List<Customer> allCustomers = customerRepository.findAll().stream()
                .filter(c -> c.getIsEnable())
                .collect(Collectors.toList());
            
            System.out.println("Total customers found: " + allCustomers.size());
            
            // Tính người dùng tháng này và tháng trước
            try {
                String currentMonthQuery = "SELECT COUNT(*) FROM customer c JOIN account a ON c.account_id = a.account_id " +
                    "WHERE YEAR(a.create_date) = YEAR(CURDATE()) AND MONTH(a.create_date) = MONTH(CURDATE())";
                BigInteger currentMonthUsers = (BigInteger) entityManager.createNativeQuery(currentMonthQuery).getSingleResult();
                
                String previousMonthQuery = "SELECT COUNT(*) FROM customer c JOIN account a ON c.account_id = a.account_id " +
                    "WHERE YEAR(a.create_date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) " +
                    "AND MONTH(a.create_date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))";
                BigInteger previousMonthUsers = (BigInteger) entityManager.createNativeQuery(previousMonthQuery).getSingleResult();
                
                String userPercentChange = calculatePercentChange.apply(currentMonthUsers.longValue(), previousMonthUsers.longValue());
                response.put("userTrendPercent", userPercentChange);
                
            } catch (Exception e) {
                response.put("userTrendPercent", "0%");
            }
            
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
            
            // Real course creation stats based on updated_date if available
            try {
                // Count courses created in the last week
                String weekQuery = "SELECT COUNT(*) FROM course WHERE created_date >= DATE_SUB(NOW(), INTERVAL 1 WEEK)";
                BigInteger newThisWeek = (BigInteger) entityManager.createNativeQuery(weekQuery).getSingleResult();
                courseStats.put("newThisWeek", newThisWeek.longValue());
                
                // Count courses created in the last month
                String monthQuery = "SELECT COUNT(*) FROM course WHERE created_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH)";
                BigInteger newThisMonth = (BigInteger) entityManager.createNativeQuery(monthQuery).getSingleResult();
                courseStats.put("newThisMonth", newThisMonth.longValue());
            } catch (Exception e) {
                // Fallback if created_date field doesn't exist
                courseStats.put("newThisWeek", 0L);
                courseStats.put("newThisMonth", 0L);
            }
            
            response.put("courseStats", courseStats);
            
            // Thống kê đăng ký - tính toán thực từ customer data
            Map<String, Object> registrationStats = new HashMap<>();
            try {
                // Count customers registered today
                String todayQuery = "SELECT COUNT(*) FROM customer c JOIN account a ON c.account_id = a.account_id WHERE DATE(a.create_date) = CURDATE()";
                BigInteger todayRegistrations = (BigInteger) entityManager.createNativeQuery(todayQuery).getSingleResult();
                registrationStats.put("today", todayRegistrations.longValue());
                
                // Count customers registered this week
                String weekRegQuery = "SELECT COUNT(*) FROM customer c JOIN account a ON c.account_id = a.account_id WHERE a.create_date >= DATE_SUB(NOW(), INTERVAL 1 WEEK)";
                BigInteger weekRegistrations = (BigInteger) entityManager.createNativeQuery(weekRegQuery).getSingleResult();
                registrationStats.put("thisWeek", weekRegistrations.longValue());
                
                // Count customers registered this month
                String monthRegQuery = "SELECT COUNT(*) FROM customer c JOIN account a ON c.account_id = a.account_id WHERE a.create_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH)";
                BigInteger monthRegistrations = (BigInteger) entityManager.createNativeQuery(monthRegQuery).getSingleResult();
                registrationStats.put("thisMonth", monthRegistrations.longValue());
            } catch (Exception e) {
                // Fallback if create_date field doesn't exist
                registrationStats.put("today", 0L);
                registrationStats.put("thisWeek", 0L);
                registrationStats.put("thisMonth", 0L);
            }
            
            response.put("registrationStats", registrationStats);
            
            // Thống kê doanh thu - với so sánh tháng trước
            List<VipPayment> allPayments = vipPaymentRepository.findAll();
            System.out.println("Total payments found: " + allPayments.size());
            
            List<VipPayment> successPayments = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS)
                .collect(Collectors.toList());
            
            System.out.println("Success payments found: " + successPayments.size());
            
            BigDecimal totalRevenue = successPayments.stream()
                .map(VipPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Tính doanh thu tháng này và tháng trước
            try {
                BigDecimal currentMonthRevenue = successPayments.stream()
                    .filter(p -> {
                        if (p.getCreatedDate() == null) return false;
                        java.time.LocalDate paymentDate = p.getCreatedDate()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.time.LocalDate now = java.time.LocalDate.now();
                        return paymentDate.getMonth() == now.getMonth() && paymentDate.getYear() == now.getYear();
                    })
                    .map(VipPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                BigDecimal previousMonthRevenue = successPayments.stream()
                    .filter(p -> {
                        if (p.getCreatedDate() == null) return false;
                        java.time.LocalDate paymentDate = p.getCreatedDate()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.time.LocalDate previousMonth = java.time.LocalDate.now().minusMonths(1);
                        return paymentDate.getMonth() == previousMonth.getMonth() && paymentDate.getYear() == previousMonth.getYear();
                    })
                    .map(VipPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                String revenuePercentChange = calculatePercentChange.apply(currentMonthRevenue.longValue(), previousMonthRevenue.longValue());
                response.put("revenueTrendPercent", revenuePercentChange);
                
            } catch (Exception e) {
                response.put("revenueTrendPercent", "0%");
            }
            
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
            
            // Thống kê đơn hàng - với so sánh tháng trước
            long pendingOrders = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.PENDING)
                .count();
            
            long successOrders = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS)
                .count();
            
            long failedOrders = allPayments.stream()
                .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.FAILED)
                .count();
            
            // Tính đơn thành công tháng này và tháng trước
            try {
                long currentMonthSuccessOrders = successPayments.stream()
                    .filter(p -> {
                        if (p.getCreatedDate() == null) return false;
                        java.time.LocalDate paymentDate = p.getCreatedDate()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.time.LocalDate now = java.time.LocalDate.now();
                        return paymentDate.getMonth() == now.getMonth() && paymentDate.getYear() == now.getYear();
                    })
                    .count();
                
                long previousMonthSuccessOrders = successPayments.stream()
                    .filter(p -> {
                        if (p.getCreatedDate() == null) return false;
                        java.time.LocalDate paymentDate = p.getCreatedDate()
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        java.time.LocalDate previousMonth = java.time.LocalDate.now().minusMonths(1);
                        return paymentDate.getMonth() == previousMonth.getMonth() && paymentDate.getYear() == previousMonth.getYear();
                    })
                    .count();
                
                String ordersPercentChange = calculatePercentChange.apply(currentMonthSuccessOrders, previousMonthSuccessOrders);
                response.put("ordersTrendPercent", ordersPercentChange);
                
            } catch (Exception e) {
                response.put("ordersTrendPercent", "0%");
            }
            
            Map<String, Object> orderStats = new HashMap<>();
            orderStats.put("pending", pendingOrders);
            orderStats.put("success", successOrders);
            orderStats.put("failed", failedOrders);
            orderStats.put("total", (long) allPayments.size());
            
            response.put("orderStats", orderStats);
            
            // Hoạt động gần đây với phân trang
            List<VipPayment> allRecentPayments = allPayments.stream()
                .filter(payment -> payment.getCustomer() != null) // Lọc payments có customer
                .sorted((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate())) // Sắp xếp theo thời gian mới nhất
                .collect(Collectors.toList());
            
            int totalRecentActivities = allRecentPayments.size();
            int startIndex = recentActivitiesPage * recentActivitiesSize;
            int endIndex = Math.min(startIndex + recentActivitiesSize, totalRecentActivities);
            
            List<VipPayment> paginatedPayments = allRecentPayments.subList(startIndex, endIndex);
            
            List<Map<String, Object>> recentActivities = paginatedPayments.stream()
                .map(payment -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("id", payment.getPaymentId());
                    activity.put("userName", payment.getCustomer().getCustomerName());
                    String description = "VIP Payment";
                    if (payment.getCourse() != null && payment.getCourse().getCourseName() != null) {
                        description = payment.getCourse().getCourseName();
                    }
                    activity.put("description", description);
                    activity.put("amount", payment.getAmount());
                    activity.put("status", payment.getPaymentStatus());
                    activity.put("createdAt", payment.getCreatedDate());
                    return activity;
                })
                .collect(Collectors.toList());
            
            response.put("recentActivities", recentActivities);
            response.put("recentActivitiesTotal", totalRecentActivities);
            
            // Real monthly data calculation based on payment history
            List<Map<String, Object>> monthlyData = new ArrayList<>();
            try {
                // Get the last 6 months of data
                for (int monthsBack = 5; monthsBack >= 0; monthsBack--) {
                    Map<String, Object> monthData = new HashMap<>();
                    
                    // Calculate month name
                    LocalDateTime targetMonth = LocalDateTime.now().minusMonths(monthsBack);
                    String monthName = targetMonth.getMonth().toString().substring(0, 3);
                    monthData.put("month", monthName);
                    
                    // Count new users in this month
                    String userCountQuery = "SELECT COUNT(*) FROM customer c JOIN account a ON c.account_id = a.account_id " +
                                           "WHERE YEAR(a.create_date) = ? AND MONTH(a.create_date) = ?";
                    BigInteger userCount = (BigInteger) entityManager.createNativeQuery(userCountQuery)
                        .setParameter(1, targetMonth.getYear())
                        .setParameter(2, targetMonth.getMonthValue())
                        .getSingleResult();
                    monthData.put("users", userCount.longValue());
                    
                    // Calculate revenue for this month (in millions VND)
                    String revenueQuery = "SELECT COALESCE(SUM(amount), 0) FROM vip_payment " +
                                         "WHERE payment_status = 'SUCCESS' AND YEAR(created_date) = ? AND MONTH(created_date) = ?";
                    BigDecimal monthRevenue = (BigDecimal) entityManager.createNativeQuery(revenueQuery)
                        .setParameter(1, targetMonth.getYear())
                        .setParameter(2, targetMonth.getMonthValue())
                        .getSingleResult();
                    monthData.put("revenue", monthRevenue.divide(BigDecimal.valueOf(1000000)).doubleValue()); // Convert to millions
                    
                    monthlyData.add(monthData);
                }
            } catch (Exception e) {
                // Fallback with minimal data if queries fail
                String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
                monthlyData.clear();
                for (String month : months) {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", month);
                    monthData.put("users", 0);
                    monthData.put("revenue", 0.0);
                    monthlyData.add(monthData);
                }
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
            
            // Get next sequential ID (MAX + 1)
            Integer nextId = customerTypeRepository.findAll().stream()
                .mapToInt(CustomerType::getCustomerTypeId)
                .max()
                .orElse(0) + 1;
            
            CustomerType newCustomerType = new CustomerType();
            newCustomerType.setCustomerTypeId(nextId);  // Manually assign ID
            newCustomerType.setCustomerTypeName(customerTypeName.trim());
            newCustomerType.setPrice(price);
            
            CustomerType savedType = customerTypeRepository.save(newCustomerType);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerType", savedType);
            response.put("message", "Customer type created successfully with ID: " + nextId);
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

    // ==================== CUSTOMER MANAGEMENT ENDPOINTS ====================
    
    /**
     * Lấy tất cả customers với phân trang và filtering
     */
    @GetMapping("/customers")
    public ResponseEntity<?> getAllCustomers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status, // active, banned, all
            @RequestParam(value = "customerTypeId", required = false) Integer customerTypeId) {
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            
            // Apply filters
            List<Customer> filteredCustomers = allCustomers.stream()
                .filter(customer -> {
                    // Search filter
                    if (search != null && !search.trim().isEmpty()) {
                        return customer.getCustomerName().toLowerCase().contains(search.toLowerCase()) ||
                               customer.getCustomerEmail().toLowerCase().contains(search.toLowerCase()) ||
                               customer.getCustomerPhone().contains(search);
                    }
                    return true;
                })
                .filter(customer -> {
                    // Status filter
                    if (status != null && !"all".equals(status)) {
                        if ("active".equals(status)) {
                            return customer.getIsEnable();
                        } else if ("banned".equals(status)) {
                            return !customer.getIsEnable();
                        }
                    }
                    return true;
                })
                .filter(customer -> {
                    // Customer type filter
                    if (customerTypeId != null) {
                        return customer.getCustomerType() != null && 
                               customer.getCustomerType().getCustomerTypeId().equals(customerTypeId);
                    }
                    return true;
                })
                .collect(Collectors.toList());
            
            // Pagination
            int start = page * size;
            int end = Math.min(start + size, filteredCustomers.size());
            List<Customer> paginatedCustomers = filteredCustomers.subList(start, end);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customers", paginatedCustomers);
            response.put("totalElements", filteredCustomers.size());
            response.put("totalPages", (int) Math.ceil((double) filteredCustomers.size() / size));
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting customers: " + e.getMessage());
        }
    }
    
    /**
     * Lấy customer theo ID
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<?> getCustomerById(@PathVariable Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            
            Map<String, Object> response = new HashMap<>();
            response.put("customer", customer);
            response.put("message", "Customer retrieved successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error getting customer: " + e.getMessage());
        }
    }
    
    /**
     * Tạo customer mới (admin)
     */
    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, Object> customerData) {
        try {
            // TODO: Implement customer creation logic
            // This should handle creating both Customer entity and Account if needed
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer creation endpoint - to be implemented");
            response.put("redirectTo", "/admin/customers"); // Navigate back to customers list
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating customer: " + e.getMessage());
        }
    }
    
    /**
     * Cập nhật thông tin customer
     */
    @PutMapping("/customers/{customerId}")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer customerId, 
                                           @RequestBody Map<String, Object> customerData) {
        try {
            Customer existingCustomer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            
            // Update customer fields
            if (customerData.containsKey("customerName")) {
                existingCustomer.setCustomerName((String) customerData.get("customerName"));
            }
            if (customerData.containsKey("customerEmail")) {
                existingCustomer.setCustomerEmail((String) customerData.get("customerEmail"));
            }
            if (customerData.containsKey("customerPhone")) {
                existingCustomer.setCustomerPhone((String) customerData.get("customerPhone"));
            }
            if (customerData.containsKey("customerAddress")) {
                existingCustomer.setCustomerAddress((String) customerData.get("customerAddress"));
            }
            if (customerData.containsKey("customerTypeId")) {
                Integer typeId = (Integer) customerData.get("customerTypeId");
                if (typeId != null) {
                    CustomerType customerType = customerTypeRepository.findById(typeId).orElse(null);
                    existingCustomer.setCustomerType(customerType);
                }
            }
            
            Customer updatedCustomer = customerRepository.save(existingCustomer);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customer", updatedCustomer);
            response.put("message", "Customer updated successfully");
            response.put("redirectTo", "/admin/customers"); // Navigate back to customers list
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating customer: " + e.getMessage());
        }
    }
    
    /**
     * Ban/Unban customer (toggle isEnable status)
     */
    @PutMapping("/customers/{customerId}/toggle-status")
    public ResponseEntity<?> toggleCustomerStatus(@PathVariable Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            
            boolean currentStatus = customer.getIsEnable();
            boolean newStatus = !currentStatus;
            
            customer.setIsEnable(newStatus);
            Customer updatedCustomer = customerRepository.save(customer);
            
            // Also update the associated account status
            if (customer.getAccount() != null) {
                Account account = customer.getAccount();
                account.setIsEnable(newStatus);
                accountRepository.save(account);
                logger.info("Updated account is_enable status to {} for account ID: {}", newStatus, account.getAccountId());
            } else {
                logger.warn("No account found for customer ID: {}", customerId);
            }
            
            String action = newStatus ? "activated" : "banned";
            String message = String.format("Customer %s has been %s successfully", 
                                          customer.getCustomerName(), action);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customer", updatedCustomer);
            response.put("message", message);
            response.put("action", action);
            response.put("newStatus", newStatus);
            response.put("redirectTo", "/admin/customers"); // Navigate back to customers list
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error toggling customer status: " + e.getMessage());
        }
    }
    
    /**
     * Xóa customer (soft delete)
     */
    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            
            // Soft delete by setting isEnable to false
            customer.setIsEnable(false);
            Customer deletedCustomer = customerRepository.save(customer);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customer", deletedCustomer);
            response.put("message", "Customer soft deleted successfully");
            response.put("redirectTo", "/admin/customers"); // Navigate back to customers list
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting customer: " + e.getMessage());
        }
    }

    // ==================== EXERCISE MANAGEMENT ENDPOINTS ====================
    
    /**
     * Toggle exercise status (hide/show)
     */
    @PutMapping("/exercises/{exerciseId}/toggle-status")
    public ResponseEntity<?> toggleExerciseStatus(@PathVariable Integer exerciseId) {
        try {
            Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + exerciseId));
            
            boolean currentStatus = exercise.getIsEnable() != null ? exercise.getIsEnable() : true;
            boolean newStatus = !currentStatus;
            
            exercise.setIsEnable(newStatus);
            Exercise updatedExercise = exerciseRepository.save(exercise);
            
            String action = newStatus ? "enabled" : "disabled";
            String message = String.format("Exercise %s has been %s successfully", 
                                          exercise.getExerciseName(), action);
            
            Map<String, Object> response = new HashMap<>();
            response.put("exercise", updatedExercise);
            response.put("message", message);
            response.put("action", action);
            response.put("newStatus", newStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error toggling exercise status: " + e.getMessage());
        }
    }

    /**
     * Update exercise status with explicit status value
     */
    @PutMapping("/exercise/{exerciseId}/status")
    public ResponseEntity<?> updateExerciseStatus(@PathVariable Integer exerciseId, 
                                                 @RequestBody Map<String, Object> statusData) {
        try {
            Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercise not found with ID: " + exerciseId));
            
            Boolean newStatus = (Boolean) statusData.get("status");
            if (newStatus == null) {
                return ResponseEntity.badRequest().body("Status is required");
            }
            
            exercise.setIsEnable(newStatus);
            Exercise updatedExercise = exerciseRepository.save(exercise);
            
            String action = newStatus ? "enabled" : "disabled";
            String message = String.format("Exercise %s has been %s successfully", 
                                          exercise.getExerciseName(), action);
            
            Map<String, Object> response = new HashMap<>();
            response.put("exercise", updatedExercise);
            response.put("message", message);
            response.put("action", action);
            response.put("newStatus", newStatus);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating exercise status: " + e.getMessage());
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
                    
                    // Add VIP and price information for admin filter
                    courseInfo.put("isVip", course.getIsVip());
                    courseInfo.put("is_vip", course.getIsVip()); // Alternative field name for compatibility
                    courseInfo.put("price", course.getPrice());
                    
                    // Additional course details for admin
                    courseInfo.put("weeklySessions", course.getWeeklySessions());
                    courseInfo.put("recommendedFor", course.getRecommendedFor());
                    courseInfo.put("difficultyLevel", course.getDifficultyLevel());
                    courseInfo.put("createdDate", course.getCreatedDate());
                    courseInfo.put("updatedDate", course.getUpdatedDate());
                    
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
            
            // Add VIP statistics
            long vipCourses = courses.stream().filter(c -> c.getIsVip() != null && c.getIsVip()).count();
            long freeCourses = courses.stream().filter(c -> c.getIsVip() == null || !c.getIsVip()).count();
            response.put("vipCourses", vipCourses);
            response.put("freeCourses", freeCourses);
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch courses: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            // Get the current highest day number for this course to determine next day number
            String getMaxDaySQL = "SELECT MAX(CAST(SUBSTRING(d.day_name, 5) AS UNSIGNED)) as max_day_num " +
                                  "FROM days d " +
                                  "JOIN course_days cd ON d.day_id = cd.day_id " +
                                  "WHERE cd.course_id = :courseId AND cd.status = true " +
                                  "AND d.day_name REGEXP '^Day [0-9]+$'";
            
            Query maxDayQuery = entityManager.createNativeQuery(getMaxDaySQL);
            maxDayQuery.setParameter("courseId", courseId);
            
            Number maxDayNum = (Number) maxDayQuery.getSingleResult();
            int nextDayNumber = (maxDayNum != null) ? maxDayNum.intValue() + 1 : 1;
            
            // Auto-generate day name or use provided name
            String dayName = (String) dayData.get("dayName");
            if (dayName == null || dayName.trim().isEmpty()) {
                dayName = "Day " + nextDayNumber;
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
            response.put("dayNumber", nextDayNumber);
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
            // Get the day number of the day being deleted
            String getDayNumberSQL = "SELECT CAST(SUBSTRING(d.day_name, 5) AS UNSIGNED) as day_num " +
                                     "FROM days d " +
                                     "JOIN course_days cd ON d.day_id = cd.day_id " +
                                     "WHERE cd.course_id = :courseId AND d.day_id = :dayId " +
                                     "AND d.day_name REGEXP '^Day [0-9]+$'";
            
            Query getDayNumberQuery = entityManager.createNativeQuery(getDayNumberSQL);
            getDayNumberQuery.setParameter("courseId", courseId);
            getDayNumberQuery.setParameter("dayId", dayId);
            
            Number deletedDayNumber = null;
            try {
                deletedDayNumber = (Number) getDayNumberQuery.getSingleResult();
            } catch (Exception e) {
                // Day doesn't follow "Day X" format or doesn't exist, proceed with normal deletion
            }
            
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
            
            // If the deleted day followed "Day X" format, renumber subsequent days
            if (deletedDayNumber != null) {
                renumberSubsequentDays(courseId, deletedDayNumber.intValue());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course day deleted successfully");
            response.put("dayId", dayId);
            response.put("courseId", courseId);
            response.put("deletedDayNumber", deletedDayNumber);
            response.put("renumbered", deletedDayNumber != null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete course day: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Helper method to renumber days after deletion
     */
    private void renumberSubsequentDays(Integer courseId, int deletedDayNumber) {
        try {
            // Get all days with numbers higher than the deleted day
            String getSubsequentDaysSQL = "SELECT d.day_id, CAST(SUBSTRING(d.day_name, 5) AS UNSIGNED) as day_num " +
                                          "FROM days d " +
                                          "JOIN course_days cd ON d.day_id = cd.day_id " +
                                          "WHERE cd.course_id = :courseId AND cd.status = true " +
                                          "AND d.day_name REGEXP '^Day [0-9]+$' " +
                                          "AND CAST(SUBSTRING(d.day_name, 5) AS UNSIGNED) > :deletedDayNumber " +
                                          "ORDER BY CAST(SUBSTRING(d.day_name, 5) AS UNSIGNED)";
            
            Query getSubsequentDaysQuery = entityManager.createNativeQuery(getSubsequentDaysSQL);
            getSubsequentDaysQuery.setParameter("courseId", courseId);
            getSubsequentDaysQuery.setParameter("deletedDayNumber", deletedDayNumber);
            
            List<Object[]> subsequentDays = getSubsequentDaysQuery.getResultList();
            
            // Renumber each subsequent day
            for (Object[] dayInfo : subsequentDays) {
                Integer dayId = ((Number) dayInfo[0]).intValue();
                Integer currentDayNum = ((Number) dayInfo[1]).intValue();
                Integer newDayNum = currentDayNum - 1; // Decrease by 1
                
                // Update the day name
                String updateDayNameSQL = "UPDATE days SET day_name = :newDayName WHERE day_id = :dayId";
                Query updateDayNameQuery = entityManager.createNativeQuery(updateDayNameSQL);
                updateDayNameQuery.setParameter("newDayName", "Day " + newDayNum);
                updateDayNameQuery.setParameter("dayId", dayId);
                updateDayNameQuery.executeUpdate();
            }
            
            logger.info("Renumbered {} days after deleting day {} from course {}", 
                       subsequentDays.size(), deletedDayNumber, courseId);
            
        } catch (Exception e) {
            logger.error("Error renumbering subsequent days: {}", e.getMessage());
            // Don't throw the exception as the main deletion was successful
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
            @RequestParam("courseName") String courseName,
            @RequestParam(value = "filename", required = false) String filename) {
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

    /**
     * Check customer ban status and clear sessions if needed
     */
    @GetMapping("/check-customer-ban/{username}")
    public ResponseEntity<?> checkCustomerBanStatus(@PathVariable String username) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            
            // Find customer by username (including banned ones)
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                response.put("error", "Customer not found");
                return ResponseEntity.ok(response);
            }
            
            response.put("customerId", customer.getCustomerId());
            response.put("customerName", customer.getCustomerName());
            response.put("customerEmail", customer.getCustomerEmail());
            response.put("isEnabled", customer.getIsEnable());
            response.put("isBanned", !customer.getIsEnable());
            
            // Check account status
            if (customer.getAccount() != null) {
                response.put("accountEnabled", customer.getAccount().getIsEnable());
                response.put("accountUsername", customer.getAccount().getUserName());
                response.put("accountEmail", customer.getAccount().getEmail());
            }
            
            // Additional message
            if (!customer.getIsEnable()) {
                response.put("message", "Customer is BANNED - should not be able to login");
                response.put("action", "User should be automatically logged out on next request");
            } else {
                response.put("message", "Customer is ACTIVE - can login normally");
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error checking customer ban status: " + e.getMessage());
        }
    }

    /**
     * Test banned customer login prevention
     */
    @PostMapping("/test-banned-login")
    public ResponseEntity<?> testBannedLogin(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            Map<String, Object> response = new HashMap<>();
            response.put("username", username);
            
            // Simulate what UserDetailService does
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                response.put("result", "NO_CUSTOMER_FOUND");
                response.put("message", "No customer found for username");
                return ResponseEntity.ok(response);
            }
            
            if (!customer.getIsEnable()) {
                response.put("result", "BANNED_CUSTOMER");
                response.put("message", "Customer is banned - login should fail");
                response.put("customerId", customer.getCustomerId());
                response.put("customerName", customer.getCustomerName());
                response.put("shouldBlockLogin", true);
            } else {
                response.put("result", "ACTIVE_CUSTOMER");
                response.put("message", "Customer is active - login should succeed");
                response.put("customerId", customer.getCustomerId());
                response.put("customerName", customer.getCustomerName());
                response.put("shouldBlockLogin", false);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error testing banned login: " + e.getMessage());
        }
    }

    /**
     * Get detailed customer info by ID (for debugging banned customer)
     */
    @GetMapping("/customer-detail/{customerId}")
    public ResponseEntity<?> getCustomerDetail(@PathVariable Integer customerId) {
        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            
            if (!customerOpt.isPresent()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Customer not found");
                error.put("customerId", customerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            Customer customer = customerOpt.get();
            Map<String, Object> response = new HashMap<>();
            
            // Customer basic info
            response.put("customerId", customer.getCustomerId());
            response.put("customerCode", customer.getCustomerCode());
            response.put("customerName", customer.getCustomerName());
            response.put("customerEmail", customer.getCustomerEmail());
            response.put("customerPhone", customer.getCustomerPhone());
            response.put("customerAddress", customer.getCustomerAddress());
            response.put("customerGender", customer.getCustomerGender());
            response.put("dateOfBirth", customer.getDateOfBirth());

            response.put("customerImg", customer.getCustomerImg());
            response.put("isEnable", customer.getIsEnable());
            response.put("isBanned", !customer.getIsEnable());
            
            // Customer type info
            if (customer.getCustomerType() != null) {
                Map<String, Object> typeInfo = new HashMap<>();
                typeInfo.put("customerTypeId", customer.getCustomerType().getCustomerTypeId());
                typeInfo.put("customerTypeName", customer.getCustomerType().getCustomerTypeName());
                typeInfo.put("price", customer.getCustomerType().getPrice());
                response.put("customerType", typeInfo);
            } else {
                response.put("customerType", null);
            }
            
            // Account info
            if (customer.getAccount() != null) {
                Map<String, Object> accountInfo = new HashMap<>();
                accountInfo.put("accountId", customer.getAccount().getAccountId());
                accountInfo.put("userName", customer.getAccount().getUserName());
                accountInfo.put("email", customer.getAccount().getEmail());
                accountInfo.put("isEnable", customer.getAccount().getIsEnable());
                // accountInfo.put("createDate", customer.getAccount().getCreateDate()); // Field might not exist
                
                // Roles
                if (customer.getAccount().getRoles() != null) {
                    List<String> roleNames = customer.getAccount().getRoles().stream()
                        .map(role -> role.getRoleName())
                        .collect(Collectors.toList());
                    accountInfo.put("roles", roleNames);
                }
                
                response.put("account", accountInfo);
            } else {
                response.put("account", null);
            }
            
            // Status message
            if (!customer.getIsEnable()) {
                response.put("statusMessage", "CUSTOMER IS BANNED - Should not be able to login or access any endpoints");
                response.put("loginAllowed", false);
            } else {
                response.put("statusMessage", "Customer is active");
                response.put("loginAllowed", true);
            }
            
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting customer detail: " + e.getMessage());
        }
    }

    @PostMapping("/courses/delete-image")
    public ResponseEntity<?> deleteImage(@RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            if (imageUrl == null || imageUrl.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "Image URL is required");
                error.put("status", "error");
                return ResponseEntity.badRequest().body(error);
            }
            
            // Delete the image using the service
            firebaseStorageService.deleteFile(imageUrl);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Image deleted successfully");
            response.put("status", "success");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to delete image: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to delete image: " + e.getMessage());
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cleanup problematic image paths in courses
     */
    @PostMapping("/courses/cleanup-images")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cleanupCourseImages() {
        try {
            // Find all courses with problematic image paths
            List<Course> allCourses = courseRepository.findAll();
            List<Course> coursesToUpdate = new ArrayList<>();
            
            for (Course course : allCourses) {
                String imageUrl = course.getImage();
                if (imageUrl != null && imageUrl.contains("/assets/images/default-course.jpg")) {
                    course.setImage(null); // Set to null instead of empty string
                    course.setUpdatedDate(LocalDateTime.now());
                    coursesToUpdate.add(course);
                }
            }
            
            if (!coursesToUpdate.isEmpty()) {
                courseRepository.saveAll(coursesToUpdate);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course images cleanup completed");
            response.put("coursesUpdated", coursesToUpdate.size());
            response.put("updatedCourseIds", coursesToUpdate.stream()
                .map(Course::getCourseId)
                .collect(Collectors.toList()));
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to cleanup course images: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to cleanup course images: " + e.getMessage());
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // ==================== CUSTOMER TOTAL VIP SPENT MANAGEMENT ====================
    
    /**
     * Đồng bộ hóa total_vip_spent cho tất cả customers từ lịch sử thanh toán
     */
    @PostMapping("/sync-customer-vip-spending")
    public ResponseEntity<?> syncCustomerVipSpending() {
        try {
            logger.info("Starting synchronization of customer VIP spending...");
            
            int updatedCount = paymentStatusService.recalculateAllCustomerTotalVipSpent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer VIP spending synchronized successfully");
            response.put("updatedCustomers", updatedCount);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Customer VIP spending sync completed for {} customers", updatedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during customer VIP spending synchronization", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to synchronize customer VIP spending: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Tự động phân loại lại customer types dựa trên total_vip_spent
     */
    @PostMapping("/auto-classify-customer-types")
    public ResponseEntity<?> autoClassifyCustomerTypes() {
        try {
            logger.info("Starting automatic customer type classification...");
            
            int updatedCount = paymentStatusService.updateAllCustomerTypesBasedOnSpent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Customer types classified successfully based on VIP spending");
            response.put("updatedCustomers", updatedCount);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Customer type classification completed for {} customers", updatedCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during customer type classification", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to classify customer types: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Thực hiện full sync: đồng bộ spending + phân loại types
     */
    @PostMapping("/full-sync-customer-data")
    public ResponseEntity<?> fullSyncCustomerData() {
        try {
            logger.info("Starting full customer data synchronization...");
            
            // Bước 1: Đồng bộ total_vip_spent
            int spendingSyncCount = paymentStatusService.recalculateAllCustomerTotalVipSpent();
            
            // Bước 2: Phân loại customer types
            int typeUpdateCount = paymentStatusService.updateAllCustomerTypesBasedOnSpent();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Full customer data synchronization completed successfully");
            response.put("spendingSyncCount", spendingSyncCount);
            response.put("typeUpdateCount", typeUpdateCount);
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Full sync completed - Spending: {} customers, Types: {} customers", 
                spendingSyncCount, typeUpdateCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error during full customer data synchronization", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to synchronize customer data: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy thống kê chi tiết về customer VIP spending
     */
    @GetMapping("/customer-vip-spending-stats")
    public ResponseEntity<?> getCustomerVipSpendingStats() {
        try {
            List<Customer> allCustomers = customerRepository.findAll();
            
            // Thống kê tổng quan
            BigDecimal totalVipSpending = allCustomers.stream()
                .filter(c -> c.getTotalVipSpent() != null)
                .map(Customer::getTotalVipSpent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
            long customersWithSpending = allCustomers.stream()
                .filter(c -> c.getTotalVipSpent() != null && c.getTotalVipSpent().compareTo(BigDecimal.ZERO) > 0)
                .count();
                
            // Top 10 customers by spending
            List<Map<String, Object>> topSpenders = allCustomers.stream()
                .filter(c -> c.getTotalVipSpent() != null && c.getTotalVipSpent().compareTo(BigDecimal.ZERO) > 0)
                .sorted((c1, c2) -> c2.getTotalVipSpent().compareTo(c1.getTotalVipSpent()))
                .limit(10)
                .map(customer -> {
                    Map<String, Object> customerInfo = new HashMap<>();
                    customerInfo.put("customerId", customer.getCustomerId());
                    customerInfo.put("customerName", customer.getCustomerName());
                    customerInfo.put("totalVipSpent", customer.getTotalVipSpent());
                    customerInfo.put("customerType", customer.getCustomerType() != null ? 
                        customer.getCustomerType().getCustomerTypeName() : "No Type");
                    return customerInfo;
                })
                .collect(Collectors.toList());
                
            // Thống kê theo customer type
            Map<String, Object> typeStats = allCustomers.stream()
                .filter(c -> c.getCustomerType() != null && c.getTotalVipSpent() != null)
                .collect(Collectors.groupingBy(
                    c -> c.getCustomerType().getCustomerTypeName(),
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        customers -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("count", customers.size());
                            stats.put("totalSpending", customers.stream()
                                .map(Customer::getTotalVipSpent)
                                .reduce(BigDecimal.ZERO, BigDecimal::add));
                            stats.put("averageSpending", customers.isEmpty() ? BigDecimal.ZERO :
                                customers.stream()
                                    .map(Customer::getTotalVipSpent)
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(BigDecimal.valueOf(customers.size()), 2, BigDecimal.ROUND_HALF_UP));
                            return stats;
                        }
                    )
                ));
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalCustomers", allCustomers.size());
            response.put("customersWithSpending", customersWithSpending);
            response.put("totalVipSpending", totalVipSpending);
            response.put("averageSpending", customersWithSpending > 0 ? 
                totalVipSpending.divide(BigDecimal.valueOf(customersWithSpending), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO);
            response.put("topSpenders", topSpenders);
            response.put("typeStats", typeStats);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting customer VIP spending stats", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get spending stats: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // ==================== CUSTOMER COURSES MANAGEMENT ====================
    
    /**
     * Lấy danh sách tất cả courses mà customer đã tham gia
     */
    @GetMapping("/customers/{customerId}/courses")
    @Transactional
    public ResponseEntity<?> getCustomerCourses(@PathVariable Integer customerId) {
        try {
            logger.info("=== Admin Customer Courses API Called for Customer ID: {} ===", customerId);
            
            // Kiểm tra customer có tồn tại không
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            
            logger.info("Customer found: {} (ID: {})", customer.getCustomerName(), customer.getCustomerId());
            
            // Lấy danh sách courses của customer từ customer_course table
            // Fixed query: removed cc.status (non-existent column) and cc.enrollment_date, cc.completion_date
            String sql = "SELECT " +
                "cc.customer_course_id, cc.customer_id, cc.course_id, " +
                "'Active' as enrollment_status, " +  // Default status since table doesn't have status column
                "COALESCE(cc.recommended_status, false) as recommended_status, " +
                "c.course_name, c.description, c.duration, c.image, " +
                "COALESCE(c.price, 0) as price, COALESCE(c.is_vip, false) as is_vip, " +
                "c.difficulty_level, c.recommended_for, COALESCE(c.weekly_sessions, 1) as weekly_sessions, " +
                "COALESCE(ct.course_type_name, 'Unknown') as course_type_name " +
                "FROM customer_course cc " +
                "JOIN course c ON cc.course_id = c.course_id " +
                "LEFT JOIN course_type ct ON c.course_type_id = ct.course_type_id " +
                "WHERE cc.customer_id = :customerId " +
                "ORDER BY cc.customer_course_id DESC";
            
            logger.info("Executing main courses query for customer {}", customerId);
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("customerId", customerId);
            
            @SuppressWarnings("unchecked")
            List<Object[]> results = query.getResultList();
            logger.info("Found {} courses for customer {}", results.size(), customerId);
            
            List<Map<String, Object>> customerCourses = new ArrayList<>();
            
            for (Object[] row : results) {
                try {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("customerCourseId", row[0]);
                    courseInfo.put("customerId", row[1]);
                    courseInfo.put("courseId", row[2]);
                    courseInfo.put("enrollmentStatus", row[3]);  // "Active"
                    courseInfo.put("recommendedStatus", row[4]);
                    courseInfo.put("courseName", row[5]);
                    courseInfo.put("description", row[6]);
                    courseInfo.put("duration", row[7]);
                    courseInfo.put("image", row[8]);
                    courseInfo.put("price", row[9]);
                    courseInfo.put("isVip", row[10]);
                    courseInfo.put("difficultyLevel", row[11]);
                    courseInfo.put("recommendedFor", row[12]);
                    courseInfo.put("weeklySessions", row[13]);
                    courseInfo.put("courseTypeName", row[14]);
                    
                    // Set default enrollment and completion dates since table doesn't have them
                    courseInfo.put("enrollmentDate", null);
                    courseInfo.put("completionDate", null);
                    
                    // Get progress data separately for each course to avoid complex joins
                    Integer courseId = (Integer) row[2];
                    try {
                        // Đếm tổng số ngày từ bảng course_days (đúng như logic trong CustomerDayCompletionService)
                        String totalDaysSql = "SELECT COUNT(*) FROM course_days WHERE course_id = ? AND status = true";
                        Query totalDaysQuery = entityManager.createNativeQuery(totalDaysSql);
                        totalDaysQuery.setParameter(1, courseId);
                        Number totalDays = (Number) totalDaysQuery.getSingleResult();
                        
                        // Kiểm tra và khởi tạo completion records nếu cần
                        if (totalDays.intValue() > 0) {
                            String checkCompletionSql = "SELECT COUNT(*) FROM customer_day_completion " +
                                "WHERE customer_id = ? AND course_id = ?";
                            Query checkCompletionQuery = entityManager.createNativeQuery(checkCompletionSql);
                            checkCompletionQuery.setParameter(1, customerId);
                            checkCompletionQuery.setParameter(2, courseId);
                            Number existingRecords = (Number) checkCompletionQuery.getSingleResult();
                            
                            if (existingRecords.intValue() == 0) {
                                logger.info("No completion records found for customer {} course {}, initializing...", 
                                    customerId, courseId);
                                
                                // Khởi tạo completion records từ course_days
                                String initSql = "INSERT INTO customer_day_completion " +
                                    "(customer_id, course_id, day_id, is_completed, completion_date) " +
                                    "SELECT ?, ?, cd.day_id, false, NULL " +
                                    "FROM course_days cd " +
                                    "WHERE cd.course_id = ? AND cd.status = true";
                                Query initQuery = entityManager.createNativeQuery(initSql);
                                initQuery.setParameter(1, customerId);
                                initQuery.setParameter(2, courseId);
                                initQuery.setParameter(3, courseId);
                                initQuery.executeUpdate();
                                
                                logger.info("Initialized {} completion records for customer {} course {}", 
                                    totalDays, customerId, courseId);
                            }
                        }
                        
                        // Đếm số ngày đã completed từ bảng customer_day_completion
                        String completedDaysSql = "SELECT COUNT(*) FROM customer_day_completion " +
                            "WHERE customer_id = ? AND course_id = ? AND is_completed = true";
                        Query completedDaysQuery = entityManager.createNativeQuery(completedDaysSql);
                        completedDaysQuery.setParameter(1, customerId);
                        completedDaysQuery.setParameter(2, courseId);
                        Number completedDays = (Number) completedDaysQuery.getSingleResult();
                        
                        logger.info("Course {} - Total days from course_days: {}, Completed days: {}", 
                                courseId, totalDays, completedDays);
                        
                        courseInfo.put("completedDays", completedDays != null ? completedDays.intValue() : 0);
                        courseInfo.put("totalDays", totalDays != null ? totalDays.intValue() : 0);
                        
                        if (totalDays != null && totalDays.intValue() > 0) {
                            double percentage = (completedDays.doubleValue() / totalDays.doubleValue()) * 100.0;
                            courseInfo.put("completionPercentage", Math.round(percentage * 100.0) / 100.0);
                        } else {
                            courseInfo.put("completionPercentage", 0.0);
                        }
                        
                    } catch (Exception progressError) {
                        logger.warn("Could not get progress for course {} customer {}: {}", courseId, customerId, progressError.getMessage());
                        courseInfo.put("completedDays", 0);
                        courseInfo.put("totalDays", 0);
                        courseInfo.put("completionPercentage", 0.0);
                    }
                    
                    customerCourses.add(courseInfo);
                    
                } catch (Exception rowError) {
                    logger.error("Error processing course row for customer {}: {}", customerId, rowError.getMessage());
                    // Continue with other rows
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> customerInfo = new HashMap<>();
            customerInfo.put("customerId", customer.getCustomerId());
            customerInfo.put("customerName", customer.getCustomerName());
            customerInfo.put("customerEmail", customer.getCustomerEmail());
            customerInfo.put("customerType", customer.getCustomerType() != null ? 
                customer.getCustomerType().getCustomerTypeName() : "No Type");
            
            response.put("customer", customerInfo);
            response.put("courses", customerCourses);
            response.put("totalCourses", customerCourses.size());
            response.put("timestamp", LocalDateTime.now());
            
            logger.info("Successfully returning {} courses for customer {}", customerCourses.size(), customerId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting customer courses for customer ID: " + customerId, e);
            logger.error("Full error stack trace: ", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get customer courses: " + e.getMessage());
            errorResponse.put("customerId", customerId);
            errorResponse.put("timestamp", LocalDateTime.now());
            errorResponse.put("details", e.getClass().getSimpleName());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Lấy chi tiết tiến độ của một course cụ thể của customer
     */
    @GetMapping("/customers/{customerId}/courses/{courseId}/progress")
    public ResponseEntity<?> getCustomerCourseProgress(
            @PathVariable Integer customerId, 
            @PathVariable Integer courseId) {
        try {
            // Kiểm tra customer course có tồn tại không
            String checkSql = "SELECT COUNT(*) FROM customer_course " +
                "WHERE customer_id = :customerId AND course_id = :courseId";
            Query checkQuery = entityManager.createNativeQuery(checkSql);
            checkQuery.setParameter("customerId", customerId);
            checkQuery.setParameter("courseId", courseId);
            
            Number count = (Number) checkQuery.getSingleResult();
            if (count.intValue() == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Customer is not enrolled in this course");
            }
            
            // Lấy thông tin chi tiết từng ngày
            String progressSql = "SELECT " +
                "d.day_id, d.day_name, cd.course_id, cd.status as day_status, " +
                "COALESCE(cdc.is_completed, false) as is_completed, " +
                "cdc.completion_date, " +
                "COALESCE(COUNT(ed.exercise_id), 0) as total_exercises, " +
                "cdc.created_at, cdc.updated_at " +
                "FROM course_days cd " +
                "INNER JOIN days d ON cd.day_id = d.day_id " +
                "LEFT JOIN customer_day_completion cdc ON cd.day_id = cdc.day_id " +
                "    AND cdc.customer_id = :customerId AND cdc.course_id = :courseId " +
                "LEFT JOIN exercise_day ed ON cd.day_id = ed.day_id AND ed.course_id = cd.course_id " +
                "WHERE cd.course_id = :courseId " +
                "GROUP BY d.day_id, d.day_name, cd.course_id, cd.status, " +
                "    cdc.is_completed, cdc.completion_date, cdc.created_at, cdc.updated_at " +
                "ORDER BY d.day_id";
            
            Query progressQuery = entityManager.createNativeQuery(progressSql);
            progressQuery.setParameter("customerId", customerId);
            progressQuery.setParameter("courseId", courseId);
            
            @SuppressWarnings("unchecked")
            List<Object[]> progressResults = progressQuery.getResultList();
            
            List<Map<String, Object>> dailyProgress = new ArrayList<>();
            int completedDays = 0;
            
            for (Object[] row : progressResults) {
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("dayId", row[0]);
                dayInfo.put("dayName", row[1]);
                dayInfo.put("courseId", row[2]);
                dayInfo.put("dayStatus", row[3]);
                dayInfo.put("isCompleted", row[4]);
                dayInfo.put("completionDate", row[5]);
                dayInfo.put("totalExercises", row[6]);
                dayInfo.put("createdAt", row[7]);
                dayInfo.put("updatedAt", row[8]);
                
                // Format completion date for better display
                if (row[5] != null) {
                    try {
                        java.sql.Timestamp timestamp = (java.sql.Timestamp) row[5];
                        java.time.LocalDateTime completionDateTime = timestamp.toLocalDateTime();
                        dayInfo.put("completionDateFormatted", completionDateTime.toString());
                        dayInfo.put("completionDateDisplay", completionDateTime.format(
                            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        ));
                    } catch (Exception e) {
                        logger.warn("Error formatting completion date for day {}: {}", row[0], e.getMessage());
                    }
                }
                
                if (Boolean.TRUE.equals(row[4])) {
                    completedDays++;
                }
                
                dailyProgress.add(dayInfo);
            }
            
            // Lấy thông tin body metrics trước và sau course
            String metricsSql = "SELECT " +
                "ud.user_data_id, ud.customer_id, ud.age, ud.bmi, ud.weight, ud.height, " +
                "ud.training_goals, ud.training_history, ud.created_date " +
                "FROM user_data_ai ud " +
                "WHERE ud.customer_id = :customerId " +
                "ORDER BY ud.created_date";
            
            Query metricsQuery = entityManager.createNativeQuery(metricsSql);
            metricsQuery.setParameter("customerId", customerId);
            
            @SuppressWarnings("unchecked")
            List<Object[]> metricsResults = metricsQuery.getResultList();
            
            Map<String, Object> bodyMetrics = new HashMap<>();
            if (!metricsResults.isEmpty()) {
                Object[] firstRecord = metricsResults.get(0);
                Object[] lastRecord = metricsResults.get(metricsResults.size() - 1);
                
                Map<String, Object> beforeMetrics = new HashMap<>();
                beforeMetrics.put("bmi", firstRecord[3] != null ? firstRecord[3] : 0);
                beforeMetrics.put("weight", firstRecord[4] != null ? firstRecord[4] : 0);
                beforeMetrics.put("height", firstRecord[5] != null ? firstRecord[5] : 0);
                beforeMetrics.put("recordDate", firstRecord[8]);
                bodyMetrics.put("before", beforeMetrics);
                
                if (metricsResults.size() > 1) {
                    Map<String, Object> afterMetrics = new HashMap<>();
                    afterMetrics.put("bmi", lastRecord[3] != null ? lastRecord[3] : 0);
                    afterMetrics.put("weight", lastRecord[4] != null ? lastRecord[4] : 0);
                    afterMetrics.put("height", lastRecord[5] != null ? lastRecord[5] : 0);
                    afterMetrics.put("recordDate", lastRecord[8]);
                    bodyMetrics.put("after", afterMetrics);
                } else {
                    bodyMetrics.put("after", "No updated metrics available");
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("courseId", courseId);
            response.put("customerId", customerId);
            response.put("totalDays", dailyProgress.size());
            response.put("completedDays", completedDays);
            response.put("completionPercentage", dailyProgress.size() > 0 ? 
                (completedDays * 100.0 / dailyProgress.size()) : 0);
            response.put("dailyProgress", dailyProgress);
            response.put("bodyMetrics", bodyMetrics);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting customer course progress for customer {} and course {}", 
                customerId, courseId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get course progress: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Tạo course mới với tự động tạo course_days
     */
    @PostMapping("/courses/create-with-days")
    public ResponseEntity<?> createCourseWithDays(@RequestBody Map<String, Object> courseData) {
        try {
            logger.info("Creating new course with auto-generated days: {}", courseData);
            
            // Validate required fields
            if (!courseData.containsKey("courseName") || courseData.get("courseName") == null ||
                courseData.get("courseName").toString().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Course name is required");
            }
            
            if (!courseData.containsKey("duration") || courseData.get("duration") == null) {
                return ResponseEntity.badRequest().body("Duration (weeks) is required");
            }
            
            if (!courseData.containsKey("weeklySessionss") || courseData.get("weeklySessionss") == null) {
                return ResponseEntity.badRequest().body("Weekly sessions is required");
            }
            
            if (!courseData.containsKey("courseTypeId") || courseData.get("courseTypeId") == null) {
                return ResponseEntity.badRequest().body("Course type is required");
            }
            
            // Extract course data
            String courseName = courseData.get("courseName").toString().trim();
            String description = courseData.get("description") != null ? 
                courseData.get("description").toString() : "";
            Integer durationWeeks = Integer.valueOf(courseData.get("duration").toString());
            Integer weeklySessions = Integer.valueOf(courseData.get("weeklySessionss").toString());
            Integer courseTypeId = Integer.valueOf(courseData.get("courseTypeId").toString());
            
            // Calculate total days
            Integer totalDays = durationWeeks * weeklySessions;
            
            // Auto-determine VIP status based on price
            Double price = courseData.get("price") != null ? 
                Double.valueOf(courseData.get("price").toString()) : 0.0;
            Boolean isVip = price > 0;
            
            // Set default values
            Boolean status = courseData.get("status") != null ? 
                Boolean.valueOf(courseData.get("status").toString()) : true;
            String recommendedFor = courseData.get("recommendedFor") != null ? 
                courseData.get("recommendedFor").toString() : "Normal";
            String difficultyLevel = courseData.get("difficultyLevel") != null ? 
                courseData.get("difficultyLevel").toString() : "Medium";
            String image = courseData.get("image") != null ? 
                courseData.get("image").toString() : "";
            
            // Create course insert SQL
            String insertCourseSql = "INSERT INTO course " +
                "(course_name, description, duration, status, price, is_vip, " +
                "recommended_for, difficulty_level, weekly_sessions, course_type_id, image) " +
                "VALUES (:courseName, :description, :totalDays, :status, :price, :isVip, " +
                ":recommendedFor, :difficultyLevel, :weeklySessions, :courseTypeId, :image)";
            
            Query insertCourseQuery = entityManager.createNativeQuery(insertCourseSql);
            insertCourseQuery.setParameter("courseName", courseName);
            insertCourseQuery.setParameter("description", description);
            insertCourseQuery.setParameter("totalDays", totalDays);
            insertCourseQuery.setParameter("status", status);
            insertCourseQuery.setParameter("price", price);
            insertCourseQuery.setParameter("isVip", isVip);
            insertCourseQuery.setParameter("recommendedFor", recommendedFor);
            insertCourseQuery.setParameter("difficultyLevel", difficultyLevel);
            insertCourseQuery.setParameter("weeklySessions", weeklySessions);
            insertCourseQuery.setParameter("courseTypeId", courseTypeId);
            insertCourseQuery.setParameter("image", image);
            
            int insertedRows = insertCourseQuery.executeUpdate();
            
            if (insertedRows == 0) {
                throw new RuntimeException("Failed to create course");
            }
            
            // Get the generated course ID
            String getIdSql = "SELECT LAST_INSERT_ID()";
            Query getIdQuery = entityManager.createNativeQuery(getIdSql);
            Number courseId = (Number) getIdQuery.getSingleResult();
            
            logger.info("Created course with ID: {}", courseId);
            
            // Create course_days automatically
            List<Map<String, Object>> createdDays = new ArrayList<>();
            for (int i = 1; i <= totalDays; i++) {
                String dayName = "Day " + i;
                
                String insertDaySql = "INSERT INTO course_days (day_name, course_id, status) " +
                    "VALUES (:dayName, :courseId, true)";
                
                Query insertDayQuery = entityManager.createNativeQuery(insertDaySql);
                insertDayQuery.setParameter("dayName", dayName);
                insertDayQuery.setParameter("courseId", courseId);
                
                insertDayQuery.executeUpdate();
                
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("dayName", dayName);
                dayInfo.put("dayNumber", i);
                createdDays.add(dayInfo);
            }
            
            logger.info("Created {} course days for course ID: {}", totalDays, courseId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Course created successfully with auto-generated days");
            response.put("courseId", courseId);
            response.put("courseName", courseName);
            response.put("durationWeeks", durationWeeks);
            response.put("weeklySessions", weeklySessions);
            response.put("totalDays", totalDays);
            response.put("isVip", isVip);
            response.put("createdDays", createdDays);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error creating course with days", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create course: " + e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Debug endpoint để kiểm tra dữ liệu course và sync nếu cần
     */
    @GetMapping("/debug/customers/{customerId}/courses")
    public ResponseEntity<?> debugCustomerCourses(@PathVariable Integer customerId) {
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // 1. Kiểm tra customer
            Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
            debugInfo.put("customer", customer.getCustomerName());
            
            // 2. Lấy courses của customer
            String coursesSql = "SELECT cc.course_id, c.course_name, c.duration " +
                "FROM customer_course cc JOIN course c ON cc.course_id = c.course_id " +
                "WHERE cc.customer_id = ?";
            Query coursesQuery = entityManager.createNativeQuery(coursesSql);
            coursesQuery.setParameter(1, customerId);
            
            @SuppressWarnings("unchecked")
            List<Object[]> courses = coursesQuery.getResultList();
            
            List<Map<String, Object>> courseDetails = new ArrayList<>();
            
            for (Object[] row : courses) {
                Integer courseId = (Integer) row[0];
                String courseName = (String) row[1];
                String duration = (String) row[2];
                
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("courseId", courseId);
                courseInfo.put("courseName", courseName);
                courseInfo.put("duration", duration);
                
                // Kiểm tra course_days
                String courseDaysSql = "SELECT COUNT(*) FROM course_days WHERE course_id = ? AND status = true";
                Query courseDaysQuery = entityManager.createNativeQuery(courseDaysSql);
                courseDaysQuery.setParameter(1, courseId);
                Number totalDaysInCourseDays = (Number) courseDaysQuery.getSingleResult();
                courseInfo.put("totalDaysInCourseDays", totalDaysInCourseDays.intValue());
                
                // Kiểm tra completion records
                String completionSql = "SELECT COUNT(*) as total, " +
                    "SUM(CASE WHEN is_completed = true THEN 1 ELSE 0 END) as completed " +
                    "FROM customer_day_completion WHERE customer_id = ? AND course_id = ?";
                Query completionQuery = entityManager.createNativeQuery(completionSql);
                completionQuery.setParameter(1, customerId);
                completionQuery.setParameter(2, courseId);
                
                Object[] completionResult = (Object[]) completionQuery.getSingleResult();
                Number totalCompletionRecords = (Number) completionResult[0];
                Number completedDays = (Number) completionResult[1];
                
                courseInfo.put("totalCompletionRecords", totalCompletionRecords != null ? totalCompletionRecords.intValue() : 0);
                courseInfo.put("completedDays", completedDays != null ? completedDays.intValue() : 0);
                
                // Tính phần trăm
                if (totalDaysInCourseDays.intValue() > 0) {
                    double percentage = (completedDays.doubleValue() / totalDaysInCourseDays.doubleValue()) * 100.0;
                    courseInfo.put("calculatedPercentage", Math.round(percentage * 100.0) / 100.0);
                } else {
                    courseInfo.put("calculatedPercentage", 0.0);
                }
                
                courseDetails.add(courseInfo);
            }
            
            debugInfo.put("courses", courseDetails);
            debugInfo.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            logger.error("Error in debug endpoint for customer {}", customerId, e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("customerId", customerId);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 