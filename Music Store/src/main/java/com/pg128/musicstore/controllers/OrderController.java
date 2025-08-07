package com.pg128.musicstore.controllers;

import com.pg128.musicstore.models.Customer;
import com.pg128.musicstore.models.Order;
import com.pg128.musicstore.services.CustomerService;
import com.pg128.musicstore.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class OrderController {

    private final OrderService orderService;
    private final CustomerService customerService;

    @Autowired
    public OrderController(OrderService orderService, CustomerService customerService) {
        this.orderService = orderService;
        this.customerService = customerService;
    }

    @GetMapping
    public String listOrders(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) Long customerId,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<Order> orderPage;
        
        if (customerId != null) {
            Optional<Customer> customer = customerService.getCustomerById(customerId);
            if (customer.isPresent()) {
                orderPage = orderService.getPagedOrdersByCustomer(customer.get(), pageRequest);
                model.addAttribute("selectedCustomer", customer.get());
            } else {
                orderPage = orderService.getPagedOrders(pageRequest);
            }
        } else if (status != null && !status.isEmpty()) {
            try {
                Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
                orderPage = orderService.getPagedOrdersByStatus(orderStatus, pageRequest);
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                orderPage = orderService.getPagedOrders(pageRequest);
            }
        } else if (startDate != null && endDate != null) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            orderPage = orderService.getPagedOrdersByDateRange(startDateTime, endDateTime, pageRequest);
            model.addAttribute("startDate", startDate);
            model.addAttribute("endDate", endDate);
        } else {
            orderPage = orderService.getPagedOrders(pageRequest);
        }
        
        model.addAttribute("orders", orderPage);
        model.addAttribute("statuses", Order.OrderStatus.values());
        
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isPresent()) {
            model.addAttribute("order", order.get());
            model.addAttribute("statuses", Order.OrderStatus.values());
            return "admin/orders/view";
        } else {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }
    }

    @PostMapping("/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, 
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            orderService.updateOrderStatus(id, newStatus);
            redirectAttributes.addFlashAttribute("success", "Order status updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid order status");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating order status: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("success", "Order cancelled successfully");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error cancelling order: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}