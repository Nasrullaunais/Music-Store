package com.music.musicstore.controllers;

import com.music.musicstore.models.Cart;
import com.music.musicstore.models.Customer;
import com.music.musicstore.models.Order;
import com.music.musicstore.repositories.OrderRepository;
import com.music.musicstore.services.CartService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public DashboardController(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Customer customer, Model model) {
        if (customer == null) {
            return "redirect:/login";
        }
        List<Order> orders = orderRepository.findByCustomer(customer);
        Cart cart = cartService.getOrCreateCart(customer);
        model.addAttribute("user", customer);
        model.addAttribute("orders", orders);
        model.addAttribute("cart", cart);
        return "dashboards/user";
    }
}
