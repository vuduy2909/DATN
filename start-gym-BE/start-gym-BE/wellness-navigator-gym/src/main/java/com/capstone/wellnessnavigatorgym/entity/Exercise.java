package com.capstone.wellnessnavigatorgym.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exercise")
@Setter
@Getter
@RequiredArgsConstructor
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_id")
    private Integer exerciseId;
    
    @Column(name = "exercise_name")
    private String exerciseName;
    
    @Column(name = "body_part")
    private String bodyPart;
    
    @Column(name = "equipment")
    private String equipment;
    
    @Column(name = "video_url", length = 2000)
    private String videoUrl;
    
    @Column(name = "target")
    private String target;
    
    @Column(name = "exercise_description", length = 2000)
    private String exerciseDescription;
    
    @Column(name = "instructions", length = 2000)
    private String instructions;
    
    @Column(name = "is_enable")
    private Boolean isEnable;
    
    @Column(name = "duration")
    private Double duration;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @Column(name = "views")
    private Integer views;

    public Exercise(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }
}
