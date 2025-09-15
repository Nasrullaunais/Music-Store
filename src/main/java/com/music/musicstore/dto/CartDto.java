package com.music.musicstore.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartDto {
    private Long id;
    private List<CartItemDto> items;
    private BigDecimal total;
    private String customerUsername;

    // Constructors
    public CartDto() {}

    public CartDto(Long id, List<CartItemDto> items, BigDecimal total, String customerUsername) {
        this.id = id;
        this.items = items;
        this.total = total;
        this.customerUsername = customerUsername;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getCustomerUsername() { return customerUsername; }
    public void setCustomerUsername(String customerUsername) { this.customerUsername = customerUsername; }
}

class CartItemDto {
    private Long id;
    private MusicDto music;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Constructors
    public CartItemDto() {}

    public CartItemDto(Long id, MusicDto music, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.id = id;
        this.music = music;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public MusicDto getMusic() { return music; }
    public void setMusic(MusicDto music) { this.music = music; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
