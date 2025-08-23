package com.music.musicstore.controllers;

import com.music.musicstore.models.Cart;
import com.music.musicstore.models.Customer;
import com.music.musicstore.models.Order;
import com.music.musicstore.repositories.OrderRepository;
import com.music.musicstore.services.CartService;
import com.music.musicstore.services.MusicService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {
    private final MusicService musicService;
    private final CartService cartService;
    private final OrderRepository orderRepository;

    public HomeController(MusicService musicService, CartService cartService, OrderRepository orderRepository) {
        this.musicService = musicService;
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    @GetMapping({"/", "/home"})
    public String home() {
//        var page = musicService.getAllMusic(PageRequest.of(0, 20, Sort.by("createdAt").descending()));
//        model.addAttribute("musicPage", page);
        return "dashboards/homepage";
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

