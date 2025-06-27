package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;
    
    @Column(name = "user_name")
    private String userName;
    
    @Column(name = "encrypt_password")
    private String encryptPassword;
    
    @Column(name = "email")
    private String email;
    
    @Column(name = "is_enable")
    private Boolean isEnable;
    
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "refresh_token")
    private String refreshToken;
    
    @Column(name = "refresh_token_expiry_date")
    private LocalDateTime refreshTokenExpiryDate;

    @ManyToMany
    @JoinTable(name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new LinkedHashSet<>();
    
    // Convenience methods for password field
    public String getPassword() {
        return this.encryptPassword;
    }
    
    public void setPassword(String password) {
        this.encryptPassword = password;
    }
    
    // Getter and setter for refresh token fields
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getRefreshTokenExpiryDate() {
        return refreshTokenExpiryDate;
    }
    
    public void setRefreshTokenExpiryDate(LocalDateTime refreshTokenExpiryDate) {
        this.refreshTokenExpiryDate = refreshTokenExpiryDate;
    }
}
