package com.music.musicstore.api;

import com.music.musicstore.services.TicketService;
import com.music.musicstore.models.users.Staff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('STAFF')")
@CrossOrigin(origins = "http://localhost:5173")
public class StaffApiController {

    @Autowired
    private TicketService ticketService;

    // Enhanced ticket management for staff
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTickets(@RequestParam(required = false) String status) {
        try {
            if (status != null) {
                return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
            } else {
                return ResponseEntity.ok(ticketService.getAllTickets());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/urgent")
    public ResponseEntity<?> getUrgentTickets() {
        try {
            return ResponseEntity.ok(ticketService.getUrgentTickets());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch urgent tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/needs-attention")
    public ResponseEntity<?> getTicketsNeedingAttention() {
        try {
            return ResponseEntity.ok(ticketService.getTicketsNeedingAttention());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets needing attention: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/unassigned")
    public ResponseEntity<?> getUnassignedTickets() {
        try {
            return ResponseEntity.ok(ticketService.getUnassignedTickets());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch unassigned tickets: " + e.getMessage()));
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

    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<?> getTicketMessages(@PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(ticketService.getTicketMessages(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket messages: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<?> replyToTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // For now, create a simple staff lookup - this should be implemented properly
            // based on your security configuration
            Staff staff = new Staff(); // This is a placeholder - implement proper staff lookup
            staff.setUsername(userDetails.getUsername());

            var message = ticketService.addStaffReply(ticketId, request.getMessage(), staff);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reply to ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<?> assignTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // For now, create a simple staff lookup - this should be implemented properly
            Staff staff = new Staff(); // This is a placeholder - implement proper staff lookup
            staff.setUsername(userDetails.getUsername());

            var ticket = ticketService.assignTicket(ticketId, staff);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to assign ticket: " + e.getMessage()));
        }
    }

    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestBody TicketStatusUpdateRequest request) {
        try {
            var ticket = ticketService.updateTicketStatus(ticketId, request.getStatus());
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update ticket status: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(@PathVariable Long ticketId) {
        try {
            var ticket = ticketService.closeTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to close ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reopen")
    public ResponseEntity<?> reopenTicket(@PathVariable Long ticketId) {
        try {
            var ticket = ticketService.reopenTicket(ticketId);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reopen ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/stats")
    public ResponseEntity<?> getTicketStatistics() {
        try {
            var stats = ticketService.getStatusDistribution();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<?> searchTickets(@RequestParam String query) {
        try {
            return ResponseEntity.ok(ticketService.searchTickets(query));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to search tickets: " + e.getMessage()));
        }
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

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
