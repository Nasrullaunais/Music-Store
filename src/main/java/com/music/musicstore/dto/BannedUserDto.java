package com.music.musicstore.dto;

import java.time.LocalDateTime;

public class BannedUserDto {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String userType; // "CUSTOMER" or "ARTIST"
    private String banReason;
    private LocalDateTime bannedAt;
    private LocalDateTime bannedUntil;
    private String bannedBy;
    private Long hoursRemaining;

    public BannedUserDto() {}

    public BannedUserDto(Long userId, String username, String email, String firstName,
                        String lastName, String userType, String banReason,
                        LocalDateTime bannedAt, LocalDateTime bannedUntil, String bannedBy) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.banReason = banReason;
        this.bannedAt = bannedAt;
        this.bannedUntil = bannedUntil;
        this.bannedBy = bannedBy;
        this.hoursRemaining = calculateHoursRemaining();
    }

    private Long calculateHoursRemaining() {
        if (bannedUntil == null) return null;
        LocalDateTime now = LocalDateTime.now();
        if (bannedUntil.isBefore(now)) return 0L;
        return java.time.Duration.between(now, bannedUntil).toHours();
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public LocalDateTime getBannedAt() {
        return bannedAt;
    }

    public void setBannedAt(LocalDateTime bannedAt) {
        this.bannedAt = bannedAt;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
        this.hoursRemaining = calculateHoursRemaining();
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public void setBannedBy(String bannedBy) {
        this.bannedBy = bannedBy;
    }

    public Long getHoursRemaining() {
        return hoursRemaining;
    }

    public void setHoursRemaining(Long hoursRemaining) {
        this.hoursRemaining = hoursRemaining;
    }
}

