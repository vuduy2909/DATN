package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.Cart;

public interface ICartService {
    Cart update(Cart cart);
    Cart findByUsername(String username);
    Cart findById(Integer id);
}
