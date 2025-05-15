package com.capstone.wellnessnavigatorgym.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer cartId;
    private int totalAmount;
    @OneToMany
    private Set<CartDetail> cartDetails = new LinkedHashSet<>();
    private String tnxRef;
    private boolean isPaid;
}
