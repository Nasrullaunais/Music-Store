package com.music.musicstore.api;

import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.services.MusicService;
import com.music.musicstore.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/artist")
@PreAuthorize("hasRole('ARTIST')")
@CrossOrigin(origins = "http://localhost:5173")
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
    public ResponseEntity<?> getMyMusic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            if (page == 0 && size == 10) {
                // Return all music if default pagination
                List<Music> musicList = musicService.getMusicByArtist(userDetails.getUsername());
                List<MusicDto> musicDtoList = musicList.stream()
                    .map(this::convertToDto)
                    .toList();
                return ResponseEntity.ok(musicDtoList);
            } else {
                // Return paginated results
                Page<Music> musicPage = musicService.getMusicByArtistPaginated(userDetails.getUsername(), page, size);
                Page<MusicDto> musicDtoPage = musicPage.map(this::convertToDto);
                return ResponseEntity.ok(musicDtoPage);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch music: " + e.getMessage()));
        }
    }

    @GetMapping("/music/count")
    public ResponseEntity<?> getMyMusicCount(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            long count = musicService.countMusicByArtist(userDetails.getUsername());
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Failed to fetch music count: " + e.getMessage()));
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
        dto.setDescription(music.getDescription());
        dto.setPrice(music.getPrice());
        dto.setImageUrl(music.getImageUrl());
        dto.setAudioFilePath(music.getAudioFilePath());
        dto.setCategory(music.getCategory());
        dto.setArtist(music.getArtistUsername() != null ? music.getArtistUsername() : "Unknown Artist");
        dto.setAlbum(music.getAlbumName());
        dto.setGenre(music.getGenre());
        dto.setReleaseYear(music.getReleaseYear());
        dto.setCreatedAt(music.getCreatedAt());
        dto.setAverageRating(music.getAverageRating() != null ? music.getAverageRating().doubleValue() : 0.0);
        dto.setTotalReviews(music.getTotalReviews());
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
