package com.capstone.wellnessnavigatorgym.dto.customer;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.Errors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerInfo {

    private Integer customerId;

    private String customerCode;

    @NotBlank(message = "Please enter your first and last name")
    @Pattern(regexp = "^[AÀẢÃÁẠĂẰẲẴẮẶÂẦẨẪẤẬBCDĐEÈẺẼÉẸÊỀỂỄẾỆFGHIÌỈĨÍỊJKLMNOÒỎÕÓỌÔỒỔỖỐỘƠỜỞỠỚỢPQRSTUÙỦŨÚỤƯỪỬỮỨỰVWXYỲỶỸÝỴZ][aàảãáạăằẳẵắặâầẩẫấậbcdđeèẻẽéẹêềểễếệfghiìỉĩíịjklmnoòỏõóọôồổỗốộơờởỡớợpqrstuùủũúụưừửữứựvwxyỳỷỹýỵz]+ [AÀẢÃÁẠĂẰẲẴẮẶÂẦẨẪẤẬBCDĐEÈẺẼÉẸÊỀỂỄẾỆFGHIÌỈĨÍỊJKLMNOÒỎÕÓỌÔỒỔỖỐỘƠỜỞỠỚỢPQRSTUÙỦŨÚỤƯỪỬỮỨỰVWXYỲỶỸÝỴZ][aàảãáạăằẳẵắặâầẩẫấậbcdđeèẻẽéẹêềểễếệfghiìỉĩíịjklmnoòỏõóọôồổỗốộơờởỡớợpqrstuùủũúụưừửữứựvwxyỳỷỹýỵz]+(?: [AÀẢÃÁẠĂẰẲẴẮẶÂẦẨẪẤẬBCDĐEÈẺẼÉẸÊỀỂỄẾỆFGHIÌỈĨÍỊJKLMNOÒỎÕÓỌÔỒỔỖỐỘƠỜỞỠỚỢPQRSTUÙỦŨÚỤƯỪỬỮỨỰVWXYỲỶỸÝỴZ][aàảãáạăằẳẵắặâầẩẫấậbcdđeèẻẽéẹêềểễếệfghiìỉĩíịjklmnoòỏõóọôồổỗốộơờởỡớợpqrstuùủũúụưừửữứựvwxyỳỷỹýỵz]*)*",message = "Full name is not in correct format")
    @Length(min = 3,max = 50, message = "The full name must contain at least 3 characters and a maximum of 50 characters")
    private String customerName;

    @NotBlank(message = "Please enter email")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",message = "Email is not in the correct format, please re-enter. For example: name_email@gmail.com")
    @Length(min = 6,max = 30,message = "Email names are only allowed to contain 6 to 30 characters")
    private String customerEmail;

    @NotBlank(message = "Please enter the phone number")
    @Pattern(regexp = "^(086|096|097|098|038|037|036|035|034|033|032|091|094|088|081|082|083|084|085|070|076|077|078|079|089|090|093|092|052|056|058|099|059|087)\\d{7}$",
            message = "Phone numbers are only allowed to have 10 digits and the prefix of the Vietnamese network operator")
    private String customerPhone;

    @NotNull(message = "Please select gender")
    private String customerGender;

    @NotNull(message = "Please enter date of birth")
    private Date dateOfBirth;



    @NotBlank(message = "Please enter your address")
    @Length(min = 5,max = 100, message = "The address must be at least 5 and maximum 100 characters")
    private String customerAddress;

    @NotNull(message = "Please choose a representative photo")
    private String customerImg;

    private Boolean isEnable;

    @NotNull(message = "Please select customer type")
    private CustomerType customerType;

    private Integer customerTypeId;

    private Account account;

    // Field to handle frontend sending username but ignore it during JSON parsing
    @JsonIgnore
    private String username;

    public void validate(Object target, Errors errors) {
        CustomerInfo customerInfo = (CustomerInfo) target;
        if (!(customerInfo.dateOfBirth == null)) {
            LocalDate today = LocalDate.now();
            LocalDate minAgeDate = today.minusYears(12);
            LocalDate maxAgeDate = today.minusYears(90);
            if (customerInfo.dateOfBirth.toLocalDate().isAfter(minAgeDate)) {
                errors.rejectValue("dateOfBirth", "", "Not yet 12 years old");
            }
            if (customerInfo.dateOfBirth.toLocalDate().isBefore(maxAgeDate)) {
                errors.rejectValue("dateOfBirth", "", "Greater than 90 years old");
            }
        }
    }
}
