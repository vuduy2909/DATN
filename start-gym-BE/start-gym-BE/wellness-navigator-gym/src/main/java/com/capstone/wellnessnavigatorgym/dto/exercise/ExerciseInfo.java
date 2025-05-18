package com.capstone.wellnessnavigatorgym.dto.exercise;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExerciseInfo {

    private Integer exerciseId;

    @NotBlank(message = "Exercise name cannot be blank")
    @Size(max = 100, message = "Exercise name cannot exceed 100 characters")
    @Pattern(regexp="^[a-zA-Z0-9 ]*$", message = "Exercise name can only contain letters, numbers and spaces")
    private String exerciseName;

    @NotBlank(message="Body part cannot be blank")
    @Size(max = 100, message= "Body part cannot exceed 100 characters")
    private String bodyPart;

    @NotBlank(message = "Equipment cannot be blank")
    @Size(max = 100, message = "Equipment cannot exceed 100 characters")
    private String equipment;

    @NotBlank(message = "Video URL cannot be blank")
    @URL(message="Invalid URL format for video")
    private String videoUrl;

    @NotBlank(message = "Target cannot be blank")
    @Size(max= 100 , message= "Target cannot exceed 100 characters")
    private String target;

    @NotBlank(message = "Description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String exerciseDescription;

    @NotBlank(message="Instructions cannot be blank")
    private String instructions;

    private Boolean isEnable;

    private Double duration;

    private LocalDateTime uploadDate;

    private Integer views;

    private Integer days;
}
