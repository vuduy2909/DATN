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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
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

    public String createPaymentUrl(PaymentRequest paymentRequest, String username) throws Exception {
        // Lấy thông tin customer và course
        Customer customer = customerRepository.findByAccountUserName(username);
        if (customer == null) {
            throw new RuntimeException("Customer not found");
        }
        
        Course course = courseRepository.findById(paymentRequest.getCourseId().intValue())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Tạo mã giao dịch unique
        String vnpTxnRef = generateTxnRef();
        
        // Tạo bản ghi payment
        VipPayment payment = new VipPayment();
        payment.setCustomer(customer);
        payment.setCourse(course);
        payment.setAmount(paymentRequest.getAmount());
        payment.setCurrency(paymentRequest.getCurrency());
        payment.setVnpTxnRef(vnpTxnRef);
        payment.setVnpOrderInfo(paymentRequest.getOrderInfo() != null ? 
                paymentRequest.getOrderInfo() : 
                "Thanh toán khóa học: " + course.getCourseName());
        payment.setPaymentStatus(VipPayment.PaymentStatus.PENDING);
        
        vipPaymentRepository.save(payment);

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
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                try {
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
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
        
        return vnpPayUrl + "?" + queryUrl;
    }

    public boolean verifyPayment(Map<String, String> vnpParams) {
        String vnpSecureHash = vnpParams.remove("vnp_SecureHash");
        
        // Sắp xếp parameters
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        
        String calculatedHash = hmacSHA512(vnpSecretKey, hashData.toString());
        return calculatedHash.equals(vnpSecureHash);
    }

    public void updatePaymentStatus(Map<String, String> vnpParams) {
        String vnpTxnRef = vnpParams.get("vnp_TxnRef");
        String vnpResponseCode = vnpParams.get("vnp_ResponseCode");
        String vnpTransactionNo = vnpParams.get("vnp_TransactionNo");
        String vnpPayDate = vnpParams.get("vnp_PayDate");
        
        VipPayment payment = vipPaymentRepository.findByVnpTxnRef(vnpTxnRef)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        payment.setVnpResponseCode(vnpResponseCode);
        payment.setVnpTransactionNo(vnpTransactionNo);
        payment.setVnpPayDate(vnpPayDate);
        payment.setUpdatedDate(LocalDateTime.now());
        
        // Cập nhật trạng thái thanh toán
        if ("00".equals(vnpResponseCode)) {
            payment.setPaymentStatus(VipPayment.PaymentStatus.SUCCESS);
            payment.setCompletedDate(LocalDateTime.now());
        } else {
            payment.setPaymentStatus(VipPayment.PaymentStatus.FAILED);
        }
        
        // Lưu full response từ VNPay
        payment.setPaymentGatewayResponse(vnpParams.toString());
        
        vipPaymentRepository.save(payment);
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