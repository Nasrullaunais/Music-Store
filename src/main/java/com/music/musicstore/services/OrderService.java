package com.music.musicstore.services;

import com.music.musicstore.models.cart.Cart;
import com.music.musicstore.models.cart.CartItem;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.order.OrderItem;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.CartItemRepository;
import com.music.musicstore.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final EmailSender emailSender;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartService cartService, CartItemRepository cartItemRepository, EmailSender emailSender) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.emailSender = emailSender;
    }

    public void placeOrder(Customer customer){
        Cart cart = cartService.getOrCreateCart(customer);
        if(cart.getItems().isEmpty()){
            throw new IllegalStateException("Cannot place order with empty cart");
        }

        Order order = new Order(cart);
        order.setOrderDate(LocalDateTime.now());
        order.setCustomer(customer);
        order.setTotalAmount(cart.getTotalAmount());

        emailSender.sendReceipt(order.getTotalAmount(), cart.getItemList(), customer.getEmail(), order.getId().toString());

        for(CartItem cartItem : cart.getItems()){
            OrderItem orderItem = new OrderItem(cartItem);
            orderItem.setOrder(order);
            order.getOrderItems().add(orderItem);
        }

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();



        orderRepository.save(order);
    }

    public void cancelOrder(Long orderId, Customer customer){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if(!order.getCustomer().getId().equals(customer.getId())){
            throw new IllegalArgumentException("Order does not belong to the customer");
        }

        if(order.getStatus() == Order.OrderStatus.CANCELLED){
            throw new IllegalStateException("Order is already cancelled");
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    public void updateOrderStatus(Long orderId, Order.OrderStatus status){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        order.setStatus(status);
        orderRepository.save(order);
    }

    // Missing methods for customer functionality
    public Order checkout(String username) {
        // Implementation would get customer and place order
        throw new UnsupportedOperationException("Checkout not implemented yet");
    }

    public Order purchaseMusic(String username, Long musicId) {
        // Implementation would handle direct music purchase
        throw new UnsupportedOperationException("Purchase music not implemented yet");
    }

    public Page<Order> getOrdersByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Implementation would get orders by username
        return Page.empty(pageable);
    }

    public Order getOrderDetails(Long orderId, String username) {
        // Implementation would get order details with validation
        throw new UnsupportedOperationException("Get order details not implemented yet");
    }

    // Missing methods for admin functionality
    public Page<Order> getAllOrdersForAdmin(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        // Implementation would get all orders with optional status filter
        return Page.empty(pageable);
    }

    public void refundOrder(Long orderId) {
        // Implementation would handle order refund
        throw new UnsupportedOperationException("Refund order not implemented yet");
    }

    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public long getTotalOrdersCount(LocalDate startDate, LocalDate endDate) {
        // Implementation would count orders in date range
        return 0;
    }

    public double getTotalRevenue() {
        // Implementation would sum all order totals
        return 0.0;
    }

    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        // Implementation would sum revenue in date range
        return 0.0;
    }

    public Map<String, Object> getSalesAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalOrders", getTotalOrdersCount(startDate, endDate));
        analytics.put("totalRevenue", getTotalRevenue(startDate, endDate));
        return analytics;
    }

    // Missing methods for staff functionality
    public Map<String, Object> generateSalesReport(LocalDate startDate, LocalDate endDate, String format) {
        Map<String, Object> report = new HashMap<>();
        report.put("totalSales", getTotalOrdersCount(startDate, endDate));
        report.put("revenue", getTotalRevenue(startDate, endDate));
        report.put("format", format);
        return report;
    }

    public Map<String, Object> getCustomerInsights(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> insights = new HashMap<>();
        insights.put("totalCustomers", 0);
        insights.put("repeatCustomers", 0);
        return insights;
    }

}
