package com.music.musicstore.dto;

import java.util.List;

public class TicketDto {
    private Long id;
    private String subject;
    private String status;
    private String createdAt; // ISO 8601 UTC
    private UserSummaryDto customer;
    private UserSummaryDto staff; // nullable
    private TicketMessageDto lastMessage; // optional summary
    private List<TicketMessageDto> messages; // optional full messages

    public TicketDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public UserSummaryDto getCustomer() { return customer; }
    public void setCustomer(UserSummaryDto customer) { this.customer = customer; }

    public UserSummaryDto getStaff() { return staff; }
    public void setStaff(UserSummaryDto staff) { this.staff = staff; }

    public TicketMessageDto getLastMessage() { return lastMessage; }
    public void setLastMessage(TicketMessageDto lastMessage) { this.lastMessage = lastMessage; }

    public List<TicketMessageDto> getMessages() { return messages; }
    public void setMessages(List<TicketMessageDto> messages) { this.messages = messages; }
}

