package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.request.PaymentRequest;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.Course;
import com.capstone.wellnessnavigatorgym.entity.VipPayment;
import com.capstone.wellnessnavigatorgym.repository.IVipPaymentRepository;
import com.capstone.wellnessnavigatorgym.repository.ICourseRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class VNPayService {

    @Value("${vnpay.url}")
    private String vnpPayUrl;

    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.secretKey}")
    private String vnpSecretKey;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    @Autowired
    private IVipPaymentRepository vipPaymentRepository;

    @Autowired
    private ICourseRepository courseRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public String createPaymentUrl(PaymentRequest paymentRequest, String username) throws Exception {
        try {
            System.out.println("=== VNPay Payment URL Creation ===");
            System.out.println("Username: " + username);
            System.out.println("Course ID: " + paymentRequest.getCourseId());
            System.out.println("Amount: " + paymentRequest.getAmount());
            System.out.println("Currency: " + paymentRequest.getCurrency());
            System.out.println("Order Info: " + paymentRequest.getOrderInfo());
            
        // Lấy thông tin customer và course
        Customer customer = customerRepository.findByAccountUserName(username);
        if (customer == null) {
                throw new RuntimeException("Customer not found for username: " + username);
        }
        
            Course course = courseRepository.findById(paymentRequest.getCourseId())
                    .orElseThrow(() -> new RuntimeException("Course not found with ID: " + paymentRequest.getCourseId()));

            System.out.println("Customer found: " + customer.getCustomerName());
            System.out.println("Course found: " + course.getCourseName());
            System.out.println("Course isVip: " + course.getIsVip());
            System.out.println("Course price: " + course.getPrice());

            // Kiểm tra course VIP
            if (!course.getIsVip() || course.getPrice() == null || course.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Course is not a valid VIP course with price. Course ID: " + course.getCourseId() + ", isVip: " + course.getIsVip() + ", price: " + course.getPrice());
            }

        // Tạo mã giao dịch unique
        String vnpTxnRef = generateTxnRef();
            System.out.println("Generated TxnRef: " + vnpTxnRef);
        
        // Tạo bản ghi payment
        VipPayment payment = new VipPayment();
        payment.setCustomer(customer);
        payment.setCourse(course);
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setVnpTxnRef(vnpTxnRef);
        payment.setVnpOrderInfo(paymentRequest.getOrderInfo() != null ? 
                paymentRequest.getOrderInfo() : 
                    "VIP Course Payment: " + course.getCourseName());
        payment.setPaymentStatus(VipPayment.PaymentStatus.PENDING);
        
        vipPaymentRepository.save(payment);
            System.out.println("Payment record saved with ID: " + payment.getPaymentId());

        // Tạo parameters cho VNPay
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(paymentRequest.getAmount().longValue() * 100)); // VNPay yêu cầu nhân 100
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", payment.getVnpOrderInfo());
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", paymentRequest.getReturnUrl() != null ? 
                paymentRequest.getReturnUrl() : vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", paymentRequest.getIpAddress());

        // Thời gian tạo và hết hạn
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cld.add(Calendar.MINUTE, 15); // Hết hạn sau 15 phút
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);

        // Sắp xếp parameters và tạo query string
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                try {
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Error encoding field: " + fieldName + " = " + fieldValue, e);
                }
                
                // Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString()));
                query.append('=');
                try {
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException("Error encoding query field: " + fieldName + " = " + fieldValue, e);
                }
                
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        
        String queryUrl = query.toString();
        String vnpSecureHash = hmacSHA512(vnpSecretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnpSecureHash;
        
            String finalUrl = vnpPayUrl + "?" + queryUrl;
            
            // Log thông tin debug
            System.out.println("VNPay URL created successfully:");
            System.out.println("Customer: " + customer.getCustomerName());
            System.out.println("Course: " + course.getCourseName());
            System.out.println("Amount: " + paymentRequest.getAmount());
            System.out.println("TxnRef: " + vnpTxnRef);
            System.out.println("Final URL length: " + finalUrl.length());
            
            return finalUrl;
            
        } catch (Exception e) {
            System.err.println("Error creating VNPay payment URL: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create payment URL: " + e.getMessage(), e);
        }
    }

    public boolean verifyPayment(Map<String, String> vnpParams) {
        System.out.println("=== VNPay Payment Verification ===");
        System.out.println("Input params: " + vnpParams);
        
        // Create a copy to avoid modifying the original map
        Map<String, String> paramsCopy = new HashMap<>(vnpParams);
        String vnpSecureHash = paramsCopy.remove("vnp_SecureHash");
        System.out.println("Received vnp_SecureHash: " + vnpSecureHash);
        
        // Sắp xếp parameters
        List<String> fieldNames = new ArrayList<>(paramsCopy.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = paramsCopy.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        String calculatedHash = hmacSHA512(vnpSecretKey, hashData.toString());
        System.out.println("Hash data string: " + hashData.toString());
        System.out.println("Calculated hash: " + calculatedHash);
        System.out.println("Signature verification result: " + calculatedHash.equals(vnpSecureHash));
        
        return calculatedHash.equals(vnpSecureHash);
    }

    public void updatePaymentStatus(Map<String, String> vnpParams) {
        String vnpTxnRef = vnpParams.get("vnp_TxnRef");
        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnpTransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnpPayDate = vnpParams.get("vnp_PayDate");
        
        System.out.println("=== Updating Payment Status ===");
        System.out.println("TxnRef: " + vnpTxnRef);
        System.out.println("ResponseCode: " + vnpResponseCode);
        System.out.println("TransactionNo: " + vnpTransactionNo);
        System.out.println("PayDate: " + vnpPayDate);
        
        VipPayment payment = vipPaymentRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found for TxnRef: " + vnpTxnRef));
        
        System.out.println("Found payment with ID: " + payment.getPaymentId());
        
        payment.setVnpResponseCode(vnpResponseCode);
        payment.setVnpTransactionNo(vnpTransactionNo);
        payment.setVnpPayDate(vnpPayDate);
        payment.setUpdatedDate(LocalDateTime.now());
        
        // Cập nhật trạng thái thanh toán
        if ("00".equals(vnpResponseCode)) {
            payment.setPaymentStatus(VipPayment.PaymentStatus.SUCCESS);
            payment.setCompletedDate(LocalDateTime.now());
            System.out.println("Payment status set to SUCCESS");
        } else {
            payment.setPaymentStatus(VipPayment.PaymentStatus.FAILED);
            System.out.println("Payment status set to FAILED");
        }
        
        // Lưu full response từ VNPay (but remove sensitive data first)
        Map<String, String> safeParams = new HashMap<>(vnpParams);
        safeParams.remove("vnp_SecureHash"); // Remove hash for storage
        
        String responseToStore = null;
        
        try {
            // Ensure we have ObjectMapper available
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
            
            String jsonResponse = objectMapper.writeValueAsString(safeParams);
            System.out.println("Generated JSON: " + jsonResponse);
            
            // Validate the JSON by parsing it back
            objectMapper.readTree(jsonResponse);
            System.out.println("JSON validation successful");
            
            // Truncate if too long (MySQL TEXT field limit is usually 65535)
            if (jsonResponse.length() > 60000) {
                jsonResponse = jsonResponse.substring(0, 60000) + "...truncated";
            }
            
            responseToStore = jsonResponse;
            System.out.println("VNPay response will be saved as JSON: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())) + "...");
            
        } catch (Exception e) {
            System.err.println("Error with JSON conversion/validation: " + e.getMessage());
            e.printStackTrace();
            
            // Create a simple valid JSON object as fallback
            try {
                Map<String, Object> fallbackMap = new HashMap<>();
                fallbackMap.put("status", "response_saved");
                fallbackMap.put("vnp_ResponseCode", vnpResponseCode);
                fallbackMap.put("vnp_TxnRef", vnpTxnRef);
                fallbackMap.put("vnp_TransactionNo", vnpTransactionNo);
                fallbackMap.put("vnp_PayDate", vnpPayDate);
                fallbackMap.put("error", "Full response could not be stored as JSON");
                
                responseToStore = objectMapper.writeValueAsString(fallbackMap);
                System.out.println("Using minimal JSON fallback: " + responseToStore);
                
            } catch (Exception e2) {
                System.err.println("Even fallback JSON failed: " + e2.getMessage());
                // Last resort - store as null to avoid database error
                responseToStore = null;
            }
        }
        
        payment.setPaymentGatewayResponse(responseToStore);
        
        System.out.println("About to save payment to database...");
        vipPaymentRepository.save(payment);
        System.out.println("Payment saved successfully!");
    }

    private String generateTxnRef() {
        return "VIP_" + System.currentTimeMillis();
    }

    private String hmacSHA512(String key, String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException();
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes();
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            return "";
        }
    }
    
    // Getter methods for testing
    public String getVnpPayUrl() {
        return vnpPayUrl;
    }
    
    public String getVnpTmnCode() {
        return vnpTmnCode;
    }
} 