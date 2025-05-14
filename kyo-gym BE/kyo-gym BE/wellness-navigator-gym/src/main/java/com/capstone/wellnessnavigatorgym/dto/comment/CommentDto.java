package com.capstone.wellnessnavigatorgym.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Integer commentId;
    @NotNull(message = "Please provide a comment text")
    private String commentText;
    @NotNull(message = "Please select a rating")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    private Integer rating;
    private String customer;
    private String exercise;
}
