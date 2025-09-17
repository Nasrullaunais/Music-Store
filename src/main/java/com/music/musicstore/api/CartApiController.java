package com.music.musicstore.api;

import com.music.musicstore.dto.CartDto;
import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.services.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartApiController {

    private final CartService cartService;

    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<CartDto> getCart(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        Cart cart = cartService.getOrCreateCart(customer);
        return ResponseEntity.ok(convertToDto(cart));
    }

    @PostMapping("/add/{musicId}")
    public ResponseEntity<CartDto> addToCart(@PathVariable Long musicId,
                                           @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            cartService.addToCart(customer, musicId);
            Cart cart = cartService.getOrCreateCart(customer);
            return ResponseEntity.ok(convertToDto(cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<CartDto> removeFromCart(@PathVariable Long itemId,
                                                @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            cartService.removeItem(customer, itemId);
            Cart cart = cartService.getOrCreateCart(customer);
            return ResponseEntity.ok(convertToDto(cart));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/clear")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return ResponseEntity.status(401).build();
        }

        // Clear cart logic - remove all items from customer's cart
        Cart cart = cartService.getOrCreateCart(customer);
        cart.getItems().clear();
        cartService.saveCart(cart);
        return ResponseEntity.ok().build();
    }

    private CartDto convertToDto(Cart cart) {
        // Implementation would convert Cart entity to CartDto
        // This would include converting cart items and calculating totals
        return new CartDto(); // Simplified for now
    }
}
