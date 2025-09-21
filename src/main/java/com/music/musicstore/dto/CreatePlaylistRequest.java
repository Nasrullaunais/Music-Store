package com.music.musicstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePlaylistRequest {

    @NotBlank(message = "Playlist name is required")
    @Size(max = 100, message = "Playlist name cannot exceed 100 characters")
    private String name;

    // Default constructor
    public CreatePlaylistRequest() {
    }

    public CreatePlaylistRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
