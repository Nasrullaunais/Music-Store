package com.music.musicstore.services;

import com.music.musicstore.models.*;
import com.music.musicstore.repositories.CartItemRepository;
import com.music.musicstore.repositories.CartRepository;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MusicRepository musicRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository, MusicRepository musicRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.musicRepository = musicRepository;
    }

    public Cart getOrCreateCart(Customer customer) {
        return cartRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Cart c = new Cart(customer);
                    return cartRepository.save(c);
                });
    }

    @Transactional
    public void addToCart(Customer customer, Long musicId) {
        Cart cart = getOrCreateCart(customer);
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new IllegalArgumentException("Music not found"));


        CartItem item = new CartItem(music);
        item.setCart(cart);
        cartItemRepository.save(item);
        cart.getItems().add(item);

        cartRepository.save(cart);
    }

    @Transactional
    public void removeItem(Customer customer, Long cartItemId) {
        Cart cart = getOrCreateCart(customer);
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        if (item.getCart().getId().equals(cart.getId())) {
            cartItemRepository.delete(item);
        } else {
            throw new IllegalArgumentException("Item does not belong to user's cart");
        }
    }
}
