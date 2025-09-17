package com.music.musicstore.api;

import com.music.musicstore.dto.UnifiedRegisterRequest;
import com.music.musicstore.dto.UserDto;
import com.music.musicstore.services.UnifiedUserService;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    @Autowired
    private UnifiedUserService unifiedUserService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TicketService ticketService;

    // User Management
    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UnifiedRegisterRequest request) {
        try {
            UserDto userDto = unifiedUserService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole(),
                request.getFirstName(),
                request.getLastName(),
                request.getArtistName(),
                request.getCover()
            );
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create user: " + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role) {
        try {
            return ResponseEntity.ok(unifiedUserService.getAllUsers(page, size, role));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(unifiedUserService.getUserById(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request) {
        try {
            return ResponseEntity.ok(unifiedUserService.updateUser(userId, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            unifiedUserService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequest request) {
        try {
            unifiedUserService.updateUserStatus(userId, request.isEnabled());
            return ResponseEntity.ok(new SuccessResponse("User status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update user status: " + e.getMessage()));
        }
    }

    // Music Management
    @GetMapping("/music")
    public ResponseEntity<?> getAllMusic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(musicService.getAllMusicForAdmin(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch music: " + e.getMessage()));
        }
    }

    @DeleteMapping("/music/{musicId}")
    public ResponseEntity<?> deleteMusic(@PathVariable Long musicId) {
        try {
            musicService.deleteMusicAsAdmin(musicId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete music: " + e.getMessage()));
        }
    }

    @PutMapping("/music/{musicId}/status")
    public ResponseEntity<?> updateMusicStatus(
            @PathVariable Long musicId,
            @RequestBody MusicStatusUpdateRequest request) {
        try {
            musicService.updateMusicStatus(musicId, request.getStatus());
            return ResponseEntity.ok(new SuccessResponse("Music status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update music status: " + e.getMessage()));
        }
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        try {
            return ResponseEntity.ok(orderService.getAllOrdersForAdmin(page, size, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }

    @PutMapping("/orders/{orderId}/refund")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId) {
        try {
            orderService.refundOrder(orderId);
            return ResponseEntity.ok(new SuccessResponse("Order refunded successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to refund order: " + e.getMessage()));
        }
    }

    // Analytics and Reports
    @GetMapping("/analytics/overview")
    public ResponseEntity<?> getSystemOverview() {
        try {
            SystemOverview overview = new SystemOverview();
            overview.setTotalUsers(unifiedUserService.getTotalUsersCount());
            overview.setTotalMusic(musicService.getTotalMusicCount());
            overview.setTotalOrders(orderService.getTotalOrdersCount());
            overview.setTotalRevenue(orderService.getTotalRevenue());
            overview.setActiveTickets(ticketService.getActiveTicketsCount());

            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch system overview: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/detailed")
    public ResponseEntity<?> getDetailedAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            DetailedAnalytics analytics = new DetailedAnalytics();
            analytics.setUserGrowth(unifiedUserService.getUserGrowthAnalytics(startDate, endDate));
            analytics.setSalesAnalytics(orderService.getSalesAnalytics(startDate, endDate));
            analytics.setMusicAnalytics(musicService.getMusicAnalytics(startDate, endDate));
            analytics.setTicketAnalytics(ticketService.getTicketAnalytics(startDate, endDate));

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch detailed analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/reports/comprehensive")
    public ResponseEntity<?> generateComprehensiveReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            // This would generate a comprehensive system report
            return ResponseEntity.ok("Comprehensive report generation initiated");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to generate report: " + e.getMessage()));
        }
    }

    // System Settings
    @PostMapping("/settings/backup")
    public ResponseEntity<?> createSystemBackup() {
        try {
            // This would initiate a system backup
            return ResponseEntity.ok("System backup initiated");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create backup: " + e.getMessage()));
        }
    }

    // Admin Ticket Management - Full control over all tickets
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTicketsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority) {
        try {
            if (priority != null) {
                return ResponseEntity.ok(ticketService.getTicketsByPriority(priority));
            }
            return ResponseEntity.ok(ticketService.getAllTickets(page, size, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketDetailsAdmin(@PathVariable Long ticketId) {
        try {
            return ResponseEntity.ok(ticketService.getTicketById(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<?> replyToTicketAdmin(
            @PathVariable Long ticketId,
            @RequestBody AdminTicketReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.replyToTicket(ticketId, request.getMessage(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Admin reply sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reply to ticket: " + e.getMessage()));
        }
    }

    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatusAdmin(
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

    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId) {
        try {
            // Admin can force close/delete tickets
            ticketService.closeTicket(ticketId, "Administrative closure");
            return ResponseEntity.ok(new SuccessResponse("Ticket deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/bulk/status")
    public ResponseEntity<?> bulkUpdateTicketsAdmin(
            @RequestBody BulkTicketUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.bulkUpdateStatus(request.getTicketIds(), request.getNewStatus(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Tickets updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update tickets: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/bulk/delete")
    public ResponseEntity<?> bulkDeleteTickets(
            @RequestBody BulkTicketDeleteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            ticketService.bulkCloseTickets(request.getTicketIds(), "Administrative bulk closure");
            return ResponseEntity.ok(new SuccessResponse("Tickets deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<?> searchTicketsAdmin(
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

    @GetMapping("/tickets/analytics")
    public ResponseEntity<?> getTicketAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        try {
            return ResponseEntity.ok(ticketService.getTicketAnalytics(startDate, endDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/stats/detailed")
    public ResponseEntity<?> getDetailedTicketStats() {
        try {
            Map<String, Object> detailedStats = ticketService.getDetailedTicketStats();
            return ResponseEntity.ok(detailedStats);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch detailed ticket statistics: " + e.getMessage()));
        }
    }

    // DTOs for requests and responses
    public static class UserUpdateRequest {
        private String email;
        private String firstName;
        private String lastName;
        private String artistName;
        private String cover;

        // Getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getArtistName() { return artistName; }
        public void setArtistName(String artistName) { this.artistName = artistName; }

        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }
    }

    public static class UserStatusUpdateRequest {
        private boolean enabled;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    public static class MusicStatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class SystemOverview {
        private long totalUsers;
        private long totalMusic;
        private long totalOrders;
        private double totalRevenue;
        private long activeTickets;

        // Getters and setters
        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getTotalMusic() { return totalMusic; }
        public void setTotalMusic(long totalMusic) { this.totalMusic = totalMusic; }

        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

        public double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

        public long getActiveTickets() { return activeTickets; }
        public void setActiveTickets(long activeTickets) { this.activeTickets = activeTickets; }
    }

    public static class DetailedAnalytics {
        private Object userGrowth;
        private Object salesAnalytics;
        private Object musicAnalytics;
        private Object ticketAnalytics;

        // Getters and setters
        public Object getUserGrowth() { return userGrowth; }
        public void setUserGrowth(Object userGrowth) { this.userGrowth = userGrowth; }

        public Object getSalesAnalytics() { return salesAnalytics; }
        public void setSalesAnalytics(Object salesAnalytics) { this.salesAnalytics = salesAnalytics; }

        public Object getMusicAnalytics() { return musicAnalytics; }
        public void setMusicAnalytics(Object musicAnalytics) { this.musicAnalytics = musicAnalytics; }

        public Object getTicketAnalytics() { return ticketAnalytics; }
        public void setTicketAnalytics(Object ticketAnalytics) { this.ticketAnalytics = ticketAnalytics; }
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

    public static class AdminTicketReplyRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class TicketStatusUpdateRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class BulkTicketUpdateRequest {
        private List<Long> ticketIds;
        private String newStatus;

        public List<Long> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<Long> ticketIds) { this.ticketIds = ticketIds; }

        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    }

    public static class BulkTicketDeleteRequest {
        private List<Long> ticketIds;

        public List<Long> getTicketIds() { return ticketIds; }
        public void setTicketIds(List<Long> ticketIds) { this.ticketIds = ticketIds; }
    }
}
