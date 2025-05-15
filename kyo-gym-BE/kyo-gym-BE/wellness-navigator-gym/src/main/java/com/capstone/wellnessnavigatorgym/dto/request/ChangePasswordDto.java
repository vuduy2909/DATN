package com.capstone.wellnessnavigatorgym.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {
    private String username;
    @NotBlank(message = "Please do not leave it blank!")
    private String presentPassword;
    @NotBlank(message = "Please do not leave it blank!")
    @Size(min = 6, max = 20, message = "The new password must be between 5 and 20 characters long.")
    private String confirmPassword;
}
