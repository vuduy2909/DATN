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
}
