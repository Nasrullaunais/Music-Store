package com.music.musicstore.controllers;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.services.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(@AuthenticationPrincipal Customer customer, Model model) {
        if (customer == null) {
            return "redirect:/login";
        }
        Cart cart = cartService.getOrCreateCart(customer);
        model.addAttribute("cart", cart);
        return "cart/index";
    }

    @PostMapping("/add/{musicId}")
    public String addToCart(@PathVariable Long musicId,
                            @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return "redirect:/login";
        }
        cartService.addToCart(customer, musicId);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId, @AuthenticationPrincipal Customer customer) {
        if (customer == null) {
            return "redirect:/login";
        }
        cartService.removeItem(customer, itemId);
        return "redirect:/cart";
    }
}
