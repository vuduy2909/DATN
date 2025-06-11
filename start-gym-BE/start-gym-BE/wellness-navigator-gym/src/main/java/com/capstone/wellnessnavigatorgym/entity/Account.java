package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
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
}
