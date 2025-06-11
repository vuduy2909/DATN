package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.dto.customer.CustomerInfo;
import com.capstone.wellnessnavigatorgym.dto.customer.CustomerUserDetailDto;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface ICustomerService {
    void save(Customer customer);

    Page<Customer> findAllCustomers(Pageable pageable);

    Page<Customer> searchCustomers(String type, String name, String address, String phone, Pageable pageable);

    Customer customerLimit();

    void saveCustomer(CustomerInfo customerInfo);

    Customer findById(Integer id);

    void updateCustomer(CustomerInfo customerInfo, Integer id);

    void deleteById(Integer id);

    CustomerUserDetailDto findUserDetailByUsername(String username);

    Customer findByAccountUserName(String username);

    void updateCustomerAvatar(String username, String avatarUrl);
    
    boolean verifyCurrentPassword(String username, String password);
    
    void updatePassword(String username, String encodedPassword);
    
    void updateCustomerProfile(String username, Map<String, String> profileData);
}
