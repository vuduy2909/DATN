package com.capstone.wellnessnavigatorgym.dto.customer;

import com.capstone.wellnessnavigatorgym.entity.Account;
import com.capstone.wellnessnavigatorgym.entity.Cart;
import com.capstone.wellnessnavigatorgym.entity.CustomerType;
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
    private Boolean customerGender;

    @NotNull(message = "Please enter date of birth")
    private Date dateOfBirth;

    @NotBlank(message = "Passport/ID card cannot be blank")
    @Pattern(regexp = "^(001|002|004|006|008|010|011|012|014|015|017|019|020|022|024|025|026|027|030|031|033|034|035|036|037|038|040|042|044|045|046|048|049|051|052|054|056|058|060|062|064|066|067|068|070|072|074|075|077|079|080|082|083|084|086|087|089|091|092|093|094|095|096)([0-9])(00|0[0-9]|1[0-9]|2[0-9]|3[0-9]|4[0-9]|5[0-9]|6[0-9]|7[0-9]|8[0-9]|9[0-9])([0-9]{6})$",
            message = "Passport/ID card must have 12 numbers")
    private String idCard;

    @NotBlank(message = "Please enter your address")
    @Length(min = 5,max = 100, message = "The address must be at least 5 and maximum 100 characters")
    private String customerAddress;

    @NotNull(message = "Please choose a representative photo")
    private String customerImg;

    private Boolean isEnable;

    @NotNull(message = "Please select customer type")
    private CustomerType customerType;

    private Account account;
    private Cart cart;

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
