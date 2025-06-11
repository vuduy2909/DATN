package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.customer.CustomerInfo;
import com.capstone.wellnessnavigatorgym.dto.customer.CustomerUserDetailDto;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.IAccountRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;

    @Autowired
    private IAccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public CustomerServiceImpl(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    @Override
    public Page<Customer> findAllCustomers(Pageable pageable) {
        return this.customerRepository.findAllCustomers(pageable);
    }

    @Override
    public Page<Customer> searchCustomers(String type, String name, String address, String phone, Pageable pageable) {
        return this.customerRepository.searchCustomer(type, name, address, phone, pageable);
    }

    @Override
    public Customer customerLimit() {
        return customerRepository.limitCustomer();
    }

    @Override
    public void saveCustomer(CustomerInfo customerInfo) {
        customerRepository.insertCustomer(customerInfo.getCustomerCode(), customerInfo.getCustomerName(),
                customerInfo.getCustomerEmail(), customerInfo.getCustomerPhone(), customerInfo.getCustomerGender(),
                customerInfo.getDateOfBirth(), customerInfo.getCustomerAddress(),
                customerInfo.getCustomerImg(), true, customerInfo.getCustomerType());
    }

    @SneakyThrows
    @Override
    public Customer findById(Integer id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return customer.get();
        }
        throw new NotFoundById("Could not find any customers with code: " + id);
    }

    @Override
    public void updateCustomer(CustomerInfo customerInfo, Integer id) {
        customerRepository.updateCustomer(id, customerInfo.getCustomerCode(), customerInfo.getCustomerName(),
                customerInfo.getCustomerEmail(), customerInfo.getCustomerPhone(), customerInfo.getCustomerGender(),
                customerInfo.getDateOfBirth(), customerInfo.getCustomerAddress(),
                customerInfo.getCustomerImg(), true, customerInfo.getCustomerType(), customerInfo.getAccount());
    }

    @Override
    public void deleteById(Integer id) {
        customerRepository.deleteCustomerId(id);
    }

    @Override
    public CustomerUserDetailDto findUserDetailByUsername(String username) {
        List<Tuple> tuples = customerRepository.findUserDetailByUsername(username);
        return CustomerUserDetailDto.TupleToCustomerDto(tuples);
    }

    @Override
    public Customer findByAccountUserName(String username) {
        return customerRepository.findByAccountUserName(username);
    }

    @Override
    public void updateCustomerAvatar(String username, String avatarUrl) {
        Customer customer = customerRepository.findActiveCustomerByAccountUserName(username);
        if (customer != null) {
            customer.setCustomerImg(avatarUrl);
            customerRepository.save(customer);
        }
    }

    @Override
    public boolean verifyCurrentPassword(String username, String password) {
        Optional<Account> accountOpt = accountRepository.findAccountByUserName(username);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (account.getEncryptPassword() != null) {
                return passwordEncoder.matches(password, account.getEncryptPassword());
            }
        }
        return false;
    }

    @Override
    public void updatePassword(String username, String encodedPassword) {
        Optional<Account> accountOpt = accountRepository.findAccountByUserName(username);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setEncryptPassword(encodedPassword);
            accountRepository.save(account);
        }
    }

    @Override
    public void updateCustomerProfile(String username, Map<String, String> profileData) {
        Customer customer = customerRepository.findByAccountUserName(username);
        if (customer != null) {
            // Update email in both customer and account tables for consistency
            if (profileData.get("customerEmail") != null) {
                customer.setCustomerEmail(profileData.get("customerEmail"));
                
                // Also update account email if account exists
                if (customer.getAccount() != null) {
                    customer.getAccount().setEmail(profileData.get("customerEmail"));
                }
            }
            
            // Update phone
            if (profileData.get("customerPhone") != null) {
                customer.setCustomerPhone(profileData.get("customerPhone"));
            }
            
            // Update address
            if (profileData.get("customerAddress") != null) {
                customer.setCustomerAddress(profileData.get("customerAddress"));
            }
            
            // Update gender
            if (profileData.get("customerGender") != null) {
                customer.setCustomerGender(profileData.get("customerGender"));
            }
            
            // Update date of birth
            if (profileData.get("customerDateOfBirth") != null && !profileData.get("customerDateOfBirth").trim().isEmpty()) {
                try {
                    java.time.LocalDate localDate = java.time.LocalDate.parse(profileData.get("customerDateOfBirth"));
                    java.sql.Date dateOfBirth = java.sql.Date.valueOf(localDate);
                    customer.setDateOfBirth(dateOfBirth);
                } catch (Exception e) {
                    // Handle date parsing error - skip update
                }
            }
            
            customerRepository.save(customer);
        }
    }

    @Override
    public Customer findByCustomerEmail(String email) {
        return customerRepository.findByCustomerEmail(email);
    }
    
    @Override
    public Customer findActiveCustomerByAccountUserName(String username) {
        return customerRepository.findActiveCustomerByAccountUserName(username);
    }
    
    @Override
    public void saveCustomer(Customer customer) {
        customerRepository.save(customer);
    }
} 