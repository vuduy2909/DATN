package com.capstone.wellnessnavigatorgym.dto.customer;

import com.capstone.wellnessnavigatorgym.dto.course.CourseDetail;
import com.capstone.wellnessnavigatorgym.utils.ConvertToInteger;
import lombok.*;

import javax.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUserDetailDto {
    Integer customerId;
    String customerCode;
    String customerName;
    String customerPhone;
    String customerGender;
    Date dateOfBirth;
    String idCard;
    String customerAddress;
    String customerImg;
    String customerEmail;
    String customerTypeName;
    String username;
    String accountEmail;
    List<CourseDetail> recommendedCourses;

    public static CustomerUserDetailDto TupleToCustomerDto(List<Tuple> tuples) {
        if (tuples != null && !tuples.isEmpty()) {
            Tuple firstTuple = tuples.get(0);

            // Handle customer_gender conversion safely
            String customerGender = null;
            Object genderValue = firstTuple.get("customer_gender");
            if (genderValue != null) {
                if (genderValue instanceof Boolean) {
                    customerGender = ((Boolean) genderValue) ? "Male" : "Female";
                } else if (genderValue instanceof String) {
                    customerGender = (String) genderValue;
                }
            }

            CustomerUserDetailDto customerUserDetailDto = new CustomerUserDetailDto(
                    ConvertToInteger.convertToInteger(firstTuple.get("customer_id")),
                    firstTuple.get("customer_code", String.class),
                    firstTuple.get("customer_name", String.class),
                    firstTuple.get("customer_phone", String.class),
                    customerGender,
                    firstTuple.get("date_of_birth", Date.class),
                    firstTuple.get("id_card", String.class),
                    firstTuple.get("customer_address", String.class),
                    firstTuple.get("customer_img", String.class),
                    firstTuple.get("customer_email", String.class),
                    firstTuple.get("customer_type_name", String.class),
                    firstTuple.get("user_name", String.class),
                    firstTuple.get("email", String.class),
                    new ArrayList<>()
            );

            List<CourseDetail> recommendedCourses = tuples.stream()
                    .map(tuple -> {
                        // Safely handle recommended_status conversion from BIT to Boolean
                        Boolean recommendedStatus = null;
                        Object statusValue = tuple.get("recommended_status");
                        if (statusValue != null) {
                            if (statusValue instanceof Boolean) {
                                recommendedStatus = (Boolean) statusValue;
                            } else if (statusValue instanceof Number) {
                                recommendedStatus = ((Number) statusValue).intValue() > 0;
                            } else if (statusValue instanceof String) {
                                recommendedStatus = "true".equalsIgnoreCase((String) statusValue) || 
                                                   "1".equals((String) statusValue);
                            }
                        }
                        
                        // Only create CourseDetail if course_id exists
                        Object courseIdObj = tuple.get("course_id");
                        if (courseIdObj != null) {
                            return new CourseDetail(
                                ConvertToInteger.convertToInteger(tuple.get("user_data_id")),
                                ConvertToInteger.convertToInteger(tuple.get("age")),
                                tuple.get("bmi", Double.class),
                                tuple.get("gender", String.class),
                                tuple.get("training_goals", String.class),
                                tuple.get("training_history", String.class),
                                recommendedStatus,
                                ConvertToInteger.convertToInteger(courseIdObj),
                                tuple.get("course_name", String.class),
                                tuple.get("description", String.class),
                                tuple.get("duration", String.class),
                                tuple.get("image", String.class),
                                tuple.get("status", Boolean.class),
                                tuple.get("course_type_name", String.class)
                            );
                        }
                        return null;
                    })
                    .filter(course -> course != null) // Filter out null entries
                    .collect(Collectors.toList());

            customerUserDetailDto.setRecommendedCourses(recommendedCourses);

            return customerUserDetailDto;
        }

        return null;
    }
}
