package com.capstone.wellnessnavigatorgym.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "customer")
@Setter
@Getter
@RequiredArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;
    
    @Column(name = "customer_code")
    private String customerCode;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "customer_email")
    private String customerEmail;
    
    @Column(name = "customer_phone")
    private String customerPhone;
    
    @Column(name = "customer_gender")
    private String customerGender;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "date_of_birth")
    private Date dateOfBirth;

    @Column(name = "customer_address")
    private String customerAddress;
    
    @Column(name = "customer_img")
    private String customerImg;
    
    @Column(name = "is_enable")
    private Boolean isEnable;

    @Column(name = "total_vip_spent", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0.00")
    private BigDecimal totalVipSpent = BigDecimal.ZERO;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_type_id")
    private CustomerType customerType;

    @OneToOne
    @JoinColumn(name = "account_id")
    private Account account;

    public Customer(Integer customerId) {
        this.customerId = customerId;
    }

    public Customer(String customerCode, String customerName, String customerEmail, String customerPhone, String customerGender, Date dateOfBirth, String customerAddress, Boolean isEnable, Account account) {
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.customerGender = customerGender;
        this.dateOfBirth = dateOfBirth;
        this.customerAddress = customerAddress;
        this.isEnable = isEnable;
        this.account = account;
        this.totalVipSpent = BigDecimal.ZERO;
    }
}
