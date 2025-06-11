package com.capstone.wellnessnavigatorgym.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    @XmlElement
    private Integer courseId;
    
    @Column(name = "course_name")
    private String courseName;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @Column(name = "duration")
    private String duration;
    
    @Column(name = "image", length = 2000)
    private String image;
    
    @Column(name = "status")
    private Boolean status = false;

    // New fields for VIP payment system
    @Column(name = "price", precision = 10, scale = 0)
    private BigDecimal price = BigDecimal.ZERO;
    
    @Column(name = "is_vip")
    private Boolean isVip = false;
    
    // Rating system for courses - simplified to just average rating
    @Column(name = "average_rating", precision = 3, scale = 2)
    private Double averageRating = 0.0;
    
    @Column(name = "created_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate = LocalDateTime.now();
    
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate = LocalDateTime.now();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "course_type_id")
    private CourseType courseType;

    public Course(Integer courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("courseId cannot be null when creating Course");
        }
        this.courseId = courseId;
    }

    public Integer getCourseId() {
        return courseId;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}


