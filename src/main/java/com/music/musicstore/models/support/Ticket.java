package com.music.musicstore.models.support;

import com.music.musicstore.models.order.Order;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = true)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id", nullable = true)
    private Staff assignedStaff;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime closedAt;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp ASC")
    @JsonIgnore  // Prevent lazy loading issues - use dedicated endpoint to get messages
    private List<TicketMessage> messages = new ArrayList<>();

    // Constructors
    public Ticket() {}

    public Ticket(Order order, String subject, Customer customer, String initialMessage) {
        this.order = order;
        this.subject = subject;
        this.customer = customer;
        // Don't add initial message here - do it after saving the ticket
    }

    public Ticket(String subject, Customer customer, String initialMessage) {
        this.subject = subject;
        this.customer = customer;
        // Don't add initial message here - do it after saving the ticket
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        if ("CLOSED".equals(status) && closedAt == null) {
            this.closedAt = LocalDateTime.now();
        }
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Staff getAssignedStaff() {
        return assignedStaff;
    }

    public void setAssignedStaff(Staff assignedStaff) {
        this.assignedStaff = assignedStaff;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime closedAt) {
        this.closedAt = closedAt;
    }

    public List<TicketMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessage> messages) {
        this.messages = messages;
    }

    // Helper methods
    public void addMessage(TicketMessage message) {
        this.messages.add(message);
        message.setTicket(this);
    }

    @JsonIgnore  // Prevent circular reference during JSON serialization
    public TicketMessage getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }

    @JsonIgnore  // Prevent circular reference during JSON serialization
    public String getInitialMessage() {
        if (!messages.isEmpty()) {
            return messages.get(0).getContent();
        }
        return "";
    }

    public boolean isAssigned() {
        return assignedStaff != null;
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }
}
