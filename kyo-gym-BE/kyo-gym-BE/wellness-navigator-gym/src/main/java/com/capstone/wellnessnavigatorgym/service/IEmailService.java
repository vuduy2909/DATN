package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Cart;
import com.capstone.wellnessnavigatorgym.entity.CartDetail;

import java.util.List;

public interface IEmailService {
    void emailProcess(Cart cart, Integer totalAmount, List<CartDetail> details);
}
