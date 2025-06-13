package com.capstone.wellnessnavigatorgym.entity;

import com.capstone.wellnessnavigatorgym.dto.comment.CommentDto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;
    
    @Column(name = "comment_text", nullable = false)
    private String commentText;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = true)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = true)
    private Day day;
    
    @Column(name = "created_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate = LocalDateTime.now();
    
    @Column(name = "is_reported")
    private Boolean isReported = false;
    
    @Column(name = "report_count")
    private Integer reportCount = 0;
    
    @Column(name = "report_reason")
    private String reportReason;

    @Column(name = "rating")
    private Integer rating;

    public Comment(CommentDto commentDto) {
        this.commentText = commentDto.getCommentText();
        this.customer = new Customer(Integer.parseInt(commentDto.getCustomer()));
        this.exercise = new Exercise(Integer.parseInt(commentDto.getExercise()));
        
        // Handle optional course and day
        if (commentDto.getCourse() != null && !commentDto.getCourse().trim().isEmpty()) {
        this.course = new Course(Integer.parseInt(commentDto.getCourse()));
        }
        
        if (commentDto.getDay() != null && !commentDto.getDay().trim().isEmpty()) {
        this.day = new Day(Integer.parseInt(commentDto.getDay()));
        }

        if (commentDto.getRating() != null) {
            this.rating = commentDto.getRating();
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    
    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
        this.updatedDate = LocalDateTime.now();
    }
}
