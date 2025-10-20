package com.music.musicstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BanUserRequest {

    @NotBlank(message = "Ban reason is required")
    private String reason;

    @NotNull(message = "Ban duration in hours is required")
    private Integer durationInHours;

    public BanUserRequest() {}

    public BanUserRequest(String reason, Integer durationInHours) {
        this.reason = reason;
        this.durationInHours = durationInHours;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getDurationInHours() {
        return durationInHours;
    }

    public void setDurationInHours(Integer durationInHours) {
        this.durationInHours = durationInHours;
    }
}

