package com.music.musicstore.controllers;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.services.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final CustomerService customerService;

    public UserController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new Customer());
        return "register";
    }

    @PostMapping("/register")
    public String doRegister(@ModelAttribute Customer user) {
        // default role if not provided
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_CUSTOMER");
        }
//        customerService.registerCustomer(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/artist/login")
    public String artistLoginPage() {
        return "login";
    }

    @GetMapping("/artist/register")
    public String artistRegisterPage(Model model) {
        model.addAttribute("user", new Customer());
        return "register";
    }

//    @PostMapping("/artist/register")
//    public String doArtistRegister(@ModelAttribute Artist user) {
//
//    }
}
