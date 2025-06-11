package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@RequiredArgsConstructor
@Table(name = "days")
public class Day {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_id")
    private Integer dayId;
    
    @Column(name = "day_name")
    private String dayName;
    
    // Add constructor with dayId
    public Day(Integer dayId) {
        this.dayId = dayId;
    }
}
