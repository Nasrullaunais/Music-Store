package com.music.musicstore.repositories;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.users.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomer(Customer customer);
}
