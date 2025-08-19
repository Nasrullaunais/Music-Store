package com.music.musicstore.models;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Music music;


    @NotNull(message = "Unit price is required")
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    // Default constructor required by JPA
    public OrderItem() {
    }

    public OrderItem(Music music) {
        this.music = music;
        this.unitPrice = music.getPrice();
    }

    // Calculate subtotal
    public BigDecimal getSubtotal() {
        return unitPrice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Music getProduct() {
        return music;
    }

    public void setProduct(Music music) {
        this.music = music;
    }



    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}