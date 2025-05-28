package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerTypeRepository;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentStatusService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentStatusService.class);
    
    private final IVipPaymentRepository vipPaymentRepository;
    private final ICustomerRepository customerRepository;
    private final ICustomerTypeRepository customerTypeRepository;
    
    /**
     * Tự động cập nhật trạng thái PENDING thành FAILED sau 15 phút
     * Chạy mỗi 5 phút để kiểm tra
     */
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000ms
    @Transactional
    public void updateExpiredPendingPayments() {
        try {
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            
            // Tìm tất cả payment PENDING đã quá 15 phút - sử dụng query mới
            List<VipPayment> expiredPayments = vipPaymentRepository.findExpiredPendingPayments(fifteenMinutesAgo);
            
            int updatedCount = 0;
            for (VipPayment payment : expiredPayments) {
                payment.setPaymentStatus(VipPayment.PaymentStatus.FAILED);
                payment.setUpdatedDate(LocalDateTime.now());
                payment.setVnpResponseCode("99"); // 99 = timeout/failed code
                
                // Tạo response message cho timeout
                String timeoutResponse = String.format(
                    "{\"status\":\"FAILED\",\"reason\":\"TIMEOUT\",\"message\":\"Payment expired after 15 minutes\",\"created_date\":\"%s\",\"expired_at\":\"%s\"}",
                    payment.getCreatedDate().toString(),
                    LocalDateTime.now().toString()
                );
                payment.setPaymentGatewayResponse(timeoutResponse);
                
                vipPaymentRepository.save(payment);
                updatedCount++;
                
                logger.info("Updated payment {} to FAILED due to timeout. Created: {}, Current: {}", 
                        payment.getPaymentId(), payment.getCreatedDate(), LocalDateTime.now());
            }
            
            if (updatedCount > 0) {
                logger.info("Updated {} expired PENDING payments to FAILED status", updatedCount);
            }
            
        } catch (Exception e) {
            logger.error("Error updating expired pending payments", e);
        }
    }
    
    /**
     * Tính tổng số tiền customer đã thanh toán thành công
     */
    public BigDecimal getTotalPaidAmount(Integer customerId) {
        try {
            // Sử dụng query mới hiệu quả hơn
            BigDecimal totalPaid = vipPaymentRepository.getTotalPaidAmountByCustomer(customerId);
            return totalPaid != null ? totalPaid : BigDecimal.ZERO;
                    
        } catch (Exception e) {
            logger.error("Error calculating total paid amount for customer {}", customerId, e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Cập nhật customer type dựa trên tổng số tiền đã thanh toán
     * Logic: Sử dụng price threshold từ bảng customer_type
     * - Tìm customer type phù hợp dựa trên total paid amount so với price threshold
     */
    @Transactional
    public boolean updateCustomerTypeBasedOnPayments(Integer customerId) {
        try {
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                logger.warn("Customer not found with ID: {}", customerId);
                return false;
            }
            
            BigDecimal totalPaid = getTotalPaidAmount(customerId);
            logger.info("Customer {} total paid amount: {} VND", customerId, totalPaid);
            
            // Lấy danh sách customer types theo thứ tự price tăng dần
            List<CustomerType> customerTypes = customerTypeRepository.findAllOrderByPriceAsc();
            
            if (customerTypes.isEmpty()) {
                logger.error("No customer types found in database");
                return false;
            }
            
            // Tìm customer type phù hợp dựa trên total paid amount
            CustomerType appropriateType = null;
            
            for (CustomerType type : customerTypes) {
                if (type.getPrice() != null) {
                    BigDecimal threshold = BigDecimal.valueOf(type.getPrice());
                    if (totalPaid.compareTo(threshold) >= 0) {
                        appropriateType = type;
                    } else {
                        break; // Đã tìm được type phù hợp
                    }
                }
            }
            
            // Nếu không tìm được type nào phù hợp (totalPaid < threshold thấp nhất)
            // thì chọn type có price thấp nhất (Regular)
            if (appropriateType == null && !customerTypes.isEmpty()) {
                appropriateType = customerTypes.get(0);
            }
            
            if (appropriateType == null) {
                logger.error("Could not determine appropriate customer type for customer {}", customerId);
                return false;
            }
            
            logger.info("Determined appropriate customer type for customer {}: {} (threshold: {} VND)", 
                    customerId, appropriateType.getCustomerTypeName(), appropriateType.getPrice());
            
            // Kiểm tra nếu customer type đã đúng thì không cần update
            if (customer.getCustomerType() != null && 
                customer.getCustomerType().getCustomerTypeId().equals(appropriateType.getCustomerTypeId())) {
                logger.info("Customer {} already has correct type {}", customerId, appropriateType.getCustomerTypeName());
                return true;
            }
            
            // Cập nhật customer type
            CustomerType oldType = customer.getCustomerType();
            customer.setCustomerType(appropriateType);
            customerRepository.save(customer);
            
            logger.info("Updated customer {} type from {} to {} based on total payments of {} VND (threshold: {} VND)", 
                    customerId, 
                    oldType != null ? oldType.getCustomerTypeName() : "null",
                    appropriateType.getCustomerTypeName(), 
                    totalPaid,
                    appropriateType.getPrice());
            return true;
            
        } catch (Exception e) {
            logger.error("Error updating customer type for customer {}", customerId, e);
            return false;
        }
    }
    
    /**
     * Cập nhật customer type cho tất cả customers
     * Chạy hàng ngày lúc 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void updateAllCustomerTypes() {
        try {
            logger.info("Starting daily customer type update process");
            
            List<Customer> allCustomers = customerRepository.findAll();
            int updatedCount = 0;
            
            for (Customer customer : allCustomers) {
                if (customer.getIsEnable() && customer.getAccount() != null) {
                    boolean updated = updateCustomerTypeBasedOnPayments(customer.getCustomerId());
                    if (updated) {
                        updatedCount++;
                    }
                }
            }
            
            logger.info("Daily customer type update completed. Updated {} customers", updatedCount);
            
        } catch (Exception e) {
            logger.error("Error in daily customer type update", e);
        }
    }
    
    /**
     * Lấy thống kê thanh toán của customer
     */
    public CustomerPaymentStats getCustomerPaymentStats(Integer customerId) {
        try {
            List<VipPayment> allPayments = vipPaymentRepository.findByCustomer_CustomerIdOrderByCreatedDateDesc(customerId);
            
            long totalOrders = allPayments.size();
            long successfulOrders = allPayments.stream()
                    .mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS ? 1 : 0)
                    .sum();
            long failedOrders = allPayments.stream()
                    .mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.FAILED ? 1 : 0)
                    .sum();
            long pendingOrders = allPayments.stream()
                    .mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.PENDING ? 1 : 0)
                    .sum();
                    
            BigDecimal totalPaid = getTotalPaidAmount(customerId);
            
            return CustomerPaymentStats.builder()
                    .customerId(customerId)
                    .totalOrders(totalOrders)
                    .successfulOrders(successfulOrders)
                    .failedOrders(failedOrders)
                    .pendingOrders(pendingOrders)
                    .totalPaidAmount(totalPaid)
                    .build();
                    
        } catch (Exception e) {
            logger.error("Error getting payment stats for customer {}", customerId, e);
            return CustomerPaymentStats.builder()
                    .customerId(customerId)
                    .totalOrders(0L)
                    .successfulOrders(0L)
                    .failedOrders(0L)
                    .pendingOrders(0L)
                    .totalPaidAmount(BigDecimal.ZERO)
                    .build();
        }
    }
    
    /**
     * Lấy thông tin về customer type thresholds từ database
     */
    public List<CustomerTypeThreshold> getCustomerTypeThresholds() {
        try {
            List<CustomerType> customerTypes = customerTypeRepository.findAllOrderByPriceAsc();
            
            return customerTypes.stream()
                .map(type -> CustomerTypeThreshold.builder()
                    .customerTypeId(type.getCustomerTypeId())
                    .customerTypeName(type.getCustomerTypeName())
                    .threshold(type.getPrice() != null ? BigDecimal.valueOf(type.getPrice()) : BigDecimal.ZERO)
                    .build())
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            logger.error("Error getting customer type thresholds", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Xác định customer type phù hợp dựa trên total paid amount
     */
    public CustomerType determineCustomerType(BigDecimal totalPaid) {
        try {
            List<CustomerType> customerTypes = customerTypeRepository.findAllOrderByPriceAsc();
            
            if (customerTypes.isEmpty()) {
                return null;
            }
            
            CustomerType appropriateType = null;
            
            for (CustomerType type : customerTypes) {
                if (type.getPrice() != null) {
                    BigDecimal threshold = BigDecimal.valueOf(type.getPrice());
                    if (totalPaid.compareTo(threshold) >= 0) {
                        appropriateType = type;
                    } else {
                        break;
                    }
                }
            }
            
            // Nếu không tìm được type nào phù hợp, chọn type có price thấp nhất
            if (appropriateType == null && !customerTypes.isEmpty()) {
                appropriateType = customerTypes.get(0);
            }
            
            return appropriateType;
            
        } catch (Exception e) {
            logger.error("Error determining customer type for amount {}", totalPaid, e);
            return null;
        }
    }
    
    /**
     * DTO cho thống kê thanh toán của customer
     */
    @lombok.Builder
    @lombok.Data
    public static class CustomerPaymentStats {
        private Integer customerId;
        private Long totalOrders;
        private Long successfulOrders;
        private Long failedOrders;
        private Long pendingOrders;
        private BigDecimal totalPaidAmount;
    }
    
    /**
     * DTO cho thông tin customer type threshold
     */
    @lombok.Builder
    @lombok.Data
    public static class CustomerTypeThreshold {
        private Integer customerTypeId;
        private String customerTypeName;
        private BigDecimal threshold;
    }
} 