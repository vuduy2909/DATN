package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.dto.customer.CustomerInfo;
import com.capstone.wellnessnavigatorgym.dto.customer.CustomerUserDetailDto;
import com.capstone.wellnessnavigatorgym.entity.Cart;
import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.ICartRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.Tuple;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;
    private final ICartRepository cartRepository;

    @Autowired
    public CustomerServiceImpl(ICustomerRepository customerRepository, ICartRepository cartRepository) {
        this.customerRepository = customerRepository;
        this.cartRepository = cartRepository;
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
        cartRepository.insertCart(customerInfo.getCustomerName(), customerInfo.getCustomerAddress(), customerInfo.getCustomerPhone(), customerInfo.getCustomerEmail());
        Cart cart = cartRepository.findLastCart().orElse(null);
        if (cart != null) {
            customerRepository.insertCustomer(customerInfo.getCustomerCode(), customerInfo.getCustomerName(),
                    customerInfo.getCustomerEmail(), customerInfo.getCustomerPhone(), customerInfo.getCustomerGender(),
                    customerInfo.getDateOfBirth(), customerInfo.getIdCard(), customerInfo.getCustomerAddress(),
                    customerInfo.getCustomerImg(), true, customerInfo.getCustomerType(), cart.getCartId());
        }
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
                customerInfo.getDateOfBirth(), customerInfo.getIdCard(), customerInfo.getCustomerAddress(),
                customerInfo.getCustomerImg(), true, customerInfo.getCustomerType(), customerInfo.getAccount(),
                customerInfo.getCart());
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
    public Customer findByCart(Cart cart) {
        return customerRepository.findByCart(cart);
    }
}
