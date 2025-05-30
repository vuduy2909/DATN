package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Customer;
import com.capstone.wellnessnavigatorgym.entity.UserDataAi;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.ICustomerRepository;
import com.capstone.wellnessnavigatorgym.repository.IUserDataAiRepository;
import com.capstone.wellnessnavigatorgym.service.IUserDataAiService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
public class UserDataAiServiceImpl implements IUserDataAiService {

    private static final Logger logger = LoggerFactory.getLogger(UserDataAiServiceImpl.class);

    @Autowired
    private IUserDataAiRepository userDataAiRepository;
    
    @Autowired
    private ICustomerRepository customerRepository;

    @Override
    public UserDataAi saveUserDataAi(UserDataAi userDataAi) {
        if (userDataAi != null) {
            return userDataAiRepository.save(userDataAi);
        } else {
            throw new IllegalArgumentException("userDataAi cannot be null");
        }
    }

    @SneakyThrows
    @Override
    public UserDataAi findUserDataAiById(Integer id) {
        Optional<UserDataAi> userDataAi = userDataAiRepository.findById(id);
        if (userDataAi.isPresent()) {
            return userDataAi.get();
        }
        throw new NotFoundById("Could not find any AI user data with code: " + id);
    }

    @Override
    public List<UserDataAi> findByCustomerId(Integer customerId) {
        if (customerId == null) {
            logger.warn("Attempted to find UserDataAi with null customerId, returning empty list");
            return new ArrayList<>();
        }
        
        // Sử dụng phương thức đơn giản hơn nếu repository đã có sẵn
        return userDataAiRepository.findByCustomer_CustomerId(customerId);
    }
}
