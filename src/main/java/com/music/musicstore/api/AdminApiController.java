package com.music.musicstore.api;

import com.music.musicstore.dto.UnifiedRegisterRequest;
import com.music.musicstore.dto.UserDto;
import com.music.musicstore.dto.StaffRegistrationRequest;
import com.music.musicstore.dto.StaffRegistrationResponse;
import com.music.musicstore.dto.AdminRegistrationRequest;
import com.music.musicstore.dto.AdminRegistrationResponse;
import com.music.musicstore.dto.ErrorResponse;
import com.music.musicstore.services.*;
import com.music.musicstore.models.users.Staff;
import com.music.musicstore.models.users.Admin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminApiController {

    private static final Logger logger = LoggerFactory.getLogger(AdminApiController.class);

    @Autowired
    private UnifiedUserService unifiedUserService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Autowired
    private EmailSender emailSender;

    @Autowired
    private RefundService refundService;

    @Autowired
    private BanService banService;

    @Autowired
    private SalesReportService salesReportService;

    // User Management
    @PostMapping("/users/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UnifiedRegisterRequest request,
                                      @AuthenticationPrincipal UserDetails currentUser,
                                      HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to create user: {}", currentUser.getUsername(), request.getUsername());
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

            // Log successful user creation
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "CREATE_USER",
                "USER",
                userDto.getId(),
                String.format("Created user: %s with role: %s", request.getUsername(), request.getRole()),
                httpRequest
            );

            logger.info("Admin {} successfully created user: {}", currentUser.getUsername(), request.getUsername());
            return ResponseEntity.ok(userDto);
        } catch (Exception e) {
            // Log failed user creation
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "CREATE_USER",
                "USER",
                null,
                e.getMessage(),
                httpRequest
            );

            logger.error("Admin {} failed to create user: {} - Error: {}", currentUser.getUsername(), request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create user: " + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String role,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting users list - page: {}, size: {}, role: {}",
                    currentUser.getUsername(), page, size, role);
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_USERS",
                "USER",
                null,
                String.format("Viewed users list - page: %d, size: %d, role filter: %s", page, size, role),
                httpRequest
            );

            logger.info("Admin {} successfully retrieved users list", currentUser.getUsername());
            return ResponseEntity.ok(unifiedUserService.getAllUsers(page, size, role));
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve users list - Error: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch users: " + e.getMessage()));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId,
                                       @AuthenticationPrincipal UserDetails currentUser,
                                       HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_USER",
                "USER",
                userId,
                "Viewed user details",
                httpRequest
            );

            return ResponseEntity.ok(unifiedUserService.getUserById(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            UserDto updatedUser = unifiedUserService.updateUser(userId, request);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UPDATE_USER",
                "USER",
                userId,
                "Updated user information",
                httpRequest
            );

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UPDATE_USER",
                "USER",
                userId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update user: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId,
                                      @AuthenticationPrincipal UserDetails currentUser,
                                      HttpServletRequest httpRequest) {
        try {
            unifiedUserService.deleteUser(userId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "DELETE_USER",
                "USER",
                userId,
                "Deleted user account",
                httpRequest
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "DELETE_USER",
                "USER",
                userId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete user: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            unifiedUserService.updateUserStatus(userId, request.isEnabled());
            emailSender.sendUserStatusChangeEmail(
                unifiedUserService.getUserById(userId).getEmail(), currentUser.getUsername(), request.isEnabled()? "Activated" : "Deactivated");


            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UPDATE_USER_STATUS",
                "USER",
                userId,
                String.format("Changed user status to: %s", request.isEnabled() ? "ENABLED" : "DISABLED"),
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("User status updated successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UPDATE_USER_STATUS",
                "USER",
                userId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update user status: " + e.getMessage()));
        }
    }

    // Ban Management
    @PostMapping("/customers/{customerId}/ban")
    public ResponseEntity<?> banCustomer(
            @PathVariable Long customerId,
            @Valid @RequestBody com.music.musicstore.dto.BanUserRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to ban customer ID: {}", currentUser.getUsername(), customerId);
        try {
            com.music.musicstore.dto.BanUserResponse response = banService.banCustomer(
                customerId,
                request.getReason(),
                request.getDurationInHours(),
                currentUser.getUsername()
            );

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "BAN_CUSTOMER",
                "CUSTOMER",
                customerId,
                String.format("Banned customer for %d hours. Reason: %s",
                    request.getDurationInHours(), request.getReason()),
                httpRequest
            );

            logger.info("Admin {} successfully banned customer ID: {}", currentUser.getUsername(), customerId);
            return ResponseEntity.ok(response);
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            logger.warn("Business rule violation when banning customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error banning customer {}: {}", customerId, e.getMessage(), e);
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "BAN_CUSTOMER",
                "CUSTOMER",
                customerId,
                e.getMessage(),
                httpRequest
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to ban customer: " + e.getMessage()));
        }
    }

    @PostMapping("/artists/{artistId}/ban")
    public ResponseEntity<?> banArtist(
            @PathVariable Long artistId,
            @Valid @RequestBody com.music.musicstore.dto.BanUserRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to ban artist ID: {}", currentUser.getUsername(), artistId);
        try {
            com.music.musicstore.dto.BanUserResponse response = banService.banArtist(
                artistId,
                request.getReason(),
                request.getDurationInHours(),
                currentUser.getUsername()
            );

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "BAN_ARTIST",
                "ARTIST",
                artistId,
                String.format("Banned artist for %d hours. Reason: %s",
                    request.getDurationInHours(), request.getReason()),
                httpRequest
            );

            logger.info("Admin {} successfully banned artist ID: {}", currentUser.getUsername(), artistId);
            return ResponseEntity.ok(response);
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            logger.warn("Business rule violation when banning artist {}: {}", artistId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error banning artist {}: {}", artistId, e.getMessage(), e);
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "BAN_ARTIST",
                "ARTIST",
                artistId,
                e.getMessage(),
                httpRequest
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to ban artist: " + e.getMessage()));
        }
    }

    @DeleteMapping("/customers/{customerId}/ban")
    public ResponseEntity<?> unbanCustomer(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to unban customer ID: {}", currentUser.getUsername(), customerId);
        try {
            com.music.musicstore.dto.BanUserResponse response = banService.unbanCustomer(
                customerId,
                currentUser.getUsername()
            );

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UNBAN_CUSTOMER",
                "CUSTOMER",
                customerId,
                "Unbanned customer",
                httpRequest
            );

            logger.info("Admin {} successfully unbanned customer ID: {}", currentUser.getUsername(), customerId);
            return ResponseEntity.ok(response);
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            logger.warn("Business rule violation when unbanning customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error unbanning customer {}: {}", customerId, e.getMessage(), e);
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UNBAN_CUSTOMER",
                "CUSTOMER",
                customerId,
                e.getMessage(),
                httpRequest
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to unban customer: " + e.getMessage()));
        }
    }

    @DeleteMapping("/artists/{artistId}/ban")
    public ResponseEntity<?> unbanArtist(
            @PathVariable Long artistId,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to unban artist ID: {}", currentUser.getUsername(), artistId);
        try {
            com.music.musicstore.dto.BanUserResponse response = banService.unbanArtist(
                artistId,
                currentUser.getUsername()
            );

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UNBAN_ARTIST",
                "ARTIST",
                artistId,
                "Unbanned artist",
                httpRequest
            );

            logger.info("Admin {} successfully unbanned artist ID: {}", currentUser.getUsername(), artistId);
            return ResponseEntity.ok(response);
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            logger.warn("Business rule violation when unbanning artist {}: {}", artistId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error unbanning artist {}: {}", artistId, e.getMessage(), e);
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UNBAN_ARTIST",
                "ARTIST",
                artistId,
                e.getMessage(),
                httpRequest
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to unban artist: " + e.getMessage()));
        }
    }

    @GetMapping("/customers/{customerId}/ban-status")
    public ResponseEntity<?> getCustomerBanStatus(
            @PathVariable Long customerId,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_CUSTOMER_BAN_STATUS",
                "CUSTOMER",
                customerId,
                "Viewed customer ban status",
                httpRequest
            );

            com.music.musicstore.dto.BanUserResponse response = banService.getCustomerBanStatus(customerId);
            return ResponseEntity.ok(response);
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch ban status: " + e.getMessage()));
        }
    }

    @GetMapping("/artists/{artistId}/ban-status")
    public ResponseEntity<?> getArtistBanStatus(
            @PathVariable Long artistId,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_ARTIST_BAN_STATUS",
                "ARTIST",
                artistId,
                "Viewed artist ban status",
                httpRequest
            );

            com.music.musicstore.dto.BanUserResponse response = banService.getArtistBanStatus(artistId);
            return ResponseEntity.ok(response);
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch ban status: " + e.getMessage()));
        }
    }

    // Get All Banned Users Endpoints
    @GetMapping("/banned-users")
    public ResponseEntity<?> getAllBannedUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting all banned users - page: {}, size: {}",
                    currentUser.getUsername(), page, size);
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_BANNED_USERS",
                "USER",
                null,
                String.format("Viewed banned users list - page: %d, size: %d", page, size),
                httpRequest
            );

            org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

            org.springframework.data.domain.Page<com.music.musicstore.dto.BannedUserDto> bannedUsers =
                banService.getAllBannedUsersPaginated(pageable);

            logger.info("Admin {} successfully retrieved banned users list - total: {}",
                        currentUser.getUsername(), bannedUsers.getTotalElements());
            return ResponseEntity.ok(bannedUsers);
        } catch (Exception e) {
            logger.error("Error fetching banned users: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch banned users: " + e.getMessage()));
        }
    }

    @GetMapping("/banned-users/all")
    public ResponseEntity<?> getAllBannedUsersNoPagination(
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting all banned users without pagination", currentUser.getUsername());
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_ALL_BANNED_USERS",
                "USER",
                null,
                "Viewed complete banned users list",
                httpRequest
            );

            java.util.List<com.music.musicstore.dto.BannedUserDto> bannedUsers =
                banService.getAllBannedUsers();

            logger.info("Admin {} successfully retrieved all banned users - total: {}",
                        currentUser.getUsername(), bannedUsers.size());

            return ResponseEntity.ok(Map.of(
                "bannedUsers", bannedUsers,
                "totalCount", bannedUsers.size(),
                "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            logger.error("Error fetching all banned users: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch banned users: " + e.getMessage()));
        }
    }

    @GetMapping("/banned-users/statistics")
    public ResponseEntity<?> getBannedUsersStatistics(
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting banned users statistics", currentUser.getUsername());
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_BAN_STATISTICS",
                "USER",
                null,
                "Viewed ban statistics",
                httpRequest
            );

            Map<String, Object> statistics = banService.getBanStatistics();

            logger.info("Admin {} successfully retrieved ban statistics", currentUser.getUsername());
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error fetching ban statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch ban statistics: " + e.getMessage()));
        }
    }

    // Music Management
    @GetMapping("/music")
    public ResponseEntity<?> getAllMusic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_MUSIC",
                "MUSIC",
                null,
                String.format("Viewed music list - page: %d, size: %d", page, size),
                httpRequest
            );

            return ResponseEntity.ok(musicService.getAllMusicForAdmin(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch music: " + e.getMessage()));
        }
    }

    @DeleteMapping("/music/{musicId}")
    public ResponseEntity<?> deleteMusic(@PathVariable Long musicId,
                                       @AuthenticationPrincipal UserDetails currentUser,
                                       HttpServletRequest httpRequest) {
        try {
            musicService.deleteMusicAsAdmin(musicId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "DELETE_MUSIC",
                "MUSIC",
                musicId,
                "Deleted music track",
                httpRequest
            );

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "DELETE_MUSIC",
                "MUSIC",
                musicId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete music: " + e.getMessage()));
        }
    }

    @PutMapping("/music/{musicId}/status")
    public ResponseEntity<?> updateMusicStatus(
            @PathVariable Long musicId,
            @RequestBody MusicStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            musicService.updateMusicStatus(musicId, request.getStatus());

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UPDATE_MUSIC_STATUS",
                "MUSIC",
                musicId,
                String.format("Changed music status to: %s", request.getStatus()),
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Music status updated successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UPDATE_MUSIC_STATUS",
                "MUSIC",
                musicId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update music status: " + e.getMessage()));
        }
    }

    // NEW: Flagged Content Management
    @GetMapping("/music/flagged")
    public ResponseEntity<?> getAllFlaggedMusic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_FLAGGED_MUSIC",
                "MUSIC",
                null,
                "Viewed flagged music list",
                httpRequest
            );

            return ResponseEntity.ok(musicService.getAllFlaggedMusic(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch flagged music: " + e.getMessage()));
        }
    }

    @PostMapping("/music/{musicId}/unflag")
    public ResponseEntity<?> unflagMusic(@PathVariable Long musicId,
                                       @AuthenticationPrincipal UserDetails currentUser,
                                       HttpServletRequest httpRequest) {
        try {
            musicService.unflagMusic(musicId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UNFLAG_MUSIC",
                "MUSIC",
                musicId,
                "Unflagged music content",
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Music unflagged successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UNFLAG_MUSIC",
                "MUSIC",
                musicId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to unflag music: " + e.getMessage()));
        }
    }

    @DeleteMapping("/music/{musicId}/flagged")
    public ResponseEntity<?> deleteFlaggedMusic(@PathVariable Long musicId,
                                              @AuthenticationPrincipal UserDetails currentUser,
                                              HttpServletRequest httpRequest) {
        try {
            musicService.deleteFlaggedMusic(musicId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "DELETE_FLAGGED_MUSIC",
                "MUSIC",
                musicId,
                "Deleted flagged music content",
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Flagged music deleted successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "DELETE_FLAGGED_MUSIC",
                "MUSIC",
                musicId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete flagged music: " + e.getMessage()));
        }
    }

    // NEW: Review Management
    @GetMapping("/reviews")
    public ResponseEntity<?> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_REVIEWS",
                "REVIEW",
                null,
                String.format("Viewed reviews list - page: %d, size: %d, sortBy: %s", page, size, sortBy),
                httpRequest
            );

            return ResponseEntity.ok(reviewService.getAllReviewsForAdmin(page, size, sortBy));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch reviews: " + e.getMessage()));
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId,
                                        @AuthenticationPrincipal UserDetails currentUser,
                                        HttpServletRequest httpRequest) {
        try {
            reviewService.deleteReviewAsAdmin(reviewId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "DELETE_REVIEW",
                "REVIEW",
                reviewId,
                "Deleted review",
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Review deleted successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "DELETE_REVIEW",
                "REVIEW",
                reviewId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete review: " + e.getMessage()));
        }
    }

    @GetMapping("/reviews/flagged")
    public ResponseEntity<?> getFlaggedReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_FLAGGED_REVIEWS",
                "REVIEW",
                null,
                "Viewed flagged reviews list",
                httpRequest
            );

            return ResponseEntity.ok(reviewService.getFlaggedReviews(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch flagged reviews: " + e.getMessage()));
        }
    }

    // Order Management
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_ORDERS",
                "ORDER",
                null,
                String.format("Viewed orders list - page: %d, size: %d, status: %s", page, size, status),
                httpRequest
            );

            return ResponseEntity.ok(orderService.getAllOrdersForAdmin(page, size, status));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch orders: " + e.getMessage()));
        }
    }

    @PutMapping("/orders/{orderId}/refund")
    public ResponseEntity<?> refundOrder(@PathVariable Long orderId,
                                       @AuthenticationPrincipal UserDetails currentUser,
                                       HttpServletRequest httpRequest) {
        try {
            orderService.refundOrder(orderId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "REFUND_ORDER",
                "ORDER",
                orderId,
                "Processed order refund",
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Order refunded successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "REFUND_ORDER",
                "ORDER",
                orderId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to refund order: " + e.getMessage()));
        }
    }

    // Refund Request Management
    @GetMapping("/refunds")
    public ResponseEntity<?> getAllRefundRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting refund requests - page: {}, size: {}, status: {}",
                    currentUser.getUsername(), page, size, status);
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_REFUND_REQUESTS",
                "REFUND",
                null,
                String.format("Viewed refund requests list - page: %d, size: %d, status filter: %s", page, size, status),
                httpRequest
            );

            org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

            if (status != null) {
                com.music.musicstore.models.order.RefundRequest.RefundStatus refundStatus =
                    com.music.musicstore.models.order.RefundRequest.RefundStatus.valueOf(status.toUpperCase());
                return ResponseEntity.ok(refundService.getRefundRequestsByStatus(refundStatus, pageable));
            }

            return ResponseEntity.ok(refundService.getAllRefundRequests(pageable));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid refund status filter: {}", status);
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Invalid status. Valid values: PENDING, APPROVED, REJECTED"));
        } catch (Exception e) {
            logger.error("Error fetching refund requests: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch refund requests: " + e.getMessage()));
        }
    }

    @GetMapping("/refunds/pending")
    public ResponseEntity<?> getPendingRefundRequests(
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting pending refund requests", currentUser.getUsername());
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_PENDING_REFUNDS",
                "REFUND",
                null,
                "Viewed pending refund requests",
                httpRequest
            );

            java.util.List<com.music.musicstore.models.order.RefundRequest> pendingRequests =
                refundService.getPendingRefundRequests();

            return ResponseEntity.ok(Map.of(
                "refundRequests", pendingRequests,
                "count", pendingRequests.size()
            ));
        } catch (Exception e) {
            logger.error("Error fetching pending refund requests: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch pending refund requests: " + e.getMessage()));
        }
    }

    @GetMapping("/refunds/{refundId}")
    public ResponseEntity<?> getRefundRequestById(
            @PathVariable Long refundId,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_REFUND_REQUEST",
                "REFUND",
                refundId,
                "Viewed refund request details",
                httpRequest
            );

            com.music.musicstore.models.order.RefundRequest refundRequest =
                refundService.getRefundRequestById(refundId);
            return ResponseEntity.ok(refundRequest);
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch refund request: " + e.getMessage()));
        }
    }

    @PostMapping("/refunds/{refundId}/approve")
    public ResponseEntity<?> approveRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody com.music.musicstore.dto.RefundApprovalDto request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} approving refund request {}", currentUser.getUsername(), refundId);
        try {
            com.music.musicstore.models.order.RefundRequest refundRequest =
                refundService.approveRefund(refundId, currentUser.getUsername(), request.getAdminNotes());

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "APPROVE_REFUND",
                "REFUND",
                refundId,
                String.format("Approved refund request for order %d. Amount: %s. Notes: %s",
                    refundRequest.getOrder().getId(),
                    refundRequest.getRefundAmount(),
                    request.getAdminNotes()),
                httpRequest
            );

            logger.info("Admin {} successfully approved refund request {}", currentUser.getUsername(), refundId);
            return ResponseEntity.ok(Map.of(
                "message", "Refund approved successfully",
                "refundRequest", refundRequest
            ));
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            logger.warn("Business rule violation when approving refund {}: {}", refundId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error approving refund request {}: {}", refundId, e.getMessage(), e);
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "APPROVE_REFUND",
                "REFUND",
                refundId,
                e.getMessage(),
                httpRequest
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to approve refund: " + e.getMessage()));
        }
    }

    @PostMapping("/refunds/{refundId}/reject")
    public ResponseEntity<?> rejectRefund(
            @PathVariable Long refundId,
            @Valid @RequestBody com.music.musicstore.dto.RefundApprovalDto request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} rejecting refund request {}", currentUser.getUsername(), refundId);
        try {
            com.music.musicstore.models.order.RefundRequest refundRequest =
                refundService.rejectRefund(refundId, currentUser.getUsername(), request.getAdminNotes());

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "REJECT_REFUND",
                "REFUND",
                refundId,
                String.format("Rejected refund request for order %d. Notes: %s",
                    refundRequest.getOrder().getId(),
                    request.getAdminNotes()),
                httpRequest
            );

            logger.info("Admin {} successfully rejected refund request {}", currentUser.getUsername(), refundId);
            return ResponseEntity.ok(Map.of(
                "message", "Refund rejected successfully",
                "refundRequest", refundRequest
            ));
        } catch (com.music.musicstore.exceptions.BusinessRuleException e) {
            logger.warn("Business rule violation when rejecting refund {}: {}", refundId, e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(e.getMessage()));
        } catch (com.music.musicstore.exceptions.ResourceNotFoundException e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rejecting refund request {}: {}", refundId, e.getMessage(), e);
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "REJECT_REFUND",
                "REFUND",
                refundId,
                e.getMessage(),
                httpRequest
            );
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to reject refund: " + e.getMessage()));
        }
    }

    @GetMapping("/refunds/statistics")
    public ResponseEntity<?> getRefundStatistics(
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting refund statistics", currentUser.getUsername());
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_REFUND_STATISTICS",
                "REFUND",
                null,
                "Viewed refund statistics",
                httpRequest
            );

            long pendingCount = refundService.getPendingRefundCount();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("pendingRefunds", pendingCount);
            statistics.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error fetching refund statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch refund statistics: " + e.getMessage()));
        }
    }

    // ENHANCED: Analytics and Reports
    @GetMapping("/analytics/overview")
    public ResponseEntity<?> getSystemOverview(@AuthenticationPrincipal UserDetails currentUser,
                                             HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting system overview", currentUser.getUsername());
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_ANALYTICS_OVERVIEW",
                "ANALYTICS",
                null,
                "Viewed system overview analytics",
                httpRequest
            );

            Map<String, Object> overview = new HashMap<>();
            overview.put("totalUsers", unifiedUserService.getTotalUsersCount());
            overview.put("totalMusic", musicService.getTotalMusicCount());
            overview.put("totalOrders", orderService.getTotalOrdersCount());
            overview.put("totalRevenue", orderService.getTotalRevenue());
            overview.put("activeTickets", ticketService.countTicketsByStatus("OPEN") +
                                        ticketService.countTicketsByStatus("IN_PROGRESS") +
                                        ticketService.countTicketsByStatus("URGENT"));
            overview.put("totalReviews", reviewService.getTotalReviewsCount());
            overview.put("flaggedMusic", musicService.getFlaggedMusicCount());
            overview.put("averageRating", musicService.getAverageRatingAcrossAllMusic());
            overview.put("todayRegistrations", unifiedUserService.getTodayRegistrationsCount());
            overview.put("todayOrders", orderService.getTodayOrdersCount());
            overview.put("todayRevenue", orderService.getTodayRevenue());

            logger.info("Admin {} successfully retrieved system overview", currentUser.getUsername());
            return ResponseEntity.ok(overview);
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve system overview - Error: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch system overview: " + e.getMessage()));
        }
    }

    // NEW: Monthly Sales Report PDF Download
    @GetMapping("/reports/sales/monthly")
    public ResponseEntity<?> downloadMonthlySalesReport(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting monthly sales report PDF for {}/{}",
                    currentUser.getUsername(), year, month);
        try {
            // Generate PDF
            byte[] pdfBytes = salesReportService.generateMonthlySalesPdf(year, month);

            // Log the action
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "DOWNLOAD_SALES_REPORT",
                "REPORT",
                null,
                String.format("Downloaded monthly sales report for %d/%d", year, month),
                httpRequest
            );

            // Set headers for PDF download
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                org.springframework.http.ContentDisposition.builder("attachment")
                    .filename(String.format("sales_report_%d_%02d.pdf", year, month))
                    .build()
            );
            headers.setContentLength(pdfBytes.length);

            logger.info("Admin {} successfully generated sales report PDF for {}/{}",
                        currentUser.getUsername(), year, month);

            return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Admin {} failed to generate sales report PDF for {}/{} - Error: {}",
                        currentUser.getUsername(), year, month, e.getMessage(), e);

            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "DOWNLOAD_SALES_REPORT",
                "REPORT",
                null,
                String.format("Failed to generate sales report for %d/%d: %s", year, month, e.getMessage()),
                httpRequest
            );

            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to generate sales report: " + e.getMessage()));
        }
    }

    // NEW: Get Monthly Sales Data (JSON)
    @GetMapping("/reports/sales/monthly/data")
    public ResponseEntity<?> getMonthlySalesData(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting monthly sales data for {}/{}",
                    currentUser.getUsername(), year, month);
        try {
            com.music.musicstore.dto.MonthlySalesReportData reportData =
                salesReportService.generateMonthlySalesData(year, month);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_SALES_DATA",
                "REPORT",
                null,
                String.format("Viewed monthly sales data for %d/%d", year, month),
                httpRequest
            );

            logger.info("Admin {} successfully retrieved sales data for {}/{}",
                        currentUser.getUsername(), year, month);

            return ResponseEntity.ok(reportData);
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve sales data for {}/{} - Error: {}",
                        currentUser.getUsername(), year, month, e.getMessage(), e);

            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to retrieve sales data: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/detailed")
    public ResponseEntity<?> getDetailedAnalytics(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting detailed analytics - startDate: {}, endDate: {}",
                    currentUser.getUsername(), startDate, endDate);
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_DETAILED_ANALYTICS",
                "ANALYTICS",
                null,
                String.format("Viewed detailed analytics - startDate: %s, endDate: %s", startDate, endDate),
                httpRequest
            );

            Map<String, Object> analytics = new HashMap<>();

            // User analytics
            analytics.put("userGrowth", unifiedUserService.getUserGrowthAnalytics(startDate, endDate));
            analytics.put("usersByRole", unifiedUserService.getUserCountByRole());

            // Sales analytics
            analytics.put("salesAnalytics", orderService.getSalesAnalytics(startDate, endDate));
            analytics.put("revenueByPeriod", orderService.getRevenueByPeriod(startDate, endDate));
            analytics.put("topSellingMusic", musicService.getTopSellingMusic(10));

            // Music analytics
            analytics.put("musicByGenre", musicService.getMusicCountByGenre());
            analytics.put("musicByCategory", musicService.getMusicCountByCategory());
            analytics.put("artistPerformance", musicService.getArtistPerformanceAnalytics());

            // Review analytics
            analytics.put("reviewAnalytics", reviewService.getReviewAnalytics(startDate, endDate));
            analytics.put("ratingDistribution", reviewService.getRatingDistribution());

            // Ticket analytics
            analytics.put("ticketAnalytics", ticketService.getStatusDistribution());
            analytics.put("ticketResolutionTime", ticketService.getAverageResolutionTime());

            logger.info("Admin {} successfully retrieved detailed analytics", currentUser.getUsername());
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve detailed analytics - Error: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch detailed analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/performance")
    public ResponseEntity<?> getPerformanceMetrics(@AuthenticationPrincipal UserDetails currentUser,
                                                  HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting performance metrics", currentUser.getUsername());
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_PERFORMANCE_METRICS",
                "ANALYTICS",
                null,
                "Viewed system performance metrics",
                httpRequest
            );

            Map<String, Object> metrics = new HashMap<>();

            // System performance metrics
            Runtime runtime = Runtime.getRuntime();
            metrics.put("memoryUsed", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)); // MB
            metrics.put("memoryTotal", runtime.totalMemory() / (1024 * 1024)); // MB
            metrics.put("memoryFree", runtime.freeMemory() / (1024 * 1024)); // MB
            metrics.put("processors", runtime.availableProcessors());

            // Database metrics
            metrics.put("databaseConnections", "Not implemented"); // Would need connection pool metrics
            metrics.put("activeUsers", unifiedUserService.getActiveUsersCount());
            metrics.put("systemUptime", getSystemUptime());

            logger.info("Admin {} successfully retrieved performance metrics", currentUser.getUsername());
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve performance metrics - Error: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch performance metrics: " + e.getMessage()));
        }
    }

    @GetMapping("/reports/comprehensive")
    public ResponseEntity<?> generateComprehensiveReport(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "pdf") String format,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "GENERATE_COMPREHENSIVE_REPORT",
                "REPORT",
                null,
                String.format("Generated comprehensive report - format: %s, period: %s to %s", format, startDate, endDate),
                httpRequest
            );

            Map<String, Object> report = new HashMap<>();
            report.put("generated_at", LocalDateTime.now());
            report.put("period", Map.of("start", startDate, "end", endDate));
            report.put("format", format);
            report.put("overview", getSystemOverview(currentUser, httpRequest).getBody());
            report.put("detailed_analytics", getDetailedAnalytics(startDate, endDate, currentUser, httpRequest).getBody());
            report.put("performance_metrics", getPerformanceMetrics(currentUser, httpRequest).getBody());

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "GENERATE_COMPREHENSIVE_REPORT",
                "REPORT",
                null,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to generate report: " + e.getMessage()));
        }
    }

    // System Settings
    @PostMapping("/settings/backup")
    public ResponseEntity<?> createSystemBackup(@AuthenticationPrincipal UserDetails currentUser,
                                               HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "CREATE_SYSTEM_BACKUP",
                "SYSTEM",
                null,
                "Initiated system backup",
                httpRequest
            );

            Map<String, Object> backup = new HashMap<>();
            backup.put("initiated_at", LocalDateTime.now());
            backup.put("status", "initiated");
            backup.put("message", "System backup initiated successfully");
            return ResponseEntity.ok(backup);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "CREATE_SYSTEM_BACKUP",
                "SYSTEM",
                null,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to create backup: " + e.getMessage()));
        }
    }

    // NEW: Server Management
    @PostMapping("/system/shutdown")
    public ResponseEntity<?> shutdownServer(
            @RequestParam(defaultValue = "0") int delaySeconds,
            @RequestParam(required = false) String reason,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "SHUTDOWN_SERVER",
                "SYSTEM",
                null,
                String.format("Initiated server shutdown - delay: %d seconds, reason: %s", delaySeconds, reason),
                httpRequest
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Server shutdown initiated");
            response.put("delay_seconds", delaySeconds);
            response.put("reason", reason != null ? reason : "Manual shutdown by admin");
            response.put("shutdown_time", LocalDateTime.now().plusSeconds(delaySeconds));

            // Schedule shutdown
            new Thread(() -> {
                try {
                    Thread.sleep(delaySeconds * 1000L);
                    SpringApplication.exit(applicationContext, () -> 0);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "SHUTDOWN_SERVER",
                "SYSTEM",
                null,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to shutdown server: " + e.getMessage()));
        }
    }

    @GetMapping("/system/status")
    public ResponseEntity<?> getSystemStatus(@AuthenticationPrincipal UserDetails currentUser,
                                           HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_SYSTEM_STATUS",
                "SYSTEM",
                null,
                "Viewed system status",
                httpRequest
            );

            Map<String, Object> status = new HashMap<>();
            status.put("status", "running");
            status.put("uptime", getSystemUptime());
            status.put("timestamp", LocalDateTime.now());
            status.put("version", "1.0.0");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to get system status: " + e.getMessage()));
        }
    }

    // Admin Ticket Management
    @GetMapping("/tickets")
    public ResponseEntity<?> getAllTicketsAdmin(@RequestParam(required = false) String status,
                                               @AuthenticationPrincipal UserDetails currentUser,
                                               HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_TICKETS",
                "TICKET",
                null,
                String.format("Viewed tickets list - status filter: %s", status),
                httpRequest
            );

            if (status != null) {
                return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
            }
            return ResponseEntity.ok(ticketService.getAllTickets());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch tickets: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}")
    public ResponseEntity<?> getTicketDetailsAdmin(@PathVariable Long ticketId,
                                                  @AuthenticationPrincipal UserDetails currentUser,
                                                  HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_TICKET",
                "TICKET",
                ticketId,
                "Viewed ticket details",
                httpRequest
            );

            return ResponseEntity.ok(ticketService.getTicketById(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket: " + e.getMessage()));
        }
    }

    @GetMapping("/tickets/{ticketId}/messages")
    public ResponseEntity<?> getTicketMessagesAdmin(@PathVariable Long ticketId,
                                                   @AuthenticationPrincipal UserDetails currentUser,
                                                   HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_TICKET_MESSAGES",
                "TICKET",
                ticketId,
                "Viewed ticket messages",
                httpRequest
            );

            return ResponseEntity.ok(ticketService.getTicketMessages(ticketId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch ticket messages: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/reply")
    public ResponseEntity<?> replyToTicketAdmin(
            @PathVariable Long ticketId,
            @RequestBody AdminTicketReplyRequest request,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        try {
            Staff staff = staffService.findByUsername(userDetails.getUsername());
            var message = ticketService.addStaffReply(ticketId, request.getMessage(), staff);

            auditLogService.logAdminAction(
                userDetails.getUsername(),
                "REPLY_TO_TICKET",
                "TICKET",
                ticketId,
                "Replied to support ticket",
                httpRequest
            );

            return ResponseEntity.ok(message);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                userDetails.getUsername(),
                "REPLY_TO_TICKET",
                "TICKET",
                ticketId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to reply to ticket: " + e.getMessage()));
        }
    }

    @PutMapping("/tickets/{ticketId}/status")
    public ResponseEntity<?> updateTicketStatusAdmin(
            @PathVariable Long ticketId,
            @RequestBody TicketStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            var ticket = ticketService.updateTicketStatus(ticketId, request.getStatus());

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UPDATE_TICKET_STATUS",
                "TICKET",
                ticketId,
                String.format("Updated ticket status to: %s", request.getStatus()),
                httpRequest
            );

            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UPDATE_TICKET_STATUS",
                "TICKET",
                ticketId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update ticket status: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/assign")
    public ResponseEntity<?> assignTicketAdmin(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest httpRequest) {
        try {
            Staff staff = staffService.findByUsername(userDetails.getUsername());
            var ticket = ticketService.assignTicket(ticketId, staff);

            auditLogService.logAdminAction(
                userDetails.getUsername(),
                "ASSIGN_TICKET",
                "TICKET",
                ticketId,
                "Assigned ticket to self",
                httpRequest
            );

            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                userDetails.getUsername(),
                "ASSIGN_TICKET",
                "TICKET",
                ticketId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to assign ticket: " + e.getMessage()));
        }
    }

    // NEW: Admin can assign any staff member to a ticket by staffId
    @PutMapping("/tickets/{ticketId}/assign/{staffId}")
    public ResponseEntity<?> assignTicketToStaffAdmin(
            @PathVariable Long ticketId,
            @PathVariable Long staffId,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            // Find the staff by ID (will throw if not found)
            Staff staff = staffService.findById(staffId);

            // Assign the ticket to the specified staff member
            var ticket = ticketService.assignTicket(ticketId, staff);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "ASSIGN_TICKET_TO_STAFF",
                "TICKET",
                ticketId,
                String.format("Assigned ticket to staff id=%d (username=%s)", staffId, staff.getUsername()),
                httpRequest
            );

            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser != null ? currentUser.getUsername() : "unknown",
                "ASSIGN_TICKET_TO_STAFF",
                "TICKET",
                ticketId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to assign ticket to staff: " + e.getMessage()));
        }
    }

    @DeleteMapping("/tickets/{ticketId}")
    public ResponseEntity<?> deleteTicket(@PathVariable Long ticketId,
                                        @AuthenticationPrincipal UserDetails currentUser,
                                        HttpServletRequest httpRequest) {
        try {
            ticketService.deleteTicket(ticketId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "DELETE_TICKET",
                "TICKET",
                ticketId,
                "Deleted support ticket",
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Ticket deleted successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "DELETE_TICKET",
                "TICKET",
                ticketId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete ticket: " + e.getMessage()));
        }
    }

    @PostMapping("/tickets/{ticketId}/close")
    public ResponseEntity<?> closeTicketAdmin(@PathVariable Long ticketId,
                                            @AuthenticationPrincipal UserDetails currentUser,
                                            HttpServletRequest httpRequest) {
        try {
            var ticket = ticketService.closeTicket(ticketId);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "CLOSE_TICKET",
                "TICKET",
                ticketId,
                "Closed support ticket",
                httpRequest
            );

            return ResponseEntity.ok(ticket);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "CLOSE_TICKET",
                "TICKET",
                ticketId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to close ticket: " + e.getMessage()));
        }
    }

    // NEW: Add audit log viewing endpoints for admins
    @GetMapping("/audit-logs")
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String adminUsername,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_AUDIT_LOGS",
                "AUDIT",
                null,
                String.format("Viewed audit logs - page: %d, filters: admin=%s, action=%s, resource=%s",
                             page, adminUsername, action, resourceType),
                httpRequest
            );

            // Implement filtering logic based on parameters
            org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size);

            if (adminUsername != null) {
                return ResponseEntity.ok(auditLogService.getAuditLogsByAdmin(adminUsername, pageable));
            } else {
                return ResponseEntity.ok(auditLogService.getAuditLogs(pageable));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch audit logs: " + e.getMessage()));
        }
    }

    // NEW: Dedicated Staff Registration Endpoint
    @PostMapping("/staff/register")
    public ResponseEntity<?> registerStaff(@Valid @RequestBody StaffRegistrationRequest request,
                                          @AuthenticationPrincipal UserDetails currentUser,
                                          HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to register new staff: {}", currentUser.getUsername(), request.getUsername());
        try {
            // Create staff directly using StaffService
            Staff staff = new Staff();
            staff.setUsername(request.getUsername());
            staff.setPassword(request.getPassword()); // Will be encoded in service
            staff.setEmail(request.getEmail());
            staff.setFirstName(request.getFirstName());
            staff.setLastName(request.getLastName());
            staff.setPosition(request.getPosition());

            Staff savedStaff = staffService.createStaff(staff);

            // Create detailed response
            StaffRegistrationResponse response = new StaffRegistrationResponse();
            response.setId(savedStaff.getId());
            response.setUsername(savedStaff.getUsername());
            response.setEmail(savedStaff.getEmail());
            response.setFirstName(savedStaff.getFirstName());
            response.setLastName(savedStaff.getLastName());
            response.setRole(savedStaff.getRole());
            response.setEnabled(savedStaff.isEnabled());
            response.setCreatedAt(savedStaff.getCreatedAt());
            response.setPosition(savedStaff.getPosition());

            // Log successful staff registration
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "REGISTER_STAFF",
                "STAFF",
                savedStaff.getId(),
                String.format("Registered new staff member: %s (%s %s) - Position: %s",
                            request.getUsername(), request.getFirstName(), request.getLastName(),
                            request.getPosition()),
                httpRequest
            );

            logger.info("Admin {} successfully registered staff: {}", currentUser.getUsername(), request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log failed staff registration
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "REGISTER_STAFF",
                "STAFF",
                null,
                String.format("Failed to register staff %s: %s", request.getUsername(), e.getMessage()),
                httpRequest
            );

            logger.error("Admin {} failed to register staff: {} - Error: {}",
                        currentUser.getUsername(), request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to register staff member: " + e.getMessage()));
        }
    }

    // NEW: Dedicated Admin Registration Endpoint
    @PostMapping("/admin/register")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request,
                                          @AuthenticationPrincipal UserDetails currentUser,
                                          HttpServletRequest httpRequest) {
        logger.info("Admin {} attempting to register new admin: {}", currentUser.getUsername(), request.getUsername());
        try {
            // Create admin directly using AdminService
            Admin admin = new Admin();
            admin.setUsername(request.getUsername());
            admin.setPassword(request.getPassword()); // Will be encoded in service
            admin.setEmail(request.getEmail());
            admin.setFirstName(request.getFirstName());
            admin.setLastName(request.getLastName());

            Admin savedAdmin = adminService.createAdmin(admin);

            // Create detailed response
            AdminRegistrationResponse response = new AdminRegistrationResponse();
            response.setId(savedAdmin.getId());
            response.setUsername(savedAdmin.getUsername());
            response.setEmail(savedAdmin.getEmail());
            response.setFirstName(savedAdmin.getFirstName());
            response.setLastName(savedAdmin.getLastName());
            response.setRole(savedAdmin.getRole());
            response.setEnabled(savedAdmin.isEnabled());
            response.setCreatedAt(savedAdmin.getCreatedAt());

            // Log successful admin registration
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "REGISTER_ADMIN",
                "ADMIN",
                savedAdmin.getId(),
                String.format("Registered new admin: %s (%s %s)",
                            request.getUsername(), request.getFirstName(), request.getLastName()),
                httpRequest
            );

            logger.info("Admin {} successfully registered admin: {}", currentUser.getUsername(), request.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log failed admin registration
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "REGISTER_ADMIN",
                "ADMIN",
                null,
                String.format("Failed to register admin %s: %s", request.getUsername(), e.getMessage()),
                httpRequest
            );

            logger.error("Admin {} failed to register admin: {} - Error: {}",
                        currentUser.getUsername(), request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to register admin: " + e.getMessage()));
        }
    }

    // NEW: Get All Staff Members
    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting staff list - page: {}, size: {}, department: {}, position: {}",
                    currentUser.getUsername(), page, size, department, position);
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_STAFF_LIST",
                "STAFF",
                null,
                String.format("Viewed staff list - page: %d, size: %d, department filter: %s, position filter: %s",
                            page, size, department, position),
                httpRequest
            );

            // Use the existing getAllUsers method with STAFF role filter
            var staffList = unifiedUserService.getAllUsers(page, size, "STAFF");

            logger.info("Admin {} successfully retrieved staff list", currentUser.getUsername());
            return ResponseEntity.ok(staffList);
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve staff list - Error: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch staff list: " + e.getMessage()));
        }
    }

    // NEW: Get All Admin Users
    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String adminLevel,
            @RequestParam(required = false) String department,
            @AuthenticationPrincipal UserDetails currentUser,
            HttpServletRequest httpRequest) {
        logger.info("Admin {} requesting admin list - page: {}, size: {}, adminLevel: {}, department: {}",
                    currentUser.getUsername(), page, size, adminLevel, department);
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_ADMIN_LIST",
                "ADMIN",
                null,
                String.format("Viewed admin list - page: %d, size: %d, admin level filter: %s, department filter: %s",
                            page, size, adminLevel, department),
                httpRequest
            );

            // Use the existing getAllUsers method with ADMIN role filter
            var adminList = unifiedUserService.getAllUsers(page, size, "ADMIN");

            logger.info("Admin {} successfully retrieved admin list", currentUser.getUsername());
            return ResponseEntity.ok(adminList);
        } catch (Exception e) {
            logger.error("Admin {} failed to retrieve admin list - Error: {}", currentUser.getUsername(), e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch admin list: " + e.getMessage()));
        }
    }

    // NEW: Get Staff Member by ID
    @GetMapping("/staff/{staffId}")
    public ResponseEntity<?> getStaffById(@PathVariable Long staffId,
                                         @AuthenticationPrincipal UserDetails currentUser,
                                         HttpServletRequest httpRequest) {
        try {
            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "VIEW_STAFF_DETAILS",
                "STAFF",
                staffId,
                "Viewed staff member details",
                httpRequest
            );

            Staff staff = staffService.findById(staffId);
            StaffRegistrationResponse response = new StaffRegistrationResponse();
            response.setId(staff.getId());
            response.setUsername(staff.getUsername());
            response.setEmail(staff.getEmail());
            response.setFirstName(staff.getFirstName());
            response.setLastName(staff.getLastName());
            response.setRole(staff.getRole());
            response.setEnabled(staff.isEnabled());
            response.setCreatedAt(staff.getCreatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch staff member: " + e.getMessage()));
        }
    }

    // NEW: Update Staff Member
    @PutMapping("/staff/{staffId}")
    public ResponseEntity<?> updateStaff(@PathVariable Long staffId,
                                        @Valid @RequestBody StaffRegistrationRequest request,
                                        @AuthenticationPrincipal UserDetails currentUser,
                                        HttpServletRequest httpRequest) {
        try {
            Staff staff = staffService.findById(staffId);

            // Update staff information
            staff.setEmail(request.getEmail());
            staff.setFirstName(request.getFirstName());
            staff.setLastName(request.getLastName());

            Staff updatedStaff = staffService.updateStaff(staff);

            // Create response
            StaffRegistrationResponse response = new StaffRegistrationResponse();
            response.setId(updatedStaff.getId());
            response.setUsername(updatedStaff.getUsername());
            response.setEmail(updatedStaff.getEmail());
            response.setFirstName(updatedStaff.getFirstName());
            response.setLastName(updatedStaff.getLastName());
            response.setRole(updatedStaff.getRole());
            response.setEnabled(updatedStaff.isEnabled());
            response.setCreatedAt(updatedStaff.getCreatedAt());
            response.setPosition(request.getPosition());

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UPDATE_STAFF",
                "STAFF",
                staffId,
                String.format("Updated staff member: %s", updatedStaff.getUsername()),
                httpRequest
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UPDATE_STAFF",
                "STAFF",
                staffId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update staff member: " + e.getMessage()));
        }
    }

    // NEW: Enable/Disable Staff Member
    @PutMapping("/staff/{staffId}/status")
    public ResponseEntity<?> updateStaffStatus(@PathVariable Long staffId,
                                              @RequestBody UserStatusUpdateRequest request,
                                              @AuthenticationPrincipal UserDetails currentUser,
                                              HttpServletRequest httpRequest) {
        try {
            Staff staff = staffService.findById(staffId);
            staff.setEnabled(request.isEnabled());
            staffService.updateStaff(staff);

            auditLogService.logAdminAction(
                currentUser.getUsername(),
                "UPDATE_STAFF_STATUS",
                "STAFF",
                staffId,
                String.format("Changed staff status to: %s", request.isEnabled() ? "ENABLED" : "DISABLED"),
                httpRequest
            );

            return ResponseEntity.ok(new SuccessResponse("Staff status updated successfully"));
        } catch (Exception e) {
            auditLogService.logFailedAdminAction(
                currentUser.getUsername(),
                "UPDATE_STAFF_STATUS",
                "STAFF",
                staffId,
                e.getMessage(),
                httpRequest
            );

            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update staff status: " + e.getMessage()));
        }
    }

    // Helper methods
    private String getSystemUptime() {
        long uptimeMillis = System.currentTimeMillis() - getSystemStartTime();
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        return String.format("%d days, %d hours, %d minutes",
                           days, hours % 24, minutes % 60);
    }

    private long getSystemStartTime() {
        // This is a simplified implementation
        // In production, you might store the actual start time
        return System.currentTimeMillis() - (Runtime.getRuntime().totalMemory() / 1024);
    }

    // Inner classes for request/response DTOs
    public static class ErrorResponse {
        private String message;
        private LocalDateTime timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        // getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class SuccessResponse {
        private String message;
        private LocalDateTime timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }

        // getters and setters
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class UserUpdateRequest {
        private String email;
        private String firstName;
        private String lastName;
        // Add other fields as needed

        // getters and setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
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
}
