package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.config.VnPayConfig;
import com.capstone.wellnessnavigatorgym.dto.cart.CartWithDetail;
import com.capstone.wellnessnavigatorgym.dto.payment.PaymentResponseDto;
import com.capstone.wellnessnavigatorgym.dto.payment.TransactionStatusDTO;
import com.capstone.wellnessnavigatorgym.entity.*;
import com.capstone.wellnessnavigatorgym.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import com.capstone.wellnessnavigatorgym.dto.request.PaymentRequest;
import com.capstone.wellnessnavigatorgym.dto.response.MessageResponse;
import com.capstone.wellnessnavigatorgym.service.VNPayService;
import com.capstone.wellnessnavigatorgym.service.ICustomerCourseService;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.security.jwt.JwtUtility;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/payment")
@CrossOrigin(origins = "http://localhost:3000")
public class PaymentController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private IPaymentService paymentService;

    @Autowired
    private ICartDetailService cartDetailService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private ICustomerTypeService customerTypeService;

    @Autowired
    private ICustomerService customerService;

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private IVipPaymentRepository vipPaymentRepository;

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private JwtUtility jwtUtility;

    @Autowired
    private ICustomerCourseService customerCourseService;

    @Autowired
    private PaymentStatusService paymentStatusService;

    @PutMapping("/create-payment")
    public ResponseEntity<PaymentResponseDto> createPayment(@RequestBody CartWithDetail cartWithDetail) throws UnsupportedEncodingException {
        Cart cart = cartWithDetail.getCart();
        List<CartDetail> cartDetailList = cartWithDetail.getCartDetailList();
        int totalAmount = 0;
        Set<CartDetail> cartDetails = new HashSet<>();
        cartService.update(cart);
        for (CartDetail cartDetail: cartDetailList) {
            if (!cartDetail.isStatus()) {
                totalAmount += cartDetail.getQuantity() * cartDetail.getCustomerType().getPrice();
                cartDetails.add(cartDetail);
            }
        }
        if (totalAmount != 0) {
            String vnp_TxnRef = VnPayConfig.getRandomNumber(8);
            String vnp_IpAddr = "127.0.0.1";
            String vnp_TmnCode = VnPayConfig.vnp_TmnCode;

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Amount", totalAmount + "00");
            vnp_Params.put("vnp_Command", VnPayConfig.vnp_Command);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_CurrCode", "VND");
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_OrderInfo", "Thanh toan hoa don:" + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", "topup");
            vnp_Params.put("vnp_ReturnUrl", VnPayConfig.vnp_ReturnUrl);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_Version", VnPayConfig.vnp_Version);

            List fieldNames = new ArrayList(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = (String) vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }

            String queryUrl = query.toString();
            String vnp_SecureHash = VnPayConfig.hmacSHA512(VnPayConfig.vnp_HashSecret, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VnPayConfig.vnp_PayUrl + "?" + queryUrl;

            Payment payment = new Payment();
            payment.setCartDetails(cartDetails);
            payment.setTotalAmount(totalAmount);
            payment.setPaid(false);
            payment.setTnxRef(vnp_TxnRef);
            payment.setCartId(cart.getCartId());
            paymentService.update(payment);

            PaymentResponseDto paymentResponseDto = new PaymentResponseDto();
            paymentResponseDto.setStatus("OK");
            paymentResponseDto.setMessage("Successfully");
            paymentResponseDto.setUrl(paymentUrl);
            return new ResponseEntity<>(paymentResponseDto, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }

    @GetMapping("/transaction/{tnxRef}")
    public ResponseEntity<?> transactionChecking(@PathVariable("tnxRef") String tnxRef) {
        Payment payment = paymentService.findPaymentByTnxRef(tnxRef);
        if (!payment.isPaid()) {
            payment.setPaid(true);
            paymentService.update(payment);
            int totalAmount = payment.getTotalAmount();
            Cart cart = cartService.findById(payment.getCartId());
            List<CartDetail> cartDetails = new ArrayList<>(payment.getCartDetails());
            for (CartDetail cartDetail : cartDetails) {
                cartDetail.setStatus(true);
                cartDetailService.update(cartDetail);
            }

            Customer customer = customerService.findByCart(cart);
            if (customer != null) {
                customerTypeService.upgradeCustomerType(customer);
            }
            emailService.emailProcess(cart, totalAmount, cartDetails);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/fail/{tnxRef}")
    public ResponseEntity<?> transactionFail(@PathVariable("tnxRef") String tnxRef) {
        paymentService.deleteByTnxRef(tnxRef);
        return new ResponseEntity<>(HttpStatus.GONE);
    }

    /**
     * Tạo URL thanh toán VNPay
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequest paymentRequest, 
                                         HttpServletRequest request, 
                                         Principal principal) {
        try {
            System.out.println("=== Payment Controller /create ===");
            System.out.println("Principal name: " + principal.getName());
            System.out.println("PaymentRequest: " + paymentRequest);
            System.out.println("Course ID: " + paymentRequest.getCourseId());
            System.out.println("Amount: " + paymentRequest.getAmount());
            System.out.println("Currency: " + paymentRequest.getCurrency());
            System.out.println("Order Info: " + paymentRequest.getOrderInfo());
            System.out.println("Return URL: " + paymentRequest.getReturnUrl());
            
            // Set IP address từ request
            String ipAddress = getClientIpAddress(request);
            paymentRequest.setIpAddress(ipAddress);
            System.out.println("IP Address: " + ipAddress);
            
            // Set return URL mặc định nếu không có
            if (paymentRequest.getReturnUrl() == null) {
                paymentRequest.setReturnUrl("http://localhost:3000/payment/result");
                System.out.println("Set default return URL: " + paymentRequest.getReturnUrl());
            }

            // Tạo URL thanh toán
            String paymentUrl = vnPayService.createPaymentUrl(paymentRequest, principal.getName());
            System.out.println("Payment URL created successfully");
            
            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("message", "Payment URL created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in PaymentController.createPayment: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error creating payment: " + e.getMessage()));
        }
    }

    /**
     * Xử lý callback từ VNPay
     */
    @GetMapping("/vnpay-return")
    public ResponseEntity<?> vnpayReturn(@RequestParam Map<String, String> vnpParams) {
        try {
            System.out.println("=== VNPay Return Callback ===");
            System.out.println("Received VNPay params: " + vnpParams);
            
            // Kiểm tra các parameter bắt buộc
            String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
            String vnpTxnRef = vnpParams.get("vnp_TxnRef");
            String vnpSecureHash = vnpParams.get("vnp_SecureHash");
            
            if (vnpResponseCode == null || vnpTxnRef == null || vnpSecureHash == null) {
                System.err.println("Missing required VNPay parameters");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "Missing required payment parameters");
                errorResponse.put("transactionRef", vnpTxnRef);
                errorResponse.put("responseCode", vnpResponseCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            System.out.println("VNP Response Code: " + vnpResponseCode);
            System.out.println("VNP TxnRef: " + vnpTxnRef);
            System.out.println("VNP SecureHash: " + vnpSecureHash);
            
            // Verify signature từ VNPay
            boolean isValidSignature;
            try {
                isValidSignature = vnPayService.verifyPayment(vnpParams);
                System.out.println("Signature valid: " + isValidSignature);
            } catch (Exception e) {
                System.err.println("Error verifying signature: " + e.getMessage());
                e.printStackTrace();
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "Signature verification failed: " + e.getMessage());
                errorResponse.put("transactionRef", vnpTxnRef);
                errorResponse.put("responseCode", vnpResponseCode);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            if (!isValidSignature) {
                System.err.println("Invalid signature from VNPay");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "Invalid signature from VNPay");
                errorResponse.put("transactionRef", vnpTxnRef);
                errorResponse.put("responseCode", vnpResponseCode);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Cập nhật trạng thái thanh toán
            try {
            vnPayService.updatePaymentStatus(vnpParams);
                System.out.println("Payment status updated successfully");
            } catch (Exception e) {
                System.err.println("Error updating payment status: " + e.getMessage());
                e.printStackTrace();
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "FAILED");
                errorResponse.put("message", "Failed to update payment status: " + e.getMessage());
                errorResponse.put("transactionRef", vnpTxnRef);
                errorResponse.put("responseCode", vnpResponseCode);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionRef", vnpTxnRef);
            response.put("responseCode", vnpResponseCode);
            
            if ("00".equals(vnpResponseCode)) {
                response.put("status", "SUCCESS");
                response.put("message", "Payment successful");
                System.out.println("Payment successful for transaction: " + vnpTxnRef);
                
                // Tự động thêm khóa học VIP vào customer_course khi thanh toán thành công
                try {
                    // Tìm payment record từ vnpTxnRef
                    Optional<VipPayment> paymentOpt = vipPaymentRepository.findByVnpTxnRef(vnpTxnRef);
                    if (paymentOpt.isPresent()) {
                        VipPayment payment = paymentOpt.get();
                        Integer customerId = payment.getCustomer().getCustomerId();
                        Integer courseId = payment.getCourse().getCourseId();
                        
                        System.out.println("Adding VIP course to customer - Customer ID: " + customerId + ", Course ID: " + courseId);
                        
                        // Kiểm tra xem customer đã có khóa học này chưa
                        if (!customerCourseService.isCustomerEnrolledInCourse(customerId, courseId)) {
                            // Thêm khóa học vào customer_course
                            customerCourseService.addCourseForCustomer(courseId, customerId);
                            System.out.println("Successfully added VIP course " + courseId + " to customer " + customerId);
                            response.put("courseEnrollment", "SUCCESS");
                        } else {
                            System.out.println("Customer " + customerId + " already enrolled in course " + courseId);
                            response.put("courseEnrollment", "ALREADY_ENROLLED");
                        }
                        
                        // Tự động cập nhật customer type dựa trên tổng số tiền đã thanh toán
                        try {
                            boolean customerTypeUpdated = paymentStatusService.updateCustomerTypeBasedOnPayments(customerId);
                            if (customerTypeUpdated) {
                                System.out.println("Successfully updated customer type for customer " + customerId);
                                response.put("customerTypeUpdate", "SUCCESS");
                                
                                // Lấy thông tin customer type mới
                                Customer updatedCustomer = customerRepository.findById(customerId).orElse(null);
                                if (updatedCustomer != null && updatedCustomer.getCustomerType() != null) {
                                    response.put("newCustomerType", updatedCustomer.getCustomerType().getCustomerTypeId());
                                    response.put("newCustomerTypeName", updatedCustomer.getCustomerType().getCustomerTypeName());
                                }
                                
                                // Lấy tổng số tiền đã thanh toán
                                BigDecimal totalPaid = paymentStatusService.getTotalPaidAmount(customerId);
                                response.put("totalPaidAmount", totalPaid);
                            } else {
                                System.out.println("Failed to update customer type for customer " + customerId);
                                response.put("customerTypeUpdate", "FAILED");
                            }
                        } catch (Exception e) {
                            System.err.println("Error updating customer type: " + e.getMessage());
                            e.printStackTrace();
                            response.put("customerTypeUpdate", "ERROR");
                            response.put("customerTypeError", e.getMessage());
                        }
                        
                    } else {
                        System.err.println("VipPayment not found for transaction: " + vnpTxnRef);
                        response.put("courseEnrollment", "PAYMENT_NOT_FOUND");
                    }
                } catch (Exception e) {
                    System.err.println("Error adding course to customer after payment: " + e.getMessage());
                    e.printStackTrace();
                    response.put("courseEnrollment", "FAILED");
                    response.put("courseEnrollmentError", e.getMessage());
                }
                
            } else {
                response.put("status", "FAILED");
                response.put("message", "Payment failed");
                System.out.println("Payment failed for transaction: " + vnpTxnRef + " with code: " + vnpResponseCode);
            }
            
            System.out.println("Returning response: " + response);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error processing VNPay return: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", "Error processing payment return: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy lịch sử thanh toán của user
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPaymentHistory(Principal principal) {
        try {
            // TODO: Implement proper customer lookup
            List<VipPayment> payments = vipPaymentRepository.findAll();
            
            return ResponseEntity.ok(payments);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error retrieving payment history: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/status/{transactionRef}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String transactionRef) {
        try {
            VipPayment payment = vipPaymentRepository.findByVnpTxnRef(transactionRef)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionRef", payment.getVnpTxnRef());
            response.put("status", payment.getPaymentStatus());
            response.put("amount", payment.getAmount());
            response.put("courseName", payment.getCourse().getCourseName());
            response.put("createdDate", payment.getCreatedDate());
            response.put("completedDate", payment.getCompletedDate());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Payment not found: " + e.getMessage()));
        }
    }

    /**
     * Test endpoint để verify VNPay configuration
     */
    @GetMapping("/test-config")
    public ResponseEntity<?> testVNPayConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            config.put("vnpayUrl", vnPayService.getVnpPayUrl());
            config.put("message", "VNPay configuration loaded successfully");
            config.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(config);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("VNPay config error: " + e.getMessage()));
        }
    }

    /**
     * Utility method để lấy IP address của client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Debug endpoint để kiểm tra course data
     */
    @GetMapping("/debug-course/{courseId}")
    public ResponseEntity<?> debugCourse(@PathVariable Integer courseId) {
        try {
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + courseId));
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("courseId", course.getCourseId());
            debug.put("courseName", course.getCourseName());
            debug.put("price", course.getPrice());
            debug.put("isVip", course.getIsVip());
            debug.put("status", course.getStatus());
            debug.put("description", course.getDescription());
            debug.put("courseType", course.getCourseType() != null ? course.getCourseType().getCourseTypeName() : null);
            debug.put("message", "Course data retrieved successfully");
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Course debug failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Lấy lịch sử thanh toán VIP của customer hiện tại
     */
    @GetMapping("/my-vip-payments")
    public ResponseEntity<?> getMyVipPayments(Authentication authentication) {
        try {
            String username = authentication.getName();
            
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            List<VipPayment> payments = vipPaymentRepository.findByCustomer_CustomerIdOrderByCreatedDateDesc(customer.getCustomerId());
            
            List<Map<String, Object>> result = payments.stream().map(payment -> {
                Map<String, Object> paymentInfo = new HashMap<>();
                paymentInfo.put("paymentId", payment.getPaymentId());
                paymentInfo.put("courseId", payment.getCourse().getCourseId());
                paymentInfo.put("courseName", payment.getCourse().getCourseName());
                paymentInfo.put("amount", payment.getAmount());
                paymentInfo.put("createdDate", payment.getCreatedDate());
                paymentInfo.put("paymentStatus", payment.getPaymentStatus());
                paymentInfo.put("vnpTransactionNo", payment.getVnpTransactionNo());
                paymentInfo.put("vnpOrderInfo", payment.getVnpOrderInfo());
                return paymentInfo;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting payment history: " + e.getMessage());
        }
    }
    
    /**
     * Lấy danh sách khóa học VIP mà customer đã mua thành công
     */
    @GetMapping("/my-vip-courses") 
    public ResponseEntity<?> getMyVipCourses(Authentication authentication) {
        try {
            String username = authentication.getName();
            
            Customer customer = customerRepository.findByAccountUserName(username);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // Lấy các khóa học VIP đã thanh toán thành công
            List<VipPayment> successfulPayments = vipPaymentRepository.findByCustomer_CustomerIdAndPaymentStatusOrderByCreatedDateDesc(
                    customer.getCustomerId(), VipPayment.PaymentStatus.SUCCESS);
            
            List<Map<String, Object>> vipCourses = successfulPayments.stream().map(payment -> {
                Course course = payment.getCourse();
                Map<String, Object> courseInfo = new HashMap<>();
                courseInfo.put("courseId", course.getCourseId());
                courseInfo.put("courseName", course.getCourseName());
                courseInfo.put("description", course.getDescription());
                courseInfo.put("price", payment.getAmount());
                courseInfo.put("purchaseDate", payment.getCreatedDate());
                courseInfo.put("paymentId", payment.getPaymentId());
                
                // Kiểm tra xem đã được thêm vào customer_course chưa
                boolean isEnrolled = customerCourseService.isCustomerEnrolledInCourse(customer.getCustomerId(), course.getCourseId());
                courseInfo.put("isEnrolled", isEnrolled);
                
                return courseInfo;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerName", customer.getCustomerName());
            response.put("totalVipCourses", vipCourses.size());
            response.put("vipCourses", vipCourses);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting VIP courses: " + e.getMessage());
        }
    }

    /**
     * Admin xem báo cáo doanh thu từ VIP courses  
     */
    @GetMapping("/admin/revenue-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getRevenueReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<VipPayment> successfulPayments;
            
            if (startDate != null && endDate != null) {
                // Có thể thêm filter theo ngày sau
                successfulPayments = vipPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.SUCCESS);
            } else {
                successfulPayments = vipPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.SUCCESS);
            }
            
            // Tổng doanh thu
            BigDecimal totalRevenue = successfulPayments.stream()
                    .map(VipPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Doanh thu theo từng khóa học
            Map<String, BigDecimal> revenueByCourse = successfulPayments.stream()
                    .collect(Collectors.groupingBy(
                            payment -> payment.getCourse().getCourseName(),
                            Collectors.reducing(BigDecimal.ZERO, VipPayment::getAmount, BigDecimal::add)
                    ));
            
            // Thống kê theo ngày
            Map<String, BigDecimal> revenueByDate = successfulPayments.stream()
                    .collect(Collectors.groupingBy(
                            payment -> payment.getCreatedDate().toLocalDate().toString(),
                            Collectors.reducing(BigDecimal.ZERO, VipPayment::getAmount, BigDecimal::add)
                    ));
            
            Map<String, Object> report = new HashMap<>();
            report.put("totalRevenue", totalRevenue);
            report.put("totalTransactions", successfulPayments.size());
            report.put("revenueByCourse", revenueByCourse);
            report.put("revenueByDate", revenueByDate);
            
            // Danh sách giao dịch chi tiết
            List<Map<String, Object>> transactions = successfulPayments.stream().map(payment -> {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("paymentId", payment.getPaymentId());
                transaction.put("customerName", payment.getCustomer().getCustomerName());
                transaction.put("courseName", payment.getCourse().getCourseName());
                transaction.put("amount", payment.getAmount());
                transaction.put("paymentDate", payment.getCreatedDate());
                transaction.put("vnpTransactionNo", payment.getVnpTransactionNo());
                return transaction;
            }).collect(Collectors.toList());
            
            report.put("transactions", transactions);
            
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting revenue report: " + e.getMessage());
        }
    }
    
    /**
     * Admin xem thống kê tổng quan  
     */
    @GetMapping("/admin/payment-stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentStats() {
        try {
            List<VipPayment> allPayments = vipPaymentRepository.findAll();
            
            long totalPayments = allPayments.size();
            long successfulPayments = allPayments.stream()
                    .mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS ? 1 : 0)
                    .sum();
            long failedPayments = allPayments.stream()
                    .mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.FAILED ? 1 : 0)
                    .sum();
            long pendingPayments = allPayments.stream()
                    .mapToLong(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.PENDING ? 1 : 0)
                    .sum();
            
            BigDecimal totalRevenue = allPayments.stream()
                    .filter(p -> p.getPaymentStatus() == VipPayment.PaymentStatus.SUCCESS)
                    .map(VipPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPayments", totalPayments);
            stats.put("successfulPayments", successfulPayments);
            stats.put("failedPayments", failedPayments);
            stats.put("pendingPayments", pendingPayments);
            stats.put("successRate", totalPayments > 0 ? (double) successfulPayments / totalPayments * 100 : 0);
            stats.put("totalRevenue", totalRevenue);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting payment stats: " + e.getMessage());
        }
    }
    
    /**
     * Debug endpoint để test thêm khóa học vào customer_course
     */
    @PostMapping("/debug/enroll-course")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> debugEnrollCourse(@RequestParam Integer customerId, @RequestParam Integer courseId) {
        try {
            // Kiểm tra customer tồn tại
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            // Kiểm tra course tồn tại  
            Course course = courseRepository.findById(courseId).orElse(null);
            if (course == null) {
                return ResponseEntity.badRequest().body("Course not found");
            }
            
            // Kiểm tra đã đăng ký chưa
            boolean isEnrolled = customerCourseService.isCustomerEnrolledInCourse(customerId, courseId);
            if (isEnrolled) {
                return ResponseEntity.badRequest().body("Customer already enrolled in this course");
            }
            
            // Thêm khóa học cho customer
            customerCourseService.addCourseForCustomer(courseId, customerId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Course successfully added to customer");
            result.put("customerId", customerId);
            result.put("customerName", customer.getCustomerName());
            result.put("courseId", courseId);
            result.put("courseName", course.getCourseName());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error enrolling course: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để kiểm tra VIP course đã được thêm vào customer_course chưa
     */
    @GetMapping("/test-vip-enrollment/{courseId}")
    public ResponseEntity<?> testVipEnrollment(@PathVariable Integer courseId, Authentication authentication) {
        try {
            String username = authentication.getName();
            Customer customer = customerRepository.findByAccountUserName(username);
            
            if (customer == null) {
                return ResponseEntity.badRequest().body("Customer not found");
            }
            
            Map<String, Object> testResult = new HashMap<>();
            testResult.put("customerId", customer.getCustomerId());
            testResult.put("customerName", customer.getCustomerName());
            testResult.put("courseId", courseId);
            
            // Kiểm tra VIP payment
            List<VipPayment> vipPayments = vipPaymentRepository.findByCustomer_CustomerIdAndPaymentStatusOrderByCreatedDateDesc(
                customer.getCustomerId(), VipPayment.PaymentStatus.SUCCESS);
            
            boolean hasVipPayment = vipPayments.stream()
                .anyMatch(vip -> vip.getCourse().getCourseId().equals(courseId));
            testResult.put("hasSuccessfulVipPayment", hasVipPayment);
            
            // Kiểm tra enrollment trong customer_course
            boolean isEnrolled = customerCourseService.isCustomerEnrolledInCourse(customer.getCustomerId(), courseId);
            testResult.put("isEnrolledInCustomerCourse", isEnrolled);
            
            // Kiểm tra course thông tin
            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isPresent()) {
                Course course = courseOpt.get();
                testResult.put("courseName", course.getCourseName());
                testResult.put("isVipCourse", course.getIsVip());
                testResult.put("coursePrice", course.getPrice());
            }
            
            testResult.put("recommendation", hasVipPayment && !isEnrolled ? 
                "VIP course đã thanh toán nhưng chưa được thêm vào customer_course" : "OK");
            
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error testing VIP enrollment: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để verify course 65 data
     */
    @GetMapping("/test-course-65")
    public ResponseEntity<?> testCourse65() {
        try {
            Course course = courseRepository.findById(65)
                    .orElseThrow(() -> new RuntimeException("Course 65 not found"));
            
            Map<String, Object> debug = new HashMap<>();
            debug.put("courseId", course.getCourseId());
            debug.put("courseName", course.getCourseName());
            debug.put("description", course.getDescription());
            debug.put("price", course.getPrice());
            debug.put("isVip", course.getIsVip());
            debug.put("status", course.getStatus());
            debug.put("duration", course.getDuration());
            debug.put("image", course.getImage());
            
            if (course.getCourseType() != null) {
                debug.put("courseTypeName", course.getCourseType().getCourseTypeName());
            }
            
            debug.put("message", "Course 65 data retrieved successfully");
            
            return ResponseEntity.ok(debug);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get course 65 data");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Test endpoint để simulate VNPay callback
     */
    @GetMapping("/test-vnpay-callback")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testVnpayCallback(@RequestParam String txnRef) {
        try {
            // Simulate VNPay callback parameters
            Map<String, String> testParams = new HashMap<>();
            testParams.put("vnp_Amount", "199000000");
            testParams.put("vnp_BankCode", "NCB");
            testParams.put("vnp_BankTranNo", "VNP14984031");
            testParams.put("vnp_CardType", "ATM");
            testParams.put("vnp_OrderInfo", "VIP Course Payment: Advanced Total Body Transformation");
            testParams.put("vnp_PayDate", "20250528040728");
            testParams.put("vnp_ResponseCode", "00");
            testParams.put("vnp_TmnCode", "79ERASY7");
            testParams.put("vnp_TransactionNo", "14984031");
            testParams.put("vnp_TransactionStatus", "00");
            testParams.put("vnp_TxnRef", txnRef);
            
            // Generate signature for test
            List<String> fieldNames = new ArrayList<>(testParams.keySet());
            Collections.sort(fieldNames);
            
            StringBuilder hashData = new StringBuilder();
            Iterator<String> itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = testParams.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(fieldValue);
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
            
            // This won't match real VNPay signature but we can test our logic
            testParams.put("vnp_SecureHash", "test_hash_" + System.currentTimeMillis());
            
            System.out.println("=== Test VNPay Callback ===");
            System.out.println("Test params: " + testParams);
            System.out.println("Hash data: " + hashData.toString());
            
            // Test individual components
            Map<String, Object> testResult = new HashMap<>();
            
            // Test 1: Check if payment exists
            Optional<VipPayment> paymentOpt = vipPaymentRepository.findByVnpTxnRef(txnRef);
            testResult.put("paymentExists", paymentOpt.isPresent());
            
            if (paymentOpt.isPresent()) {
                VipPayment payment = paymentOpt.get();
                testResult.put("paymentId", payment.getPaymentId());
                testResult.put("customerId", payment.getCustomer().getCustomerId());
                testResult.put("courseId", payment.getCourse().getCourseId());
                testResult.put("currentStatus", payment.getPaymentStatus());
                
                // Test 2: Update payment status manually
                try {
                    payment.setVnpResponseCode("00");
                    payment.setVnpTransactionNo("TEST_14984031");
                    payment.setVnpPayDate("20250528040728");
                    payment.setUpdatedDate(LocalDateTime.now());
                    payment.setPaymentStatus(VipPayment.PaymentStatus.SUCCESS);
                    payment.setCompletedDate(LocalDateTime.now());
                    payment.setPaymentGatewayResponse(testParams.toString());
                    
                    vipPaymentRepository.save(payment);
                    testResult.put("statusUpdateSuccess", true);
                    
                    // Test 3: Try course enrollment
                    Integer customerId = payment.getCustomer().getCustomerId();
                    Integer courseId = payment.getCourse().getCourseId();
                    
                    boolean isAlreadyEnrolled = customerCourseService.isCustomerEnrolledInCourse(customerId, courseId);
                    testResult.put("alreadyEnrolled", isAlreadyEnrolled);
                    
                    if (!isAlreadyEnrolled) {
                        customerCourseService.addCourseForCustomer(courseId, customerId);
                        testResult.put("enrollmentSuccess", true);
                    } else {
                        testResult.put("enrollmentSuccess", "already_enrolled");
                    }
                    
                } catch (Exception e) {
                    testResult.put("statusUpdateError", e.getMessage());
                }
            }
            
            testResult.put("message", "Test callback completed");
            return ResponseEntity.ok(testResult);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Test callback failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Test API endpoint
     */
    @GetMapping("/test-api")
    public ResponseEntity<?> testApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "VIP Payment API is working");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Admin - Lấy thống kê thanh toán của một customer
     */
    @GetMapping("/admin/customer-payment-stats/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCustomerPaymentStats(@PathVariable Integer customerId) {
        try {
            PaymentStatusService.CustomerPaymentStats stats = paymentStatusService.getCustomerPaymentStats(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("stats", stats);
            
            // Thêm thông tin customer
            Customer customer = customerRepository.findById(customerId).orElse(null);
            if (customer != null) {
                response.put("customerName", customer.getCustomerName());
                response.put("customerType", customer.getCustomerType() != null ? 
                    customer.getCustomerType().getCustomerTypeName() : "No Type");
                response.put("customerTypeId", customer.getCustomerType() != null ? 
                    customer.getCustomerType().getCustomerTypeId() : null);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting customer payment stats: " + e.getMessage());
        }
    }
    
    /**
     * Admin - Cập nhật customer type dựa trên tổng thanh toán
     */
    @PostMapping("/admin/update-customer-type/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCustomerType(@PathVariable Integer customerId) {
        try {
            boolean updated = paymentStatusService.updateCustomerTypeBasedOnPayments(customerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("customerId", customerId);
            response.put("updated", updated);
            
            if (updated) {
                Customer customer = customerRepository.findById(customerId).orElse(null);
                if (customer != null) {
                    response.put("newCustomerType", customer.getCustomerType().getCustomerTypeId());
                    response.put("newCustomerTypeName", customer.getCustomerType().getCustomerTypeName());
                    
                    BigDecimal totalPaid = paymentStatusService.getTotalPaidAmount(customerId);
                    response.put("totalPaidAmount", totalPaid);
                }
                response.put("message", "Customer type updated successfully");
            } else {
                response.put("message", "Failed to update customer type");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating customer type: " + e.getMessage());
        }
    }
    
    /**
     * Admin - Cập nhật customer type cho tất cả customers
     */
    @PostMapping("/admin/update-all-customer-types")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAllCustomerTypes() {
        try {
            // Thực hiện cập nhật thủ công (không cần đợi scheduled task)
            paymentStatusService.updateAllCustomerTypes();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "All customer types update process initiated");
            response.put("timestamp", LocalDateTime.now());
            response.put("note", "Check logs for detailed results");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating all customer types: " + e.getMessage());
        }
    }
    
    /**
     * Admin - Cập nhật thủ công các payment PENDING quá hạn thành FAILED
     */
    @PostMapping("/admin/update-expired-payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateExpiredPayments() {
        try {
            // Thực hiện cập nhật thủ công (không cần đợi scheduled task)
            paymentStatusService.updateExpiredPendingPayments();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Expired payments update process completed");
            response.put("timestamp", LocalDateTime.now());
            response.put("note", "Check logs for number of updated payments");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating expired payments: " + e.getMessage());
        }
    }
    
    /**
     * Admin - Lấy danh sách payments PENDING đã quá hạn
     */
    @GetMapping("/admin/expired-pending-payments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getExpiredPendingPayments() {
        try {
            LocalDateTime fifteenMinutesAgo = LocalDateTime.now().minusMinutes(15);
            
            List<VipPayment> pendingPayments = vipPaymentRepository.findByPaymentStatusOrderByCreatedDateDesc(VipPayment.PaymentStatus.PENDING);
            
            List<Map<String, Object>> expiredPayments = new ArrayList<>();
            
            for (VipPayment payment : pendingPayments) {
                if (payment.getCreatedDate().isBefore(fifteenMinutesAgo)) {
                    Map<String, Object> paymentInfo = new HashMap<>();
                    paymentInfo.put("paymentId", payment.getPaymentId());
                    paymentInfo.put("vnpTxnRef", payment.getVnpTxnRef());
                    paymentInfo.put("customerId", payment.getCustomer().getCustomerId());
                    paymentInfo.put("customerName", payment.getCustomer().getCustomerName());
                    paymentInfo.put("courseId", payment.getCourse().getCourseId());
                    paymentInfo.put("courseName", payment.getCourse().getCourseName());
                    paymentInfo.put("amount", payment.getAmount());
                    paymentInfo.put("createdDate", payment.getCreatedDate());
                    paymentInfo.put("minutesExpired", java.time.Duration.between(payment.getCreatedDate(), LocalDateTime.now()).toMinutes());
                    
                    expiredPayments.add(paymentInfo);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalExpiredPayments", expiredPayments.size());
            response.put("expiredPayments", expiredPayments);
            response.put("checkTime", LocalDateTime.now());
            response.put("expirationThreshold", "15 minutes");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting expired pending payments: " + e.getMessage());
        }
    }

    /**
     * Test customer type logic với database thresholds
     */
    @GetMapping("/test-customer-type-logic")
    public ResponseEntity<?> testCustomerTypeLogic() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            // Test với các số tiền khác nhau
            BigDecimal[] testAmounts = {
                BigDecimal.ZERO,
                new BigDecimal("500000"),
                new BigDecimal("1000000"),
                new BigDecimal("2500000"),
                new BigDecimal("5000000"),
                new BigDecimal("10000000")
            };
            
            List<Map<String, Object>> testResults = new ArrayList<>();
            
            for (BigDecimal amount : testAmounts) {
                CustomerType suggestedType = paymentStatusService.determineCustomerType(amount);
                
                Map<String, Object> testResult = new HashMap<>();
                testResult.put("amount", amount);
                if (suggestedType != null) {
                    testResult.put("suggestedType", suggestedType.getCustomerTypeName());
                    testResult.put("suggestedTypeId", suggestedType.getCustomerTypeId());
                    testResult.put("threshold", suggestedType.getPrice());
                } else {
                    testResult.put("suggestedType", "Unable to determine");
                }
                
                testResults.add(testResult);
            }
            
            // Lấy thresholds từ database
            List<PaymentStatusService.CustomerTypeThreshold> thresholds = paymentStatusService.getCustomerTypeThresholds();
            
            response.put("testResults", testResults);
            response.put("databaseThresholds", thresholds);
            response.put("message", "Customer type logic test completed using database thresholds");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", "Customer type logic test failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
