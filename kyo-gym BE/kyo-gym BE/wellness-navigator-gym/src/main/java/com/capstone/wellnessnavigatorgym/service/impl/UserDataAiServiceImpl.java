package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.UserDataAi;
import com.capstone.wellnessnavigatorgym.error.NotFoundById;
import com.capstone.wellnessnavigatorgym.repository.IUserDataAiRepository;
import com.capstone.wellnessnavigatorgym.service.IUserDataAiService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDataAiServiceImpl implements IUserDataAiService {

    @Autowired
    private IUserDataAiRepository userDataAiRepository;

    @Override
    public void saveUserDataAi(UserDataAi userDataAi) {
        if (userDataAi != null) {
            userDataAiRepository.save(userDataAi);
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
}
