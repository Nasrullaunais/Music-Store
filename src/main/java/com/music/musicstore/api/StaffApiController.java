package com.music.musicstore.api;

import com.music.musicstore.services.TicketService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.repositories.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

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

    // Enhanced ticket management for staff
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

    @GetMapping("/tickets/urgent")
    public ResponseEntity<?> getUrgentTickets() {
        try {
            return ResponseEntity.ok(ticketService.getTicketsByPriority("HIGH"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch urgent tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/needs-attention")
    public ResponseEntity<?> getTicketsNeedingAttention(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            return ResponseEntity.ok(ticketService.findTicketsNeedingAttention(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets needing attention: " + e.getMessage()));
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
            ticketService.replyToTicket(ticketId, request.getMessage(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Reply sent successfully"));
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
            ticketService.updateTicketStatus(ticketId, request.getStatus(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Ticket status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update ticket status: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(
            @PathVariable Long ticketId,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String reason = request != null ? request.get("reason") : null;
            if (reason != null && !reason.trim().isEmpty()) {
                ticketService.closeTicket(ticketId, "Staff closed: " + reason);
            } else {
                ticketService.closeTicket(ticketId);
            }
            return ResponseEntity.ok(new SuccessResponse("Ticket closed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to close ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/stats")
    public ResponseEntity<?> getTicketStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("total", ticketService.getTotalTicketsCount());
            stats.put("active", ticketService.getActiveTicketsCount());
            stats.put("closed", ticketService.getClosedTicketsCount());
            stats.put("urgent", ticketService.getUrgentTicketsCount());
            stats.put("statusDistribution", ticketService.getTicketStatusDistribution());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket statistics: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/bulk/status")
    public ResponseEntity<?> bulkUpdateTicketStatus(
            @RequestBody BulkTicketUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.bulkUpdateStatus(Arrays.asList(request.getTicketIds()), request.getNewStatus(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Tickets updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update tickets: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/bulk/close")
    public ResponseEntity<?> bulkCloseTickets(
            @RequestBody BulkTicketCloseRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.bulkCloseTickets(Arrays.asList(request.getTicketIds()), request.getReason());
            return ResponseEntity.ok(new SuccessResponse("Tickets closed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to close tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<?> searchTickets(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(ticketService.searchTickets(query, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to search tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/priority/{priority}")
    public ResponseEntity<?> getTicketsByPriority(
            @PathVariable String priority,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(ticketService.getTicketsByPriority(priority, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets by priority: " + e.getMessage()));
        }
    }

    @PutMapping("/tickets/{ticketId}/priority")
    public ResponseEntity<?> updateTicketPriority(
            @PathVariable Long ticketId,
            @RequestBody TicketPriorityUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.updateTicketPriority(ticketId, request.getPriority(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Ticket priority updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update ticket priority: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<?> assignTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketAssignmentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.assignTicket(ticketId, request.getAssigneeUsername(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Ticket assigned successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to assign ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/assigned-to-me")
    public ResponseEntity<?> getMyAssignedTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.getTicketsAssignedTo(userDetails.getUsername(), page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch assigned tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/website")
    public ResponseEntity<?> getWebsiteAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            WebsiteAnalytics analytics = new WebsiteAnalytics();
            analytics.setTotalUsers(0L); // Placeholder - would implement proper counting
            analytics.setTotalOrders(orderService.getTotalOrdersCount(startDate, endDate));
            analytics.setTotalRevenue(orderService.getTotalRevenue(startDate, endDate));
            analytics.setMostPopularMusic("Various Artists"); // Convert List to String summary
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

    public static class TicketPriorityUpdateRequest {
        private String priority;

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class TicketAssignmentRequest {
        private String assigneeUsername;

        public String getAssigneeUsername() { return assigneeUsername; }
        public void setAssigneeUsername(String assigneeUsername) { this.assigneeUsername = assigneeUsername; }
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

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class BulkTicketUpdateRequest {
        private Long[] ticketIds;
        private String newStatus;

        public Long[] getTicketIds() { return ticketIds; }
        public void setTicketIds(Long[] ticketIds) { this.ticketIds = ticketIds; }

        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    }

    public static class BulkTicketCloseRequest {
        private Long[] ticketIds;
        private String reason;

        public Long[] getTicketIds() { return ticketIds; }
        public void setTicketIds(Long[] ticketIds) { this.ticketIds = ticketIds; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
