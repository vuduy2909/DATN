package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
public class ExerciseDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exercise_day_id")
    private Integer exerciseDayId;
    
    @Column(name = "is_completed")
    private Boolean isCompleted;
    
    @Column(name = "status")
    private Boolean status;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "day_id")
    private Day day;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;
}
