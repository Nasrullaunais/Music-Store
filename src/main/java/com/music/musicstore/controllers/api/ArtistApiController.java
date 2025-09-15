package com.music.musicstore.controllers.api;

import com.music.musicstore.dto.MusicDto;
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
            @RequestParam("title") String title,
            @RequestParam("genre") String genre,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam("musicFile") MultipartFile musicFile,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            MusicDto musicDto = musicService.uploadMusic(
                title, genre, price, description, musicFile, coverImage, userDetails.getUsername()
            );
            return ResponseEntity.ok(musicDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to upload music: " + e.getMessage()));
        }
    }

    @GetMapping("/music/my-music")
    public ResponseEntity<?> getMyMusic(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            List<MusicDto> musicList = musicService.getMusicByArtist(userDetails.getUsername());
            return ResponseEntity.ok(musicList);
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
            MusicDto updatedMusic = musicService.updateMusic(musicId, musicDto, userDetails.getUsername());
            return ResponseEntity.ok(updatedMusic);
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

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
