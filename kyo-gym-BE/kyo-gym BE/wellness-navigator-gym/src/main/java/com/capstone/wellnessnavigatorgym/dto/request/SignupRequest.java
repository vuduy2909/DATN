package com.capstone.wellnessnavigatorgym.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    private String customerCode;
    private String name;
    private String username;
    private Boolean gender;
    private Date dateOfBirth;
    private String address;
    private String phone;
    private String idCard;
    private String email;
    private String password;
}
