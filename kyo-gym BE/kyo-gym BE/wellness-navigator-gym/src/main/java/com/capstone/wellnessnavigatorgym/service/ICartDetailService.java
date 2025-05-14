package com.capstone.wellnessnavigatorgym.service;

import com.capstone.wellnessnavigatorgym.entity.CartDetail;

import java.util.List;

public interface ICartDetailService {

    void add(Integer customerTypeId, Integer cartId);

    List<CartDetail> findByCartId(Integer id);

    CartDetail update(CartDetail cartDetail);

    CartDetail checkAvailable(Integer customer_type_id, Integer cart_id);

    void deleteById(Integer id);
}
