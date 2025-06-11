package com.capstone.wellnessnavigatorgym.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class CartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_detail_id")
    private Integer cartDetailId;
    
    @NotNull
    @Min(1)
    @Column(name = "quantity")
    private int quantity;
    
    @NotNull
    @Column(name = "status")
    private boolean status;

    @NotNull
    @Column(name = "cart_id")
    private Integer cartId;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_type_id")
    private CustomerType customerType;

}
