package com.music.musicstore.services;

import com.music.musicstore.models.*;
import com.music.musicstore.repositories.CartItemRepository;
import com.music.musicstore.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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

}
