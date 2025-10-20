package com.music.musicstore.exceptions;

import java.time.LocalDateTime;

public class UserBannedException extends RuntimeException {
    private final String username;
    private final String banReason;
    private final LocalDateTime bannedUntil;

    public UserBannedException(String username, String banReason, LocalDateTime bannedUntil) {
        super(String.format("User '%s' is banned until %s. Reason: %s", username, bannedUntil, banReason));
        this.username = username;
        this.banReason = banReason;
        this.bannedUntil = bannedUntil;
    }

    public String getUsername() {
        return username;
    }

    public String getBanReason() {
        return banReason;
    }

    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }
}

