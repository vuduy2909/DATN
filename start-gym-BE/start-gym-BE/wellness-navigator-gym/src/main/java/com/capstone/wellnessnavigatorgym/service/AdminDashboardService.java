package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.DashboardStatsDTO;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminDashboardService.class);
    
    private final ICustomerRepository customerRepository;
    private final ICourseRepository courseRepository;
    private final IVipPaymentRepository vipPaymentRepository;
    private final IAccountRepository accountRepository;
    
    public DashboardStatsDTO getDashboardStats() {
        logger.info("=== GETTING DASHBOARD STATS ===");
        
        try {
            DashboardStatsDTO.UserStatsDTO userStats = getUserStats();
            logger.info("User stats: {}", userStats);
            
            DashboardStatsDTO.CourseStatsDTO courseStats = getCourseStats();
            logger.info("Course stats: {}", courseStats);
            
            DashboardStatsDTO.RevenueStatsDTO revenueStats = getRevenueStats();
            logger.info("Revenue stats: {}", revenueStats);
            
            DashboardStatsDTO.OrderStatsDTO orderStats = getOrderStats();
            logger.info("Order stats: {}", orderStats);
            
            DashboardStatsDTO.RegistrationStatsDTO registrationStats = getRegistrationStats();
            logger.info("Registration stats: {}", registrationStats);
            
            List<DashboardStatsDTO.RecentActivityDTO> recentActivities = getRecentActivities();
            logger.info("Recent activities count: {}", recentActivities.size());
            
            DashboardStatsDTO result = DashboardStatsDTO.builder()
                    .userStats(userStats)
                    .courseStats(courseStats)
                    .revenueStats(revenueStats)
                    .orderStats(orderStats)
                    .registrationStats(registrationStats)
                    .recentActivities(recentActivities)
                    .build();
            
            logger.info("=== DASHBOARD STATS COMPLETED ===");
            return result;
        } catch (Exception e) {
            logger.error("Error getting dashboard stats", e);
            // Return default stats if there's an error
            return createDefaultDashboardStats();
        }
    }
    
    public DashboardStatsDTO getTestDashboardStats() {
        logger.info("=== GETTING TEST DASHBOARD STATS ===");
        
        // Return hardcoded test data to verify API connection
        DashboardStatsDTO.UserStatsDTO userStats = DashboardStatsDTO.UserStatsDTO.builder()
                .total(1234L)
                .regular(800L)
                .premium(300L)
                .vip(134L)
                .build();
        
        DashboardStatsDTO.CourseStatsDTO courseStats = DashboardStatsDTO.CourseStatsDTO.builder()
                .total(50L)
                .active(45L)
                .hidden(5L)
                .newThisWeek(3L)
                .newThisMonth(12L)
                .build();
        
        DashboardStatsDTO.RevenueStatsDTO revenueStats = DashboardStatsDTO.RevenueStatsDTO.builder()
                .total(125000000.0)
                .vip(80000000.0)
                .premium(45000000.0)
                .thisMonth(15000000.0)
                .thisWeek(3500000.0)
                .build();
        
        DashboardStatsDTO.OrderStatsDTO orderStats = DashboardStatsDTO.OrderStatsDTO.builder()
                .total(500L)
                .pending(8L)
                .success(480L)
                .failed(12L)
                .build();
        
        DashboardStatsDTO.RegistrationStatsDTO registrationStats = DashboardStatsDTO.RegistrationStatsDTO.builder()
                .today(15L)
                .thisWeek(95L)
                .thisMonth(350L)
                .build();
        
        List<DashboardStatsDTO.RecentActivityDTO> recentActivities = new ArrayList<>();
        recentActivities.add(DashboardStatsDTO.RecentActivityDTO.builder()
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .type("Course Purchase")
                .userName("Nguyễn Văn A")
                .status("SUCCESS")
                .amount(1500000.0)
                .description("Purchased course: Advanced Total Body")
                .build());
        
        DashboardStatsDTO result = DashboardStatsDTO.builder()
                .userStats(userStats)
                .courseStats(courseStats)
                .revenueStats(revenueStats)
                .orderStats(orderStats)
                .registrationStats(registrationStats)
                .recentActivities(recentActivities)
                .build();
        
        logger.info("=== TEST DASHBOARD STATS COMPLETED ===");
        return result;
    }
    
    public DashboardStatsDTO.UserStatsDTO getUserStats() {
        logger.info("Getting user stats...");
        
        try {
            // Tổng số customer (trừ admin) - chỉ các customer có account và đang active
            Long totalUsers = customerRepository.countAllActiveCustomers();
            logger.info("Total active customers: {}", totalUsers);
            
            // Phân loại customer theo customer_type_id
            // Giả sử: customer_type_id = 1 là Regular, 2 là Premium, 3 là VIP
            Long regularUsers = customerRepository.countCustomersByType(1);
            Long premiumUsers = customerRepository.countCustomersByType(2);
            Long vipUsers = customerRepository.countCustomersByType(3);
            
            logger.info("Customer breakdown - Regular: {}, Premium: {}, VIP: {}", 
                    regularUsers, premiumUsers, vipUsers);
            
            // Handle null values
            totalUsers = (totalUsers != null) ? totalUsers : 0L;
            regularUsers = (regularUsers != null) ? regularUsers : 0L;
            premiumUsers = (premiumUsers != null) ? premiumUsers : 0L;
            vipUsers = (vipUsers != null) ? vipUsers : 0L;
            
            logger.info("Final user stats - Total: {}, Regular: {}, Premium: {}, VIP: {}", 
                    totalUsers, regularUsers, premiumUsers, vipUsers);
            
            return DashboardStatsDTO.UserStatsDTO.builder()
                    .total(totalUsers)
                    .regular(regularUsers)
                    .premium(premiumUsers)
                    .vip(vipUsers)
                    .build();
        } catch (Exception e) {
            logger.error("Error getting user stats", e);
            // Return default values if there's an error
            return DashboardStatsDTO.UserStatsDTO.builder()
                    .total(0L)
                    .regular(0L)
                    .premium(0L)
                    .vip(0L)
                    .build();
        }
    }
    
    public DashboardStatsDTO.CourseStatsDTO getCourseStats() {
        logger.info("Getting course stats...");
        
        try {
            // Khóa học đang hoạt động (status = true trong collection)
            Long activeCourses = courseRepository.countActiveCourses();
            Long hiddenCourses = courseRepository.countHiddenCourses();
            Long totalCourses = courseRepository.count();
            
            logger.info("Course breakdown - Active: {}, Hidden: {}, Total: {}", 
                    activeCourses, hiddenCourses, totalCourses);
            
            // Placeholder cho new courses - có thể implement sau nếu có trường created_date
            Long newThisWeek = 0L;
            Long newThisMonth = 0L;
            
            return DashboardStatsDTO.CourseStatsDTO.builder()
                    .total(totalCourses)
                    .active(activeCourses != null ? activeCourses : 0L)
                    .hidden(hiddenCourses != null ? hiddenCourses : 0L)
                    .newThisWeek(newThisWeek)
                    .newThisMonth(newThisMonth)
                    .build();
        } catch (Exception e) {
            logger.error("Error getting course stats", e);
            return DashboardStatsDTO.CourseStatsDTO.builder()
                    .total(0L)
                    .active(0L)
                    .hidden(0L)
                    .newThisWeek(0L)
                    .newThisMonth(0L)
                    .build();
        }
    }
    
    public DashboardStatsDTO.RevenueStatsDTO getRevenueStats() {
        logger.info("Getting revenue stats...");
        
        try {
            // Tổng doanh thu tháng hiện tại từ vip_payment với status SUCCESS
            Double revenueThisMonth = vipPaymentRepository.getTotalRevenueThisMonth();
            logger.info("Revenue this month: {}", revenueThisMonth);
            
            // Tổng doanh thu tất cả
            Double totalRevenue = vipPaymentRepository.getTotalRevenue();
            logger.info("Total revenue: {}", totalRevenue);
            
            // Tạm thời set vip và premium revenue bằng total revenue
            // Có thể refine sau nếu cần phân chia chi tiết hơn
            Double vipRevenue = totalRevenue;
            Double premiumRevenue = 0.0;
            
            return DashboardStatsDTO.RevenueStatsDTO.builder()
                    .total(totalRevenue != null ? totalRevenue : 0.0)
                    .vip(vipRevenue != null ? vipRevenue : 0.0)
                    .premium(premiumRevenue)
                    .thisMonth(revenueThisMonth != null ? revenueThisMonth : 0.0)
                    .thisWeek(0.0) // Placeholder
                    .build();
        } catch (Exception e) {
            logger.error("Error getting revenue stats", e);
            return DashboardStatsDTO.RevenueStatsDTO.builder()
                    .total(0.0)
                    .vip(0.0)
                    .premium(0.0)
                    .thisMonth(0.0)
                    .thisWeek(0.0)
                    .build();
        }
    }
    
    public DashboardStatsDTO.OrderStatsDTO getOrderStats() {
        logger.info("Getting order stats...");
        
        try {
            // Tổng số đơn hàng thành công trong tháng hiện tại
            Long successOrdersThisMonth = vipPaymentRepository.countSuccessfulOrdersThisMonth();
            logger.info("Successful orders this month: {}", successOrdersThisMonth);
            
            // Thống kê đơn hàng theo status
            List<VipPayment> pendingPayments = vipPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.PENDING);
            List<VipPayment> successPayments = vipPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.SUCCESS);
            List<VipPayment> failedPayments = vipPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.FAILED);
            
            Long totalOrders = vipPaymentRepository.count();
            Long pendingOrders = (long) pendingPayments.size();
            Long successOrders = (long) successPayments.size();
            Long failedOrders = (long) failedPayments.size();
            
            logger.info("Order breakdown - Total: {}, Pending: {}, Success: {}, Failed: {}", 
                    totalOrders, pendingOrders, successOrders, failedOrders);
            
            return DashboardStatsDTO.OrderStatsDTO.builder()
                    .total(totalOrders)
                    .pending(pendingOrders)
                    .success(successOrdersThisMonth != null ? successOrdersThisMonth : 0L) // Sử dụng success orders của tháng hiện tại
                    .failed(failedOrders)
                    .build();
        } catch (Exception e) {
            logger.error("Error getting order stats", e);
            return DashboardStatsDTO.OrderStatsDTO.builder()
                    .total(0L)
                    .pending(0L)
                    .success(0L)
                    .failed(0L)
                    .build();
        }
    }
    
    public DashboardStatsDTO.RegistrationStatsDTO getRegistrationStats() {
        logger.info("Getting registration stats...");
        
        try {
            // Đăng ký khách hàng mới
            Long todayRegistrations = customerRepository.countCustomersRegisteredToday();
            Long weekRegistrations = customerRepository.countCustomersRegisteredThisWeek();
            Long monthRegistrations = customerRepository.countCustomersRegisteredThisMonth();
            
            logger.info("Registration breakdown - Today: {}, This week: {}, This month: {}", 
                    todayRegistrations, weekRegistrations, monthRegistrations);
            
            return DashboardStatsDTO.RegistrationStatsDTO.builder()
                    .today(todayRegistrations != null ? todayRegistrations : 0L)
                    .thisWeek(weekRegistrations != null ? weekRegistrations : 0L)
                    .thisMonth(monthRegistrations != null ? monthRegistrations : 0L)
                    .build();
        } catch (Exception e) {
            logger.error("Error getting registration stats", e);
            return DashboardStatsDTO.RegistrationStatsDTO.builder()
                    .today(0L)
                    .thisWeek(0L)
                    .thisMonth(0L)
                    .build();
        }
    }
    
    public List<DashboardStatsDTO.RecentActivityDTO> getRecentActivities() {
        logger.info("Getting recent activities...");
        
        try {
            // Lấy 10 giao dịch VIP payment gần nhất làm hoạt động gần đây
            List<VipPayment> allPayments = vipPaymentRepository.findAll();
            logger.info("Total payments in database: {}", allPayments.size());
            
            // Sắp xếp theo ngày tạo và lấy top 10
            List<VipPayment> recentPayments = allPayments.stream()
                    .sorted((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()))
                    .limit(10)
                    .collect(Collectors.toList());
            
            logger.info("Recent payments to return: {}", recentPayments.size());
            
            List<DashboardStatsDTO.RecentActivityDTO> activities = new ArrayList<>();
            
            for (VipPayment payment : recentPayments) {
                String courseName = "Unknown Course";
                if (payment.getCourse() != null) {
                    courseName = payment.getCourse().getCourseName();
                }
                
                String customerName = "Unknown Customer";
                if (payment.getCustomer() != null) {
                    customerName = payment.getCustomer().getCustomerName();
                }
                
                DashboardStatsDTO.RecentActivityDTO activity = DashboardStatsDTO.RecentActivityDTO.builder()
                        .createdAt(payment.getCreatedDate())
                        .type("Course Purchase")
                        .userName(customerName)
                        .status(payment.getPaymentStatus().toString())
                        .amount(payment.getAmount().doubleValue())
                        .description("Purchased course: " + courseName)
                        .build();
                
                activities.add(activity);
                logger.debug("Added activity: {} - {} - {}", customerName, courseName, payment.getPaymentStatus());
            }
            
            logger.info("Returning {} activities", activities.size());
            return activities;
        } catch (Exception e) {
            logger.error("Error getting recent activities", e);
            return new ArrayList<>();
        }
    }
    
    // Helper method for default stats
    private DashboardStatsDTO createDefaultDashboardStats() {
        return DashboardStatsDTO.builder()
                .userStats(DashboardStatsDTO.UserStatsDTO.builder()
                        .total(0L).regular(0L).premium(0L).vip(0L).build())
                .courseStats(DashboardStatsDTO.CourseStatsDTO.builder()
                        .total(0L).active(0L).hidden(0L).newThisWeek(0L).newThisMonth(0L).build())
                .revenueStats(DashboardStatsDTO.RevenueStatsDTO.builder()
                        .total(0.0).vip(0.0).premium(0.0).thisMonth(0.0).thisWeek(0.0).build())
                .orderStats(DashboardStatsDTO.OrderStatsDTO.builder()
                        .total(0L).pending(0L).success(0L).failed(0L).build())
                .registrationStats(DashboardStatsDTO.RegistrationStatsDTO.builder()
                        .today(0L).thisWeek(0L).thisMonth(0L).build())
                .recentActivities(new ArrayList<>())
                .build();
    }
    
    // Debug methods for controller access
    public Long getDebugTotalCustomers() {
        try {
            return customerRepository.countAllActiveCustomers();
        } catch (Exception e) {
            logger.error("Error getting debug total customers", e);
            return 0L;
        }
    }
    
    public Long getDebugActiveCourses() {
        try {
            return courseRepository.countActiveCourses();
        } catch (Exception e) {
            logger.error("Error getting debug active courses", e);
            return 0L;
        }
    }
    
    public Double getDebugTotalRevenue() {
        try {
            return vipPaymentRepository.getTotalRevenue();
        } catch (Exception e) {
            logger.error("Error getting debug total revenue", e);
            return 0.0;
        }
    }
    
    public java.util.Map<String, Object> getRawCounts() {
        java.util.Map<String, Object> counts = new java.util.HashMap<>();
        
        try {
            // Raw counts without filters
            Long allCustomers = customerRepository.count();
            Long allCourses = courseRepository.count();
            Long allPayments = vipPaymentRepository.count();
            
            // Active customers
            Long activeCustomers = customerRepository.countAllActiveCustomers();
            
            // Customer types
            Long type1 = customerRepository.countCustomersByType(1);
            Long type2 = customerRepository.countCustomersByType(2);
            Long type3 = customerRepository.countCustomersByType(3);
            
            counts.put("allCustomers", allCustomers);
            counts.put("allCourses", allCourses);
            counts.put("allPayments", allPayments);
            counts.put("activeCustomers", activeCustomers);
            counts.put("customerType1", type1);
            counts.put("customerType2", type2);
            counts.put("customerType3", type3);
            
            logger.info("Raw counts - All customers: {}, Active: {}, Type1: {}, Type2: {}, Type3: {}", 
                    allCustomers, activeCustomers, type1, type2, type3);
            logger.info("Raw counts - All courses: {}, All payments: {}", allCourses, allPayments);
            
        } catch (Exception e) {
            logger.error("Error getting raw counts", e);
            counts.put("error", e.getMessage());
        }
        
        return counts;
    }
} 