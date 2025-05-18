package com.capstone.wellnessnavigatorgym.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tiện ích để chèn dữ liệu mẫu vào CSDL
 * Sử dụng khi cần tạo bản ghi customer cho các tài khoản có sẵn
 */
@Component
public class CustomerDataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerDataInitializer.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Tạo bản ghi customer cho tài khoản có username cụ thể
     * @param username Tên đăng nhập của tài khoản
     * @return true nếu tạo thành công, false nếu có lỗi
     */
    @Transactional
    public boolean createCustomerForUsername(String username) {
        try {
            // Lấy account_id từ username
            Integer accountId = getAccountIdFromUsername(username);
            
            if (accountId == null) {
                logger.error("Không tìm thấy tài khoản với username: {}", username);
                return false;
            }
            
            // Kiểm tra xem đã có customer cho account này chưa
            if (isCustomerExistsForAccount(accountId)) {
                logger.info("Đã tồn tại bản ghi customer cho tài khoản: {}", username);
                return true; // Đã tồn tại thì cũng coi như thành công
            }
            
            // Lấy email từ account
            String email = getEmailFromUsername(username);
            
            // Tạo mã customer
            String customerCode = "CUST" + String.format("%03d", accountId);
            
            // Tạo bản ghi customer mới
            String sql = "INSERT INTO customer (customer_address, customer_code, customer_email, " +
                    "customer_gender, customer_img, customer_name, customer_phone, " +
                    "date_of_birth, id_card, is_enable, account_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            int result = jdbcTemplate.update(sql,
                    username.contains("admin") ? "789 Admin Street" : "456 User Street", // Địa chỉ
                    customerCode, // Mã customer
                    email, // Email
                    "Male", // Giới tính
                    "https://