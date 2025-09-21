package com.music.musicstore.api;

import com.music.musicstore.dto.CreateReviewRequest;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.support.Ticket;
import com.music.musicstore.services.CartService;
import com.music.musicstore.services.OrderService;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import com.music.musicstore.services.TicketService;
import com.music.musicstore.services.CustomerService;
import com.music.musicstore.repositories.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Set;

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

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/purchased")
    public ResponseEntity<Set<Music>> getPurchasedMusic(@AuthenticationPrincipal Customer customer) {
        // Fetch customer with purchased music eagerly loaded to avoid LazyInitializationException
        Customer customerWithPurchasedMusic = customerRepository.findByIdWithPurchasedMusic(customer.getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return ResponseEntity.ok(customerWithPurchasedMusic.getPurchasedMusic());
    }

    // Cart Management
    @GetMapping("/cart")
    public ResponseEntity<?> getCart(@AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(cartService.getCartByUsername(customer.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch cart: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/add/{musicId}")
    public ResponseEntity<?> addToCart(
            @PathVariable Long musicId,
            @AuthenticationPrincipal Customer customer) {
        try {
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
            @AuthenticationPrincipal Customer customer) {
        try {
            cartService.removeFromCart(customer.getUsername(), musicId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to remove from cart: " + e.getMessage()));
        }
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<?> checkout(@AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(orderService.checkout(customer.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to checkout: " + e.getMessage()));
        }
    }

    // Purchase Management
    @PostMapping("/purchase/{musicId}")
    public ResponseEntity<?> purchaseMusic(
            @PathVariable Long musicId,
            @AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(orderService.purchaseMusic(customer.getUsername(), musicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to purchase music: " + e.getMessage()));
        }
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<?> getOrderHistory(@AuthenticationPrincipal Customer customer,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(orderService.getOrdersByUsername(customer.getUsername(), page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch order history: " + e.getMessage()));
        }
    }

    // Review Management
    @PostMapping("/review")
    public ResponseEntity<?> submitReview(
            @Valid @RequestBody CreateReviewRequest request,
            @AuthenticationPrincipal Customer customer) {
        try {
            return ResponseEntity.ok(reviewService.createReview(
                request.getMusicId(),
                customer,
                request.getRating(),
                request.getComment()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to submit review: " + e.getMessage()));
        }
    }

    // Support Ticket Management
    @PostMapping("/support/ticket")
    public ResponseEntity<?> createTicket(
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal Customer customer) {
        try {
            String subject = payload.get("subject");
            String description = payload.get("description");
            Ticket ticket = ticketService.createTicket(customer, subject, description);
            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/support/tickets")
    public ResponseEntity<?> getTickets(@AuthenticationPrincipal Customer customer,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(ticketService.getTicketsByUsername(customer.getUsername(), page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    // Profile Management
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Customer customer) {
        return ResponseEntity.ok(customer);
    }

    // Define SuccessResponse and ErrorResponse classes for consistent JSON responses
    private static class SuccessResponse {
        public String message;
        public SuccessResponse(String message) { this.message = message; }
    }

    private static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }
}
