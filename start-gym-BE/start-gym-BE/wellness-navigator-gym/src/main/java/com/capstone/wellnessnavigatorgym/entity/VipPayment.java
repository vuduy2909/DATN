package com.capstone.wellnessnavigatorgym.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vip_payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VipPayment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 0)
    private BigDecimal amount;
    
    @Column(name = "currency", length = 10)
    private String currency = "VND";
    
    // VNPay fields
    @Column(name = "vnp_txn_ref", nullable = false, unique = true)
    private String vnpTxnRef;
    
    @Column(name = "vnp_transaction_no", unique = true)
    private String vnpTransactionNo;
    
    @Column(name = "vnp_order_info", nullable = false)
    private String vnpOrderInfo;
    
    @Column(name = "vnp_pay_date", length = 14)
    private String vnpPayDate;
    
    @Column(name = "vnp_response_code", length = 2)
    private String vnpResponseCode;
    
    @Column(name = "vnp_secure_hash", columnDefinition = "TEXT")
    private String vnpSecureHash;
    
    // Payment status
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "VNPAY";
    
    @Column(name = "payment_gateway_response", columnDefinition = "LONGTEXT")
    private String paymentGatewayResponse;
    
    // Timestamps
    @Column(name = "created_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate = LocalDateTime.now();
    
    @Column(name = "completed_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedDate;
    
    public enum PaymentStatus {
        PENDING,
        SUCCESS, 
        FAILED,
        CANCELLED
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
} 