package com.capstone.wellnessnavigatorgym.controller;

import com.capstone.wellnessnavigatorgym.dto.cart.CartWithDetail;
import com.capstone.wellnessnavigatorgym.entity.Cart;
import com.capstone.wellnessnavigatorgym.entity.CartDetail;
import com.capstone.wellnessnavigatorgym.service.ICartDetailService;
import com.capstone.wellnessnavigatorgym.service.ICartService;
import com.capstone.wellnessnavigatorgym.service.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private ICartDetailService cartDetailService;

    @Autowired
    private IEmailService emailService;

    @GetMapping("")
    public ResponseEntity<CartWithDetail> findCartByUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Cart cart = cartService.findByUsername(username);
        List<CartDetail> cartDetailList;
        if (cart != null) {
            Integer id = cart.getCartId();
            cartDetailList = cartDetailService.findByCartId(id);
        } else {
            cart = new Cart();
            cartDetailList = new ArrayList<>();
        }
        CartWithDetail cartWithDetail = new CartWithDetail(cart, cartDetailList);
        return new ResponseEntity<>(cartWithDetail, HttpStatus.OK);
    }

    @GetMapping("/add/{customerTypeId}")
    public ResponseEntity<CartWithDetail> addToCart(@PathVariable("customerTypeId") Integer customerTypeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Cart cart = cartService.findByUsername(username);
        Integer cartId = cart.getCartId();
        CartDetail cartDetail = cartDetailService.checkAvailable(customerTypeId, cartId);
        if (cartDetail == null) {
            cartDetailService.add(customerTypeId, cartId);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<CartWithDetail> updateCart(@RequestBody CartWithDetail cartWithDetail) {
        Cart cart = cartWithDetail.getCart();
        cartService.update(cart);
        List<CartDetail> cartDetailList = cartWithDetail.getCartDetailList();
        for (CartDetail cartDetail : cartDetailList) {
            if (cartDetail.getQuantity() > 0) {
                cartDetail.setStatus(false);
                cartDetailService.update(cartDetail);
            } else {
                cartDetailService.deleteById(cartDetail.getCartDetailId());
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("/checkout")
    public ResponseEntity<CartWithDetail> checkout(@RequestBody CartWithDetail cartWithDetail) {
        Cart cart = cartWithDetail.getCart();
        List<CartDetail> cartDetailList = cartWithDetail.getCartDetailList();
        List<CartDetail> details = new ArrayList<>();
        int totalAmount = 0;
        cartService.update(cart);
        for (CartDetail cartDetail : cartDetailList) {
            if (!cartDetail.isStatus()) {
                totalAmount += cartDetail.getQuantity() * cartDetail.getCustomerType().getPrice();
                details.add(cartDetail);
            }
            cartDetailService.update(cartDetail);
        }
        if (totalAmount != 0) {
            emailService.emailProcess(cart, totalAmount, details);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
