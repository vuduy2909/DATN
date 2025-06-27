package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.exception.TokenRefreshException;
import com.capstone.wellnessnavigatorgym.repository.IAccountRepository;
import com.capstone.wellnessnavigatorgym.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    // Refresh token expires after 2 hours
    private static final int REFRESH_TOKEN_DURATION_HOURS = 2;

    @Autowired
    private IAccountRepository accountRepository;

    @Override
    public String createRefreshToken(Integer accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            String refreshToken = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(REFRESH_TOKEN_DURATION_HOURS);
            
            account.setRefreshToken(refreshToken);
            account.setRefreshTokenExpiryDate(expiryDate);
            
            accountRepository.save(account);
            return refreshToken;
        }
        throw new RuntimeException("Account not found with id: " + accountId);
    }

    @Override
    public boolean verifyExpiration(String token) {
        Optional<Account> accountOpt = accountRepository.findByRefreshToken(token);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            if (account.getRefreshTokenExpiryDate().isBefore(LocalDateTime.now())) {
                // Token expired, clear it
                account.setRefreshToken(null);
                account.setRefreshTokenExpiryDate(null);
                accountRepository.save(account);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<Account> findByRefreshToken(String token) {
        return accountRepository.findByRefreshToken(token);
    }

    @Override
    @Transactional
    public void deleteRefreshToken(Integer accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setRefreshToken(null);
            account.setRefreshTokenExpiryDate(null);
            accountRepository.save(account);
        }
    }
} 