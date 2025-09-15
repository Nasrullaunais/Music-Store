package com.music.musicstore.services;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final CustomerService customerService;

    @Autowired
    public TicketService(TicketRepository ticketRepository, CustomerService customerService) {
        this.ticketRepository = ticketRepository;
        this.customerService = customerService;
    }

    // Original method - create ticket with Customer object
    public void createTicket(Customer customer, String subject, String message){
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setMessage(message);
        ticket.setStatus("OPEN");
        ticketRepository.save(ticket);
    }

    // Overloaded method - create ticket with Customer object and priority
    public Ticket createTicket(Customer customer, String subject, String message, String priority) {
        Ticket ticket = new Ticket();
        ticket.setCustomer(customer);
        ticket.setSubject(subject);
        ticket.setMessage(message);
        ticket.setStatus("OPEN");
        // Note: Priority field would need to be added to Ticket entity. For now, we'll use status to indicate priority
        if ("HIGH".equalsIgnoreCase(priority)) {
            ticket.setStatus("URGENT");
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            ticket.setStatus("OPEN");
        } else {
            ticket.setStatus("LOW_PRIORITY");
        }
        return ticketRepository.save(ticket);
    }

    // Create ticket with username (gets Customer by username)
    public Ticket createTicket(String username, String subject, String message) {
        Customer customer = customerService.findByUsername(username);
        return createTicket(customer, subject, message, "MEDIUM");
    }

    // Create ticket with username and priority
    public Ticket createTicket(String username, String subject, String message, String priority) {
        Customer customer = customerService.findByUsername(username);
        return createTicket(customer, subject, message, priority);
    }

    // Create ticket with Order reference
    public Ticket createTicket(Customer customer, Order order, String subject, String message) {
        Ticket ticket = new Ticket(order, subject, message);
        ticket.setCustomer(customer);
        ticket.setStatus("OPEN");
        return ticketRepository.save(ticket);
    }

    // Create ticket with Order reference and priority
    public Ticket createTicket(Customer customer, Order order, String subject, String message, String priority) {
        Ticket ticket = createTicket(customer, order, subject, message);
        if ("HIGH".equalsIgnoreCase(priority)) {
            ticket.setStatus("URGENT");
        } else if ("LOW".equalsIgnoreCase(priority)) {
            ticket.setStatus("LOW_PRIORITY");
        }
        return ticketRepository.save(ticket);
    }

    // Get tickets by username
    public List<Ticket> getTicketsByUsername(String username) {
        Customer customer = customerService.findByUsername(username);
        return ticketRepository.findByCustomer(customer);
    }

    // Get tickets by username with pagination
    public Page<Ticket> getTicketsByUsername(String username, int page, int size) {
        Customer customer = customerService.findByUsername(username);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ticketRepository.findByCustomer(customer, pageable);
    }

    // Get ticket details with username validation
    public Ticket getTicketDetails(Long ticketId, String username) {
        Ticket ticket = getTicketById(ticketId);
        Customer customer = customerService.findByUsername(username);

        if (!ticket.getCustomer().getId().equals(customer.getId())) {
            throw new SecurityException("You can only view your own tickets");
        }

        return ticket;
    }

    // Get all tickets for staff/admin
    public Page<Ticket> getAllTickets(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (status != null && !status.trim().isEmpty()) {
            return ticketRepository.findByStatus(status, pageable);
        }

        return ticketRepository.findAll(pageable);
    }

    // Get all tickets without status filter
    public Page<Ticket> getAllTickets(int page, int size) {
        return getAllTickets(page, size, null);
    }

    // Get tickets by status
    public List<Ticket> getTicketsByStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    // Get tickets by priority (using status field for now)
    public List<Ticket> getTicketsByPriority(String priority) {
        String statusEquivalent = switch(priority.toUpperCase()) {
            case "HIGH" -> "URGENT";
            case "LOW" -> "LOW_PRIORITY";
            default -> "OPEN";
        };
        return ticketRepository.findByStatus(statusEquivalent);
    }

    public Ticket getTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with id: " + ticketId));
    }

    // Close ticket
    public void closeTicket(Long id){
        Ticket ticket = getTicketById(id);
        ticket.setStatus("CLOSED");
        ticketRepository.save(ticket);
    }

    // Close ticket with reason
    public void closeTicket(Long id, String reason) {
        Ticket ticket = getTicketById(id);
        ticket.setStatus("CLOSED");
        if (reason != null && !reason.trim().isEmpty()) {
            String currentReply = ticket.getReply() != null ? ticket.getReply() : "";
            ticket.setReply(currentReply + "\n[CLOSURE REASON]: " + reason);
        }
        ticketRepository.save(ticket);
    }

    // Reply to ticket (original method)
    public void replyToTicket(Long id, String message){
        Ticket ticket = getTicketById(id);
        ticket.setReply(message);
        ticket.setStatus("REPLIED");
        ticketRepository.save(ticket);
    }

    // Reply to ticket with staff username tracking
    public void replyToTicket(Long ticketId, String reply, String staffUsername) {
        Ticket ticket = getTicketById(ticketId);
        String timestampedReply = "[" + LocalDateTime.now() + " - " + staffUsername + "]: " + reply;

        String currentReply = ticket.getReply() != null ? ticket.getReply() : "";
        if (!currentReply.isEmpty()) {
            timestampedReply = currentReply + "\n" + timestampedReply;
        }

        ticket.setReply(timestampedReply);
        ticket.setStatus("REPLIED");
        ticketRepository.save(ticket);
    }

    // Update ticket status
    public void updateTicketStatus(Long ticketId, String status, String staffUsername) {
        Ticket ticket = getTicketById(ticketId);
        String oldStatus = ticket.getStatus();
        ticket.setStatus(status);

        // Add status change log to reply
        String statusChange = "[" + LocalDateTime.now() + " - " + staffUsername + "]: Status changed from " + oldStatus + " to " + status;
        String currentReply = ticket.getReply() != null ? ticket.getReply() : "";

        if (!currentReply.isEmpty()) {
            ticket.setReply(currentReply + "\n" + statusChange);
        } else {
            ticket.setReply(statusChange);
        }

        ticketRepository.save(ticket);
    }

    // Update ticket status without staff tracking
    public void updateTicketStatus(Long ticketId, String status) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(status);
        ticketRepository.save(ticket);
    }

    // Analytics methods
    public long getActiveTicketsCount() {
        return ticketRepository.countByStatusIn(List.of("OPEN", "URGENT", "REPLIED", "IN_PROGRESS"));
    }

    public long getTotalTicketsCount() {
        return ticketRepository.count();
    }

    public long getClosedTicketsCount() {
        return ticketRepository.countByStatus("CLOSED");
    }

    public long getUrgentTicketsCount() {
        return ticketRepository.countByStatus("URGENT");
    }

    public Map<String, Long> getTicketStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        distribution.put("OPEN", ticketRepository.countByStatus("OPEN"));
        distribution.put("URGENT", ticketRepository.countByStatus("URGENT"));
        distribution.put("REPLIED", ticketRepository.countByStatus("REPLIED"));
        distribution.put("CLOSED", ticketRepository.countByStatus("CLOSED"));
        distribution.put("LOW_PRIORITY", ticketRepository.countByStatus("LOW_PRIORITY"));
        return distribution;
    }

    public Map<String, Object> getTicketAnalytics(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalTickets", getTotalTicketsCount());
        analytics.put("activeTickets", getActiveTicketsCount());
        analytics.put("closedTickets", getClosedTicketsCount());
        analytics.put("urgentTickets", getUrgentTicketsCount());
        analytics.put("statusDistribution", getTicketStatusDistribution());

        // Add time-based analytics if date range is provided
        if (startDate != null && endDate != null) {
            analytics.put("period", startDate + " to " + endDate);
            // Note: Would need createdAt field in Ticket entity for proper date filtering
        }

        return analytics;
    }

    // Final missing method for admin analytics
    public Map<String, Object> getDetailedTicketStats() {
        Map<String, Object> detailedStats = new HashMap<>();
        detailedStats.put("statusDistribution", getTicketStatusDistribution());
        detailedStats.put("totalTickets", getTotalTicketsCount());
        detailedStats.put("activeTickets", getActiveTicketsCount());
        detailedStats.put("closedTickets", getClosedTicketsCount());
        detailedStats.put("urgentTickets", getUrgentTicketsCount());

        // Additional detailed statistics
        detailedStats.put("averageResolutionTime", "2.5 days"); // Placeholder
        detailedStats.put("customerSatisfactionRate", "4.2/5"); // Placeholder
        detailedStats.put("firstResponseTime", "4 hours"); // Placeholder

        return detailedStats;
    }

    // Batch operations
    public void bulkUpdateStatus(List<Long> ticketIds, String newStatus, String staffUsername) {
        for (Long ticketId : ticketIds) {
            updateTicketStatus(ticketId, newStatus, staffUsername);
        }
    }

    public void bulkCloseTickets(List<Long> ticketIds, String reason) {
        for (Long ticketId : ticketIds) {
            closeTicket(ticketId, reason);
        }
    }

    // Search functionality
    public List<Ticket> searchTickets(String searchTerm) {
        return ticketRepository.findBySubjectContainingIgnoreCaseOrMessageContainingIgnoreCase(searchTerm, searchTerm);
    }

    public Page<Ticket> searchTickets(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return ticketRepository.findBySubjectContainingIgnoreCaseOrMessageContainingIgnoreCase(searchTerm, searchTerm, pageable);
    }

    // Customer-specific methods
    public List<Ticket> getCustomerActiveTickets(String username) {
        Customer customer = customerService.findByUsername(username);
        return ticketRepository.findByCustomerAndStatusIn(customer, List.of("OPEN", "URGENT", "REPLIED", "IN_PROGRESS"));
    }

    public boolean canCustomerViewTicket(Long ticketId, String username) {
        try {
            Ticket ticket = getTicketById(ticketId);
            Customer customer = customerService.findByUsername(username);
            return ticket.getCustomer().getId().equals(customer.getId());
        } catch (Exception e) {
            return false;
        }
    }

    // Customer search method placeholder (referenced in CustomerApiController)
    public List<Ticket> searchCustomerTickets(String username, String searchTerm) {
        Customer customer = customerService.findByUsername(username);
        return ticketRepository.findByCustomerAndSearchTerm(customer, searchTerm);
    }

    public List<Ticket> findTicketsNeedingAttention(int page, int size) {
        ticketRepository.findTicketsNeedingAttention();

    }
}
