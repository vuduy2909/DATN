package com.capstone.wellnessnavigatorgym.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Integer commentId;
    
    @NotNull(message = "Please provide a comment text")
    private String commentText;
    
    @NotNull(message = "Please provide customer ID")
    private String customer;
    
    @NotNull(message = "Please provide exercise ID")
    private String exercise;
    
    // Course and day are optional - can be null for exercise-only comments
    private String course;
    
    private String day;
    
    private Integer rating;
}
