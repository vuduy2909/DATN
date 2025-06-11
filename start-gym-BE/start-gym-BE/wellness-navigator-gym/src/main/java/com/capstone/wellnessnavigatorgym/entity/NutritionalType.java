package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "nutritional_type")
@Setter
@Getter
@RequiredArgsConstructor
public class NutritionalType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer nutritionalTypeId;
    private String nutritionalTypeName;
}
