package com.capstone.wellnessnavigatorgym.dto.cart;

import com.capstone.wellnessnavigatorgym.entity.Cart;
import com.capstone.wellnessnavigatorgym.entity.CartDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartWithDetail {
    private Cart cart;
    private List<CartDetail> cartDetailList;
}
