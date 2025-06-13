package com.capstone.wellnessnavigatorgym.entity;

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
@Table(name = "customer_day_completion")
public class CustomerDayCompletion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_id")
    private Integer completionId;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    @Column(name = "completion_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;
    
    // Lifecycle methods
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        
        // Tự động set completion_date khi is_completed = true
        if (isCompleted && completionDate == null) {
            completionDate = LocalDateTime.now();
        }
        
        // Clear completion_date khi is_completed = false
        if (!isCompleted) {
            completionDate = null;
        }
    }
    
    // Constructor với các tham số cần thiết
    // CHỈ được sử dụng khi user thực sự điểm danh thành công
    public CustomerDayCompletion(Customer customer, Course course, Day day) {
        this.customer = customer;
        this.course = course;
        this.day = day;
        this.isCompleted = false;
        this.completionDate = null; // Sẽ được set khi markAsCompleted() được gọi
    }
    
    // Helper method để đánh dấu hoàn thành
    public void markAsCompleted() {
        this.isCompleted = true;
        this.completionDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper method để đánh dấu chưa hoàn thành
    public void markAsIncomplete() {
        this.isCompleted = false;
        this.completionDate = null;
        this.updatedAt = LocalDateTime.now();
    }
} 