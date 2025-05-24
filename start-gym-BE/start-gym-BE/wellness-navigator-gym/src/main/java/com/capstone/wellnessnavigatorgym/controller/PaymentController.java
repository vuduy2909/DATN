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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

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

    // @Autowired
    // private ICustomerCourseService customerCourseService;

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
            // Set IP address từ request
            String ipAddress = getClientIpAddress(request);
            paymentRequest.setIpAddress(ipAddress);
            
            // Set return URL mặc định nếu không có
            if (paymentRequest.getReturnUrl() == null) {
                paymentRequest.setReturnUrl("http://localhost:3000/payment/result");
            }

            // Tạo URL thanh toán
            String paymentUrl = vnPayService.createPaymentUrl(paymentRequest, principal.getName());
            
            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            response.put("message", "Payment URL created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
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
            // Verify signature từ VNPay
            boolean isValidSignature = vnPayService.verifyPayment(vnpParams);
            
            if (!isValidSignature) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Invalid signature from VNPay"));
            }

            // Cập nhật trạng thái thanh toán
            vnPayService.updatePaymentStatus(vnpParams);
            
            String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
            String vnpTxnRef = vnpParams.get("vnp_TxnRef");
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactionRef", vnpTxnRef);
            response.put("responseCode", vnpResponseCode);
            
            if ("00".equals(vnpResponseCode)) {
                response.put("status", "SUCCESS");
                response.put("message", "Payment successful");
                
                // Thêm khóa học vào customer_course (trigger sẽ tự động xử lý)
                // TODO: Implement enrollCourseAfterPayment method
                /*
                try {
                    customerCourseService.enrollCourseAfterPayment(vnpTxnRef);
                } catch (Exception e) {
                    System.err.println("Error enrolling course after payment: " + e.getMessage());
                }
                */
                
            } else {
                response.put("status", "FAILED");
                response.put("message", "Payment failed");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error processing payment return: " + e.getMessage()));
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
}
