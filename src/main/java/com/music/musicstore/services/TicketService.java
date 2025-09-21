package com.music.musicstore.services;

import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Staff;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.models.support.TicketMessage;
import com.music.musicstore.models.order.Order;
import com.music.musicstore.repositories.TicketRepository;
import com.music.musicstore.repositories.TicketMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {
    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final CustomerService customerService;

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                        TicketMessageRepository ticketMessageRepository,
                        CustomerService customerService) {
        this.ticketRepository = ticketRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.customerService = customerService;
    }

    // Create ticket with Customer object
    public Ticket createTicket(Customer customer, String subject, String initialMessage) {
        Ticket ticket = new Ticket(subject, customer, initialMessage);
        ticket = ticketRepository.save(ticket); // Save ticket first

        // Now add the initial message to the new message system
        TicketMessage initialMsg = new TicketMessage(ticket, initialMessage, customer);
        ticketMessageRepository.save(initialMsg);
        ticket.addMessage(initialMsg);

        return ticket;
    }

    // Create ticket with Customer object and priority
    public Ticket createTicket(Customer customer, String subject, String initialMessage, String priority) {
        Ticket ticket = new Ticket(subject, customer, initialMessage);

        if ("HIGH".equalsIgnoreCase(priority)) {
            ticket.setStatus("URGENT");
        } else if ("MEDIUM".equalsIgnoreCase(priority)) {
            ticket.setStatus("OPEN");
        } else {
            ticket.setStatus("OPEN"); // Default to OPEN for low priority
        }

        ticket = ticketRepository.save(ticket); // Save ticket first

        // Now add the initial message to the new message system
        TicketMessage initialMsg = new TicketMessage(ticket, initialMessage, customer);
        ticketMessageRepository.save(initialMsg);
        ticket.addMessage(initialMsg);

        return ticket;
    }

    // Create ticket with username
    public Ticket createTicket(String username, String subject, String initialMessage) {
        Customer customer = customerService.findByUsername(username);
        return createTicket(customer, subject, initialMessage);
    }

    // Create ticket with username and priority
    public Ticket createTicket(String username, String subject, String initialMessage, String priority) {
        Customer customer = customerService.findByUsername(username);
        return createTicket(customer, subject, initialMessage, priority);
    }

    // Create ticket with Order reference
    public Ticket createTicket(Customer customer, Order order, String subject, String initialMessage) {
        Ticket ticket = new Ticket(order, subject, customer, initialMessage);
        ticket = ticketRepository.save(ticket); // Save ticket first

        // Now add the initial message to the new message system
        TicketMessage initialMsg = new TicketMessage(ticket, initialMessage, customer);
        ticketMessageRepository.save(initialMsg);
        ticket.addMessage(initialMsg);

        return ticket;
    }

    // Add message to existing ticket (from customer)
    public TicketMessage addCustomerMessage(Long ticketId, String content, Customer customer) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            TicketMessage message = new TicketMessage(ticket, content, customer);
            ticket.addMessage(message);
            ticketMessageRepository.save(message);
            ticketRepository.save(ticket);
            return message;
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Add message to existing ticket (from staff)
    public TicketMessage addStaffReply(Long ticketId, String content, Staff staff) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            TicketMessage message = new TicketMessage(ticket, content, staff);
            ticket.addMessage(message);
            ticketMessageRepository.save(message);
            ticketRepository.save(ticket);
            return message;
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Get all messages for a ticket
    public List<TicketMessage> getTicketMessages(Long ticketId) {
        return ticketMessageRepository.findByTicket_IdOrderByTimestampAsc(ticketId);
    }

    // Get tickets by username
    public List<Ticket> getTicketsByUsername(String username) {
        Customer customer = customerService.findByUsername(username);
        return ticketRepository.findByCustomer(customer);
    }

    // Get all tickets
    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    // Get ticket by ID
    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    // Update ticket status
    public Ticket updateTicketStatus(Long ticketId, String status) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(status);
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Assign ticket to staff
    public Ticket assignTicket(Long ticketId, Staff staff) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setAssignedStaff(staff);
            if ("OPEN".equals(ticket.getStatus())) {
                ticket.setStatus("IN_PROGRESS");
            }
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with ID: " + ticketId);
    }

    // Close ticket
    public Ticket closeTicket(Long ticketId) {
        return updateTicketStatus(ticketId, "CLOSED");
    }

    // Reopen ticket
    public Ticket reopenTicket(Long ticketId) {
        return updateTicketStatus(ticketId, "OPEN");
    }

    // Get tickets by status
    public List<Ticket> getTicketsByStatus(String status) {
        return ticketRepository.findByStatus(status);
    }

    // Get unassigned tickets
    public List<Ticket> getUnassignedTickets() {
        return ticketRepository.findByAssignedStaffIsNull();
    }

    // Get tickets assigned to staff
    public List<Ticket> getTicketsAssignedToStaff(Staff staff) {
        return ticketRepository.findByAssignedStaff(staff);
    }

    // Get urgent tickets
    public List<Ticket> getUrgentTickets() {
        return ticketRepository.findUrgentTickets();
    }

    // Get tickets needing attention
    public List<Ticket> getTicketsNeedingAttention() {
        return ticketRepository.findTicketsNeedingAttention();
    }

    // Get recent tickets
    public List<Ticket> getRecentTickets() {
        return ticketRepository.findRecentTickets();
    }

    // Search tickets
    public List<Ticket> searchTickets(String searchTerm) {
        return ticketRepository.findBySubjectOrMessageContentContaining(searchTerm);
    }

    // Search tickets for specific customer
    public List<Ticket> searchTicketsForCustomer(Customer customer, String searchTerm) {
        return ticketRepository.findByCustomerAndSearchTerm(customer, searchTerm);
    }

    // Get ticket statistics
    public List<Object[]> getStatusDistribution() {
        return ticketRepository.getStatusDistribution();
    }

    // Count tickets by status
    public long countTicketsByStatus(String status) {
        return ticketRepository.countByStatus(status);
    }

    // Count tickets for customer
    public long countTicketsForCustomer(Customer customer) {
        return ticketRepository.countByCustomer(customer);
    }

    // Delete ticket (admin only)
    public void deleteTicket(Long ticketId) {
        ticketRepository.deleteById(ticketId);
    }
}
