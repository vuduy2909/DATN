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
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "cart_id")
    private Integer cartId;
    
    @Column(name = "total_amount")
    private int totalAmount;
    
    @OneToMany
    private Set<CartDetail> cartDetails = new LinkedHashSet<>();
    
    @Column(name = "tnx_ref")
    private String tnxRef;
    
    @Column(name = "is_paid")
    private boolean isPaid;
}
