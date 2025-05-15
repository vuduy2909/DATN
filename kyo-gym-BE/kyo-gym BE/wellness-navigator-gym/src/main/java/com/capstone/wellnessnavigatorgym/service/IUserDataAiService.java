package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.UserDataAi;

public interface IUserDataAiService {
    UserDataAi saveUserDataAi(UserDataAi userDataAi);

    UserDataAi findUserDataAiById(Integer id);
}
