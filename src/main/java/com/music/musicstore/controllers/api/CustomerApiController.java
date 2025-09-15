package com.music.musicstore.controllers.api;

import com.music.musicstore.dto.CreateReviewRequest;
import com.music.musicstore.services.CartService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import com.music.musicstore.services.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('CUSTOMER')")
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
            return ResponseEntity.ok(cartService.addToCart(userDetails.getUsername(), musicId));
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
            return ResponseEntity.ok(musicService.addToPlaylist(
                playlistId, musicId, userDetails.getUsername()
            ));
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
            return ResponseEntity.ok(reviewService.createReview(
                request.getMusicId(), userDetails.getUsername(), request.getRating(), request.getComment()
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
            return ResponseEntity.ok(reviewService.updateReview(
                reviewId, userDetails.getUsername(), request.getRating(), request.getComment()
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
            reviewService.deleteReview(reviewId, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete review: " + e.getMessage()));
        }
    }

    // Support Tickets
    @PostMapping("/tickets")
    public ResponseEntity<?> createTicket(
            @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.createTicket(
                userDetails.getUsername(), request.getSubject(), request.getDescription(), request.getPriority()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets")
    public ResponseEntity<?> getMyTickets(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(ticketService.getTicketsByUsername(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
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

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
