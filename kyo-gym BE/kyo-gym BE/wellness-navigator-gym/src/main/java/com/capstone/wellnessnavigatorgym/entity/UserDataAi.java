package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@RequiredArgsConstructor
@Entity
public class UserDataAi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userDataId;
    private String activityLevel;
    private Integer age;
    private Double bmi;
    private String gender;
    private String trainingGoals;
    private String trainingHistory;
    private Boolean effective;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
