package com.pg128.musicstore.controllers;

import com.pg128.musicstore.models.Admin;
import com.pg128.musicstore.models.Order;
import com.pg128.musicstore.services.AdminService;
import com.pg128.musicstore.services.CustomerService;
import com.pg128.musicstore.services.OrderService;
import com.pg128.musicstore.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CustomerService customerService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminController(AdminService adminService, ProductService productService,
                          OrderService orderService, CustomerService customerService,
                          PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.productService = productService;
        this.orderService = orderService;
        this.customerService = customerService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        // Get current admin
        Admin admin = (Admin) authentication.getPrincipal();
        model.addAttribute("admin", admin);
        
        // Dashboard statistics
        model.addAttribute("totalProducts", productService.getAllProducts().size());
        model.addAttribute("totalCustomers", customerService.getAllCustomers().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("pendingOrders", orderService.getOrdersByStatus(Order.OrderStatus.PENDING).size());
        
        // Recent orders
        model.addAttribute("recentOrders", 
                orderService.getPagedOrders(PageRequest.of(0, 5, Sort.by("orderDate").descending())));
        
        return "admin/dashboard";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication authentication) {
        Admin admin = (Admin) authentication.getPrincipal();
        model.addAttribute("admin", admin);
        return "admin/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, Authentication authentication) {
        Admin admin = (Admin) authentication.getPrincipal();
        model.addAttribute("admin", admin);
        return "admin/edit-profile";
    }

    @PostMapping("/profile/edit")
    public String updateProfile(@Valid Admin admin, BindingResult result, 
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/edit-profile";
        }
        
        try {
            adminService.updateAdmin(admin);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
            return "redirect:/admin/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating profile: " + e.getMessage());
            return "redirect:/admin/profile/edit";
        }
    }

    @GetMapping("/admins")
    public String listAdmins(Model model) {
        model.addAttribute("admins", adminService.getAllAdmins());
        return "admin/admins";
    }

    @GetMapping("/admins/new")
    public String newAdminForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin/new-admin";
    }

    @PostMapping("/admins/new")
    public String createAdmin(@Valid Admin admin, BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/new-admin";
        }
        
        try {
            adminService.createAdmin(admin);
            redirectAttributes.addFlashAttribute("success", "Admin created successfully");
            return "redirect:/admin/admins";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating admin: " + e.getMessage());
            return "redirect:/admin/admins/new";
        }
    }

    @GetMapping("/admins/{id}/edit")
    public String editAdminForm(@PathVariable Long id, Model model, 
                               RedirectAttributes redirectAttributes) {
        Optional<Admin> admin = adminService.getAdminById(id);
        if (admin.isPresent()) {
            model.addAttribute("admin", admin.get());
            return "admin/edit-admin";
        } else {
            redirectAttributes.addFlashAttribute("error", "Admin not found");
            return "redirect:/admin/admins";
        }
    }

    @PostMapping("/admins/{id}/edit")
    public String updateAdmin(@PathVariable Long id, @Valid Admin admin, 
                             BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/edit-admin";
        }
        
        try {
            adminService.updateAdmin(admin);
            redirectAttributes.addFlashAttribute("success", "Admin updated successfully");
            return "redirect:/admin/admins";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating admin: " + e.getMessage());
            return "redirect:/admin/admins/" + id + "/edit";
        }
    }

    @PostMapping("/admins/{id}/delete")
    public String deleteAdmin(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteAdmin(id);
            redirectAttributes.addFlashAttribute("success", "Admin deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting admin: " + e.getMessage());
        }
        return "redirect:/admin/admins";
    }
}