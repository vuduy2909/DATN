package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "customer_type")
@Setter
@Getter
@RequiredArgsConstructor
public class CustomerType {
    @Id
    @Column(name = "customer_type_id")
    private Integer customerTypeId;
    
    @Column(name = "customer_type_name", unique = true, nullable = false)
    private String customerTypeName;
    
    @Column(name = "price", nullable = false)
    private Double price;
}
