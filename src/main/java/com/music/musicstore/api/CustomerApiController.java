package com.music.musicstore.api;

import com.music.musicstore.dto.CreateReviewRequest;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.services.CartService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import com.music.musicstore.services.TicketService;
import com.music.musicstore.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@CrossOrigin(origins = "http://localhost:5173")
public class CustomerApiController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CustomerService customerService;

    // Cart Management
    @GetMapping("/cart")
    public ResponseEntity<?> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(cartService.getCartByUsername(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch cart: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/add/{musicId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Get customer object first
            Customer customer = customerService.findByUsername(userDetails.getUsername());
            cartService.addToCart(customer, musicId);
            return ResponseEntity.ok(new SuccessResponse("Music added to cart successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to add to cart: " + e.getMessage()));
        }
    }

    @DeleteMapping("/cart/remove/{musicId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            cartService.removeFromCart(userDetails.getUsername(), musicId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to remove from cart: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(orderService.checkout(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to checkout: " + e.getMessage()));
        }
    }

    // Purchase Management
    @PostMapping("/purchase/{musicId}")
    public ResponseEntity<?> purchaseMusic(
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(orderService.purchaseMusic(userDetails.getUsername(), musicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to purchase music: " + e.getMessage()));
        }
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByUsername(userDetails.getUsername(), page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(orderService.getOrderDetails(orderId, userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch order details: " + e.getMessage()));
        }
    }

    // Download Management
    @GetMapping("/downloads")
    public ResponseEntity<?> getDownloadableMusic(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(musicService.getDownloadableMusic(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch downloadable music: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{musicId}")
    public ResponseEntity<?> downloadMusic(
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(musicService.downloadMusic(musicId, userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to download music: " + e.getMessage()));
        }
    }

    // Playlist Management
    @GetMapping("/playlists")
    public ResponseEntity<?> getPlaylists(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(musicService.getUserPlaylists(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch playlists: " + e.getMessage()));
        }
    }

    @PostMapping("/playlists")
    public ResponseEntity<?> createPlaylist(
            @RequestBody PlaylistCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(musicService.createPlaylist(
                userDetails.getUsername(), request.getName(), request.getDescription()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create playlist: " + e.getMessage()));
        }
    }

    @PostMapping("/playlists/{playlistId}/add/{musicId}")
    public ResponseEntity<?> addToPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            musicService.addToPlaylist(playlistId, musicId, userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Music added to playlist successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to add to playlist: " + e.getMessage()));
        }
    }

    @DeleteMapping("/playlists/{playlistId}/remove/{musicId}")
    public ResponseEntity<?> removeFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            musicService.removeFromPlaylist(playlistId, musicId, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to remove from playlist: " + e.getMessage()));
        }
    }

    // Review Management
    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Customer customer = customerService.findByUsername(userDetails.getUsername());
            return ResponseEntity.ok(reviewService.createReview(
                request.getMusicId(), customer, request.getRating(), request.getComment()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create review: " + e.getMessage()));
        }
    }

    @GetMapping("/reviews")
    public ResponseEntity<?> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByUsername(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch reviews: " + e.getMessage()));
        }
    }

    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @RequestBody UpdateReviewRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Customer customer = customerService.findByUsername(userDetails.getUsername());
            return ResponseEntity.ok(reviewService.updateReview(
                reviewId, customer, request.getRating(), request.getComment()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update review: " + e.getMessage()));
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Customer customer = customerService.findByUsername(userDetails.getUsername());
            reviewService.deleteReview(reviewId, customer);
            return ResponseEntity.ok(new SuccessResponse("Review deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete review: " + e.getMessage()));
        }
    }

    // Support Tickets - Enhanced with full functionality
    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(
            @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Ticket ticket = ticketService.createTicket(
                userDetails.getUsername(),
                request.getSubject(),
                request.getDescription(),
                request.getPriority()
            );
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/order/{orderId}")
    public ResponseEntity<?> createOrderTicket(
            @PathVariable Long orderId,
            @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // This would need OrderService integration to fetch the order
            Ticket ticket = ticketService.createTicket(
                userDetails.getUsername(),
                request.getSubject(),
                request.getDescription(),
                request.getPriority()
            );
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create order-related ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getMyTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (page == 0 && size == 10) {
                // Return all tickets if default pagination
                return ResponseEntity.ok(ticketService.getTicketsByUsername(userDetails.getUsername()));
            } else {
                // Return paginated results
                return ResponseEntity.ok(ticketService.getTicketsByUsername(userDetails.getUsername(), page, size));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/active")
    public ResponseEntity<?> getActiveTickets(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.getCustomerActiveTickets(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch active tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketDetails(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.getTicketDetails(ticketId, userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket details: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicket(
            @PathVariable Long ticketId,
            @RequestBody(required = false) Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Verify customer owns the ticket
            if (!ticketService.canCustomerViewTicket(ticketId, userDetails.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("You can only close your own tickets"));
            }

            String reason = request != null ? request.get("reason") : null;
            if (reason != null && !reason.trim().isEmpty()) {
                ticketService.closeTicket(ticketId, "Customer closed: " + reason);
            } else {
                ticketService.closeTicket(ticketId);
            }

            return ResponseEntity.ok(new SuccessResponse("Ticket closed successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to close ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/search")
    public ResponseEntity<?> searchTickets(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // For customers, we'd need a customer-specific search method
            return ResponseEntity.ok(ticketService.searchTickets(query, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to search tickets: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<?> replyToTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Verify customer owns the ticket
            if (!ticketService.canCustomerViewTicket(ticketId, userDetails.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("You can only reply to your own tickets"));
            }

            ticketService.replyToTicket(ticketId, request.getMessage(), userDetails.getUsername());
            return ResponseEntity.ok(new SuccessResponse("Reply sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reply to ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}/history")
    public ResponseEntity<?> getTicketHistory(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Verify customer owns the ticket
            if (!ticketService.canCustomerViewTicket(ticketId, userDetails.getUsername())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("You can only view your own ticket history"));
            }

            return ResponseEntity.ok(ticketService.getTicketHistory(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket history: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/status/{status}")
    public ResponseEntity<?> getTicketsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.getCustomerTicketsByStatus(userDetails.getUsername(), status, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets by status: " + e.getMessage()));
        }
    }

    // DTOs for requests
    public static class PlaylistCreateRequest {
        private String name;
        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class UpdateReviewRequest {
        private int rating;
        private String comment;

        public int getRating() { return rating; }
        public void setRating(int rating) { this.rating = rating; }

        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }

    public static class TicketCreateRequest {
        private String subject;
        private String description;
        private String priority;

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }

    public static class TicketReplyRequest {
        private String message;

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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
}
