package com.capstone.wellnessnavigatorgym.entity;

import com.capstone.wellnessnavigatorgym.dto.comment.CommentDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
@Entity
@Setter
@Getter
@RequiredArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentId;
    private String commentText;
    private Integer rating;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    public Comment(CommentDto commentDto) {
        this.commentText = commentDto.getCommentText();
        this.rating = commentDto.getRating();
        this.customer = new Customer(Integer.parseInt(commentDto.getCustomer()));
        this.exercise = new Exercise(Integer.parseInt(commentDto.getExercise()));
    }
}
