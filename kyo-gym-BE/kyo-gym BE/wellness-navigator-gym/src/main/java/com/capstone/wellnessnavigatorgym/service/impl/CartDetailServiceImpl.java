package com.capstone.wellnessnavigatorgym.service.impl;

import com.capstone.wellnessnavigatorgym.entity.CartDetail;
import com.capstone.wellnessnavigatorgym.repository.ICartDetailRepository;
import com.capstone.wellnessnavigatorgym.service.ICartDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartDetailServiceImpl implements ICartDetailService {

    @Autowired
    private ICartDetailRepository cartDetailRepository;

    @Override
    public void add(Integer customerTypeId, Integer cartId) {
        cartDetailRepository.insertCartDetail(customerTypeId, cartId);
    }

    @Override
    public List<CartDetail> findByCartId(Integer id) {
        return cartDetailRepository.findByCartId(id);
    }

    @Override
    public CartDetail update(CartDetail cartDetail) {
        if (cartDetail != null) {
            Integer cart_detail_id = cartDetail.getCartDetailId();
            Integer customer_type_id = cartDetail.getCustomerType().getCustomerTypeId();
            int quantity = cartDetail.getQuantity();
            boolean status = cartDetail.isStatus();
            Integer cart_id = cartDetail.getCartId();
            if (cart_detail_id != null) {
                cartDetailRepository.updateCartDetail(customer_type_id, quantity, status, cart_id, cart_detail_id);
            }
        }
        return cartDetail;
    }

    @Override
    public CartDetail checkAvailable(Integer customer_type_id, Integer cart_id) {
        return cartDetailRepository.checkAvailable(customer_type_id, cart_id).orElse(null);
    }

    @Override
    public void deleteById(Integer id) {
        cartDetailRepository.deleteById(id);
    }
}
