package com.capstone.wellnessnavigatorgym.repository;

import com.capstone.wellnessnavigatorgym.entity.CartDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface ICartDetailRepository extends JpaRepository<CartDetail, Integer> {

    @Query(value = "SELECT * FROM cart_detail WHERE cart_id = :id AND status = false AND quantity > 0", nativeQuery = true)
    List<CartDetail> findByCartId(@Param("id") Integer id);

    @Query(value = "SELECT * FROM cart_detail " +
            "WHERE customer_type_id = :customer_type_id AND cart_id = :cart_id AND status = false AND quantity > 0",
            nativeQuery = true)
    Optional<CartDetail> checkAvailable(@Param("customer_type_id") Integer id, @Param("cart_id") Integer cart_id);

    @Modifying
    @Query(value = "INSERT INTO cart_detail (customer_type_id, quantity, status, cart_id) " +
            "VALUES(:customer_type_id, 1, false, :cart_id)",
            nativeQuery = true)
    void insertCartDetail(@Param("customer_type_id") Integer customer_type_id, @Param("cart_id") Integer cart_id);

    @Modifying
    @Query(value = "UPDATE cart_detail " +
            "SET customer_type_id = :customer_type_id, quantity = :quantity, status = :status, cart_id = :cart_id " +
            "WHERE cart_detail_id = :cart_detail_id",
            nativeQuery = true)
    void updateCartDetail(@Param("customer_type_id") Integer customer_type_id,
                          @Param("quantity") int quantity,
                          @Param("status") boolean status,
                          @Param("cart_id") Integer cart_id,
                          @Param("cart_detail_id") Integer cart_detail_id);
}
