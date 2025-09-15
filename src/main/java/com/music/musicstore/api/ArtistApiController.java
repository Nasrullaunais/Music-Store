package com.music.musicstore.api;

import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/artist")
@CrossOrigin(origins = "http://localhost:3000")
@PreAuthorize("hasRole('ARTIST')")
public class ArtistApiController {

    @Autowired
    private MusicService musicService;

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/music/upload")
    public ResponseEntity<?> uploadMusic(
            @RequestParam String title,
            @RequestParam String genre,
            @RequestParam Double price,
            @RequestParam String description,
            @RequestParam MultipartFile musicFile,
            @RequestParam MultipartFile coverImage,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Music music = musicService.uploadMusic(
                title, genre, price, description, musicFile, coverImage, userDetails.getUsername()
            );
            MusicDto musicDto = convertToDto(music);
            return ResponseEntity.ok(musicDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to upload music: " + e.getMessage()));
        }
    }

    @GetMapping("/music/my-music")
    public ResponseEntity<?> getMyMusic(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<Music> musicList = musicService.getMusicByArtist(userDetails.getUsername());
            List<MusicDto> musicDtoList = musicList.stream()
                .map(this::convertToDto)
                .toList();
            return ResponseEntity.ok(musicDtoList);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch music: " + e.getMessage()));
        }
    }

    @PutMapping("/music/{musicId}")
    public ResponseEntity<?> updateMusic(
            @PathVariable Long musicId,
            @RequestBody MusicDto musicDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Music updatedMusic = musicService.updateMusic(musicId, musicDto, userDetails.getUsername());
            MusicDto updatedMusicDto = convertToDto(updatedMusic);
            return ResponseEntity.ok(updatedMusicDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to update music: " + e.getMessage()));
        }
    }

    @DeleteMapping("/music/{musicId}")
    public ResponseEntity<?> deleteMusic(
            @PathVariable Long musicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            musicService.deleteMusic(musicId, userDetails.getUsername());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to delete music: " + e.getMessage()));
        }
    }

    @GetMapping("/music/{musicId}/reviews")
    public ResponseEntity<?> getMusicReviews(@PathVariable Long musicId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewsByMusicId(musicId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch reviews: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/sales")
    public ResponseEntity<?> getSalesAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // This would return sales analytics for the artist's music
            return ResponseEntity.ok(musicService.getArtistSalesAnalytics(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch analytics: " + e.getMessage()));
        }
    }

    @GetMapping("/analytics/reviews")
    public ResponseEntity<?> getReviewsAnalytics(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            return ResponseEntity.ok(reviewService.getArtistReviewsAnalytics(userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch review analytics: " + e.getMessage()));
        }
    }

    // Helper method to convert Music entity to MusicDto
    private MusicDto convertToDto(Music music) {
        MusicDto dto = new MusicDto();
        dto.setId(music.getId());
        dto.setName(music.getName());
        dto.setGenre(music.getGenre());
        dto.setPrice(music.getPrice());
        // Use the helper method to get artist name
        if (music.getArtist() != null) {
            dto.setArtist(music.getArtist().getUserName());
        } else {
            dto.setArtist(music.getCategory()); // Fallback
        }
        return dto;
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
