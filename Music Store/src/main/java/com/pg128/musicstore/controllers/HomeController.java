package com.pg128.musicstore.controllers;

import com.pg128.musicstore.models.Admin;
import com.pg128.musicstore.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.annotation.PostConstruct;

@Controller
public class HomeController {

    private final AdminService adminService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public HomeController(AdminService adminService, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Initialize the first admin user if no admins exist
     */
    @PostConstruct
    public void init() {
        if (adminService.getAllAdmins().isEmpty()) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@musicstore.com");
            adminService.createAdmin(admin);
            System.out.println("Default admin user created: admin / admin123");
        }
    }
}