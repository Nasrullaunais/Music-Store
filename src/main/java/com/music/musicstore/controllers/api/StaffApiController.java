package com.music.musicstore.controllers.api;

import com.music.musicstore.services.TicketService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/staff")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('STAFF')")
public class StaffApiController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MusicService musicService;

    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            return ResponseEntity.ok(ticketService.getAllTickets(page, size, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketDetails(@PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(ticketService.getTicketById(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<?> replyToTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.replyToTicket(
                ticketId, request.getMessage(), userDetails.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reply to ticket: " + e.getMessage()));
        }
    }

    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.updateTicketStatus(
                ticketId, request.getStatus(), userDetails.getUsername()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update ticket status: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/website")
    public ResponseEntity<?> getWebsiteAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            WebsiteAnalytics analytics = new WebsiteAnalytics();
            analytics.setTotalUsers(getAllUsersCount());
            analytics.setTotalOrders(orderService.getTotalOrdersCount(startDate, endDate));
            analytics.setTotalRevenue(orderService.getTotalRevenue(startDate, endDate));
            analytics.setMostPopularMusic(musicService.getMostPopularMusic(startDate, endDate));
            analytics.setActiveTickets(ticketService.getActiveTicketsCount());

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/reports/sales")
    public ResponseEntity<?> generateSalesReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            return ResponseEntity.ok(orderService.generateSalesReport(startDate, endDate, format));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to generate sales report: " + e.getMessage()));
        }
    }

    @GetMapping("/reports/music-performance")
    public ResponseEntity<?> getMusicPerformanceReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            return ResponseEntity.ok(musicService.getMusicPerformanceReport(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch music performance report: " + e.getMessage()));
        }
    }

    @GetMapping("/reports/customer-insights")
    public ResponseEntity<?> getCustomerInsights(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            return ResponseEntity.ok(orderService.getCustomerInsights(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch customer insights: " + e.getMessage()));
        }
    }

    private long getAllUsersCount() {
        // This would aggregate counts from all user services
        return 0; // Placeholder
    }

    // DTOs for requests and responses
    public static class TicketReplyRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class TicketStatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class WebsiteAnalytics {
        private long totalUsers;
        private long totalOrders;
        private double totalRevenue;
        private String mostPopularMusic;
        private long activeTickets;

        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

        public String getMostPopularMusic() { return mostPopularMusic; }
        public void setMostPopularMusic(String mostPopularMusic) { this.mostPopularMusic = mostPopularMusic; }

        public long getActiveTickets() { return activeTickets; }
        public void setActiveTickets(long activeTickets) { this.activeTickets = activeTickets; }
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
