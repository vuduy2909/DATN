package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.ICustomerTypeRepository;
import com.capstone.wellnessnavigatorgym.service.ICustomerTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerTypeServiceImpl implements ICustomerTypeService {

    @Autowired
    private ICustomerTypeRepository customerTypeRepository;

    @Autowired
    private ICustomerRepository customerRepository;

    @Override
    public List<CustomerType> findAllCustomerType() {
        return customerTypeRepository.findAll();
    }

    @Override
    public CustomerType upgradeCustomerType(Customer customer) {
        Integer nextTypeId = customer.getCustomerType().getCustomerTypeId() + 1;
        Optional<CustomerType> nextCustomerType = customerTypeRepository.findById(nextTypeId);

        if (nextCustomerType.isPresent()) {
            customer.setCustomerType(nextCustomerType.get());
            customerRepository.save(customer);
            return nextCustomerType.get();
        } else {
            return customer.getCustomerType();
        }
    }
}
