package com.music.musicstore.api;

import com.music.musicstore.configs.JwtUtil;
import com.music.musicstore.dto.LoginRequest;
import com.music.musicstore.dto.RegisterRequest;
import com.music.musicstore.dto.UnifiedRegisterRequest;
import com.music.musicstore.dto.AuthResponse;
import com.music.musicstore.dto.UserDto;
import com.music.musicstore.dto.BanErrorResponse;
import com.music.musicstore.exceptions.UserBannedException;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.users.Artist;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.services.UnifiedUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UnifiedUserService unifiedUserService;
    private final CustomerRepository customerRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public AuthApiController(AuthenticationManager authenticationManager,
                           @Qualifier("combinedUserDetailsService") UserDetailsService userDetailsService,
                           JwtUtil jwtUtil,
                           UnifiedUserService unifiedUserService,
                           CustomerRepository customerRepository,
                           ArtistRepository artistRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
        this.unifiedUserService = unifiedUserService;
        this.customerRepository = customerRepository;
        this.artistRepository = artistRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        System.out.println("Login request received: " + request.getUsername());
        try {
            // Check if user is banned before attempting authentication
            checkIfUserBanned(request.getUsername());

            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            UserDto userDto = unifiedUserService.getUserInfo(userDetails);

            return ResponseEntity.ok(new AuthResponse(token, userDto));
        } catch (UserBannedException e) {
            // User is banned - provide detailed error message
            return ResponseEntity.status(403)
                .body(new BanErrorResponse(e.getBanReason(), e.getBannedUntil()));
        } catch (DisabledException e) {
            // User is disabled but not banned
            return ResponseEntity.status(403)
                .body(new ErrorResponse("Your account has been disabled. Please contact support."));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid username or password"));
        }
    }

    /**
     * Check if the user is banned before authentication
     */
    private void checkIfUserBanned(String username) {
        // Check if it's a customer
        Optional<Customer> customerOpt = customerRepository.findByUsername(username);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            if (customer.isBanned()) {
                // Check if ban has expired
                if (customer.getBannedUntil() != null && customer.getBannedUntil().isAfter(LocalDateTime.now())) {
                    throw new UserBannedException(username, customer.getBanReason(), customer.getBannedUntil());
                }
            }
            return;
        }

        // Check if it's an artist
        Optional<Artist> artistOpt = artistRepository.findByUserName(username);
        if (artistOpt.isPresent()) {
            Artist artist = artistOpt.get();
            if (artist.isBanned()) {
                // Check if ban has expired
                if (artist.getBannedUntil() != null && artist.getBannedUntil().isAfter(LocalDateTime.now())) {
                    throw new UserBannedException(username, artist.getBanReason(), artist.getBannedUntil());
                }
            }
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UnifiedRegisterRequest request) {
        try {
            // Validate role
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid role. Allowed roles: CUSTOMER, ARTIST"));
            }

            // Only allow CUSTOMER and ARTIST registration through public endpoint
            // STAFF and ADMIN should be created by admins only
            if (!request.getRole().equalsIgnoreCase("CUSTOMER") &&
                !request.getRole().equalsIgnoreCase("ARTIST")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Only CUSTOMER and ARTIST registration is allowed"));
            }

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

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponse(token, userDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (userDetails == null) {
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("Not authenticated"));
            }

            UserDto userDto = unifiedUserService.getUserInfo(userDetails);
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(new ErrorResponse("Failed to get user info"));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid token format"));
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                UserDto userDto = unifiedUserService.getUserInfo(userDetails);
                return ResponseEntity.ok(userDto);
            } else {
                return ResponseEntity.status(401)
                    .body(new ErrorResponse("Invalid or expired token"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new ErrorResponse("Token validation failed"));
        }
    }

    private boolean isValidRole(String role) {
        return role != null && (
            role.equalsIgnoreCase("CUSTOMER") ||
            role.equalsIgnoreCase("ARTIST") ||
            role.equalsIgnoreCase("STAFF") ||
            role.equalsIgnoreCase("ADMIN")
        );
    }

    // Inner class for error responses
    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
