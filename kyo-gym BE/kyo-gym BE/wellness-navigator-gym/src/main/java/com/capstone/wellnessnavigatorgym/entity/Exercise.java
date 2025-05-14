package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer exerciseId;
    private String exerciseName;
    private String bodyPart;
    private String equipment;
    @Column(name = "video_url", length = 2000)
    private String videoUrl;
    private String target;
    @Column(name = "exercise_description", length = 2000)
    private String exerciseDescription;
    @Column(name = "instructions", length = 2000)
    private String instructions;
    private Boolean isEnable;
    private Double duration;
    private LocalDateTime uploadDate;
    private Integer views;

    public Exercise(Integer exerciseId) {
        this.exerciseId = exerciseId;
    }
}
