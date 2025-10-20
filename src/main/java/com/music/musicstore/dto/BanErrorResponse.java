package com.music.musicstore.dto;

import java.time.LocalDateTime;

public class BanErrorResponse {
    private String message;
    private String errorType;
    private String banReason;
    private LocalDateTime bannedUntil;
    private LocalDateTime timestamp;

    public BanErrorResponse(String banReason, LocalDateTime bannedUntil) {
        this.message = "Your account has been temporarily banned";
        this.errorType = "ACCOUNT_BANNED";
        this.banReason = banReason;
        this.bannedUntil = bannedUntil;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

