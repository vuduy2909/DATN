package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Account;
import java.util.Optional;

public interface RefreshTokenService {
    String createRefreshToken(Integer accountId);
    boolean verifyExpiration(String token);
    Optional<Account> findByRefreshToken(String token);
    void deleteRefreshToken(Integer accountId);
} 