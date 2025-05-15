package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.UserDataAi;

import java.util.List;

public interface IUserDataAiService {
    UserDataAi saveUserDataAi(UserDataAi userDataAi);

    UserDataAi findUserDataAiById(Integer id);
    
    List<UserDataAi> findByCustomerId(Integer customerId);
}
