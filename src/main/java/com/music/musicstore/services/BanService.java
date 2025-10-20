package com.music.musicstore.services;

import com.music.musicstore.dto.BanUserResponse;
import com.music.musicstore.dto.BannedUserDto;
import com.music.musicstore.models.users.Artist;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.repositories.CustomerRepository;
import com.music.musicstore.exceptions.ResourceNotFoundException;
import com.music.musicstore.exceptions.BusinessRuleException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BanService {
    private static final Logger logger = LoggerFactory.getLogger(BanService.class);

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private EmailSender emailSender;

    /**
     * Ban a customer temporarily
     */
    @Transactional
    public BanUserResponse banCustomer(Long customerId, String reason, Integer durationInHours, String adminUsername) {
        logger.info("Admin {} attempting to ban customer ID: {} for {} hours", adminUsername, customerId, durationInHours);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (customer.isBanned()) {
            throw new BusinessRuleException("Customer is already banned until " +
                formatDateTime(customer.getBannedUntil()));
        }

        LocalDateTime bannedUntil = LocalDateTime.now().plusHours(durationInHours);

        customer.setBanned(true);
        customer.setBanReason(reason);
        customer.setBannedAt(LocalDateTime.now());
        customer.setBannedUntil(bannedUntil);
        customer.setBannedBy(adminUsername);
        customer.setEnabled(false); // Disable account during ban

        customerRepository.save(customer);

        // Send ban notification email
        sendBanNotificationEmail(customer.getEmail(), customer.getUsername(), reason, bannedUntil);

        logger.info("Customer {} successfully banned by admin {}", customer.getUsername(), adminUsername);

        return createBanResponse(customer.getId(), customer.getUsername(), customer.getEmail(),
                customer.isBanned(), customer.getBanReason(), customer.getBannedAt(),
                customer.getBannedUntil(), customer.getBannedBy(),
                "Customer banned successfully");
    }

    /**
     * Ban an artist temporarily
     */
    @Transactional
    public BanUserResponse banArtist(Long artistId, String reason, Integer durationInHours, String adminUsername) {
        logger.info("Admin {} attempting to ban artist ID: {} for {} hours", adminUsername, artistId, durationInHours);

        Artist artist = artistRepository.findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: " + artistId));

        if (artist.isBanned()) {
            throw new BusinessRuleException("Artist is already banned until " +
                formatDateTime(artist.getBannedUntil()));
        }

        LocalDateTime bannedUntil = LocalDateTime.now().plusHours(durationInHours);

        artist.setBanned(true);
        artist.setBanReason(reason);
        artist.setBannedAt(LocalDateTime.now());
        artist.setBannedUntil(bannedUntil);
        artist.setBannedBy(adminUsername);
        artist.setEnabled(false); // Disable account during ban

        artistRepository.save(artist);

        // Send ban notification email
        sendBanNotificationEmail(artist.getEmail(), artist.getUserName(), reason, bannedUntil);

        logger.info("Artist {} successfully banned by admin {}", artist.getUserName(), adminUsername);

        return createBanResponse(artist.getId(), artist.getUserName(), artist.getEmail(),
                artist.isBanned(), artist.getBanReason(), artist.getBannedAt(),
                artist.getBannedUntil(), artist.getBannedBy(),
                "Artist banned successfully");
    }

    /**
     * Unban a customer
     */
    @Transactional
    public BanUserResponse unbanCustomer(Long customerId, String adminUsername) {
        logger.info("Admin {} attempting to unban customer ID: {}", adminUsername, customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        if (!customer.isBanned()) {
            throw new BusinessRuleException("Customer is not currently banned");
        }

        customer.setBanned(false);
        customer.setBanReason(null);
        customer.setBannedAt(null);
        customer.setBannedUntil(null);
        customer.setBannedBy(null);
        customer.setEnabled(true); // Re-enable account

        customerRepository.save(customer);

        // Send unban notification email
        sendUnbanNotificationEmail(customer.getEmail(), customer.getUsername());

        logger.info("Customer {} successfully unbanned by admin {}", customer.getUsername(), adminUsername);

        return createBanResponse(customer.getId(), customer.getUsername(), customer.getEmail(),
                customer.isBanned(), null, null, null, null,
                "Customer unbanned successfully");
    }

    /**
     * Unban an artist
     */
    @Transactional
    public BanUserResponse unbanArtist(Long artistId, String adminUsername) {
        logger.info("Admin {} attempting to unban artist ID: {}", adminUsername, artistId);

        Artist artist = artistRepository.findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: " + artistId));

        if (!artist.isBanned()) {
            throw new BusinessRuleException("Artist is not currently banned");
        }

        artist.setBanned(false);
        artist.setBanReason(null);
        artist.setBannedAt(null);
        artist.setBannedUntil(null);
        artist.setBannedBy(null);
        artist.setEnabled(true); // Re-enable account

        artistRepository.save(artist);

        // Send unban notification email
        sendUnbanNotificationEmail(artist.getEmail(), artist.getUserName());

        logger.info("Artist {} successfully unbanned by admin {}", artist.getUserName(), adminUsername);

        return createBanResponse(artist.getId(), artist.getUserName(), artist.getEmail(),
                artist.isBanned(), null, null, null, null,
                "Artist unbanned successfully");
    }

    /**
     * Check and auto-unban expired bans
     */
    @Transactional
    public void checkAndAutoUnban() {
        LocalDateTime now = LocalDateTime.now();

        // Auto-unban customers
        customerRepository.findAll().stream()
            .filter(customer -> customer.isBanned() && customer.getBannedUntil() != null)
            .filter(customer -> customer.getBannedUntil().isBefore(now))
            .forEach(customer -> {
                logger.info("Auto-unbanning customer {} (ban expired)", customer.getUsername());
                customer.setBanned(false);
                customer.setBanReason(null);
                customer.setBannedAt(null);
                customer.setBannedUntil(null);
                customer.setBannedBy(null);
                customer.setEnabled(true);
                customerRepository.save(customer);
                sendUnbanNotificationEmail(customer.getEmail(), customer.getUsername());
            });

        // Auto-unban artists
        artistRepository.findAll().stream()
            .filter(artist -> artist.isBanned() && artist.getBannedUntil() != null)
            .filter(artist -> artist.getBannedUntil().isBefore(now))
            .forEach(artist -> {
                logger.info("Auto-unbanning artist {} (ban expired)", artist.getUserName());
                artist.setBanned(false);
                artist.setBanReason(null);
                artist.setBannedAt(null);
                artist.setBannedUntil(null);
                artist.setBannedBy(null);
                artist.setEnabled(true);
                artistRepository.save(artist);
                sendUnbanNotificationEmail(artist.getEmail(), artist.getUserName());
            });
    }

    /**
     * Get ban status for a customer
     */
    public BanUserResponse getCustomerBanStatus(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return createBanResponse(customer.getId(), customer.getUsername(), customer.getEmail(),
                customer.isBanned(), customer.getBanReason(), customer.getBannedAt(),
                customer.getBannedUntil(), customer.getBannedBy(),
                customer.isBanned() ? "Customer is currently banned" : "Customer is not banned");
    }

    /**
     * Get ban status for an artist
     */
    public BanUserResponse getArtistBanStatus(Long artistId) {
        Artist artist = artistRepository.findById(artistId)
            .orElseThrow(() -> new ResourceNotFoundException("Artist not found with id: " + artistId));

        return createBanResponse(artist.getId(), artist.getUserName(), artist.getEmail(),
                artist.isBanned(), artist.getBanReason(), artist.getBannedAt(),
                artist.getBannedUntil(), artist.getBannedBy(),
                artist.isBanned() ? "Artist is currently banned" : "Artist is not banned");
    }

    /**
     * Get all banned users
     */
    public List<BannedUserDto> getAllBannedUsers() {
        logger.info("Fetching all banned users");
        List<BannedUserDto> bannedUsers = new ArrayList<>();

        // Get banned customers
        customerRepository.findAll().stream()
            .filter(Customer::isBanned)
            .forEach(customer -> {
                BannedUserDto dto = new BannedUserDto(
                    customer.getId(),
                    customer.getUsername(),
                    customer.getEmail(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    "CUSTOMER",
                    customer.getBanReason(),
                    customer.getBannedAt(),
                    customer.getBannedUntil(),
                    customer.getBannedBy()
                );
                bannedUsers.add(dto);
            });

        // Get banned artists
        artistRepository.findAll().stream()
            .filter(Artist::isBanned)
            .forEach(artist -> {
                BannedUserDto dto = new BannedUserDto(
                    artist.getId(),
                    artist.getUserName(),
                    artist.getEmail(),
                    artist.getFirstName(),
                    artist.getLastName(),
                    "ARTIST",
                    artist.getBanReason(),
                    artist.getBannedAt(),
                    artist.getBannedUntil(),
                    artist.getBannedBy()
                );
                bannedUsers.add(dto);
            });

        logger.info("Found {} banned users total", bannedUsers.size());
        return bannedUsers;
    }

    /**
     * Get banned users with pagination
     */
    public Page<BannedUserDto> getAllBannedUsersPaginated(Pageable pageable) {
        logger.info("Fetching all banned users with pagination");
        List<BannedUserDto> allBannedUsers = getAllBannedUsers();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allBannedUsers.size());

        List<BannedUserDto> pageContent = allBannedUsers.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allBannedUsers.size());
    }

    /**
     * Get ban statistics
     */
    public Map<String, Object> getBanStatistics() {
        logger.info("Calculating ban statistics");

        long bannedCustomers = customerRepository.findAll().stream()
            .filter(Customer::isBanned)
            .count();

        long bannedArtists = artistRepository.findAll().stream()
            .filter(Artist::isBanned)
            .count();

        long totalBanned = bannedCustomers + bannedArtists;

        // Count expiring soon (within 24 hours)
        LocalDateTime next24Hours = LocalDateTime.now().plusHours(24);
        long expiringSoon = customerRepository.findAll().stream()
            .filter(c -> c.isBanned() && c.getBannedUntil() != null &&
                        c.getBannedUntil().isBefore(next24Hours))
            .count() +
            artistRepository.findAll().stream()
            .filter(a -> a.isBanned() && a.getBannedUntil() != null &&
                        a.getBannedUntil().isBefore(next24Hours))
            .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalBanned", totalBanned);
        stats.put("bannedCustomers", bannedCustomers);
        stats.put("bannedArtists", bannedArtists);
        stats.put("expiringSoon", expiringSoon);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }

    private void sendBanNotificationEmail(String email, String username, String reason, LocalDateTime bannedUntil) {
        String subject = "Account Temporarily Banned - Music Store";
        String body = String.format(
            "Dear %s,\n\n" +
            "We regret to inform you that your account has been temporarily banned.\n\n" +
            "Reason: %s\n\n" +
            "Ban Duration: Until %s\n\n" +
            "During this period, you will not be able to access your account or use our services.\n\n" +
            "If you believe this action was taken in error or have any questions, " +
            "please contact our support team.\n\n" +
            "Best regards,\n" +
            "Music Store Administration Team",
            username, reason, formatDateTime(bannedUntil)
        );

        emailSender.sendReceiptEmail(email, subject, body);
        logger.info("Ban notification email sent to {}", email);
    }

    private void sendUnbanNotificationEmail(String email, String username) {
        String subject = "Account Ban Lifted - Music Store";
        String body = String.format(
            "Dear %s,\n\n" +
            "Good news! Your account ban has been lifted.\n\n" +
            "You can now access your account and use all our services again.\n\n" +
            "We hope to provide you with a better experience moving forward.\n\n" +
            "If you have any questions, please don't hesitate to contact our support team.\n\n" +
            "Best regards,\n" +
            "Music Store Administration Team",
            username
        );

        emailSender.sendReceiptEmail(email, subject, body);
        logger.info("Unban notification email sent to {}", email);
    }

    private BanUserResponse createBanResponse(Long userId, String username, String email,
                                              boolean isBanned, String banReason,
                                              LocalDateTime bannedAt, LocalDateTime bannedUntil,
                                              String bannedBy, String message) {
        return new BanUserResponse(userId, username, email, isBanned, banReason,
                bannedAt, bannedUntil, bannedBy, message);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}

