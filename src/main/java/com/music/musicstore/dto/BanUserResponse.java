package com.music.musicstore.dto;

import java.time.LocalDateTime;

public class BanUserResponse {

    private Long userId;
    private String username;
    private String email;
    private boolean isBanned;
    private String banReason;
    private LocalDateTime bannedAt;
    private LocalDateTime bannedUntil;
    private String bannedBy;
    private String message;

    public BanUserResponse() {}

    public BanUserResponse(Long userId, String username, String email, boolean isBanned,
                          String banReason, LocalDateTime bannedAt, LocalDateTime bannedUntil,
                          String bannedBy, String message) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.isBanned = isBanned;
        this.banReason = banReason;
        this.bannedAt = bannedAt;
        this.bannedUntil = bannedUntil;
        this.bannedBy = bannedBy;
        this.message = message;
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

    public boolean isBanned() {
        return isBanned;
    }

    public void setBanned(boolean banned) {
        isBanned = banned;
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
    }

    public String getBannedBy() {
        return bannedBy;
    }

    public void setBannedBy(String bannedBy) {
        this.bannedBy = bannedBy;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
