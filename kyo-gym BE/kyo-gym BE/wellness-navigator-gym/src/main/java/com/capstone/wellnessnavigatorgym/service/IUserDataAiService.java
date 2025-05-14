package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.UserDataAi;

public interface IUserDataAiService {
    void saveUserDataAi(UserDataAi userDataAi);

    UserDataAi findUserDataAiById(Integer id);
}
