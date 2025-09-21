package com.music.musicstore.api;

import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.services.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = "http://localhost:5173")
public class MusicApiController {

    private final MusicService musicService;

    @Autowired
    public MusicApiController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public ResponseEntity<Page<MusicDto>> getAllMusic(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "50", required = false) int size,
            @RequestParam(defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(defaultValue = "desc", required = false) String sortDir,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String search) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Music> musicPage = musicService.getAllMusicPaginated(page, size);

        Page<MusicDto> musicDtoPage = musicPage.map(this::convertToDto);

        return ResponseEntity.ok(musicDtoPage);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MusicDto>> searchMusic(@RequestParam String query){
        return ResponseEntity.ok(
            musicService.searchMusic(query, 0, 50)
                .map(this::convertToDto)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MusicDto> getMusicById(@PathVariable Long id) {
        Optional<Music> music = musicService.getMusicById(id);
        return music.map(value -> ResponseEntity.ok(convertToDto(value))).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getAllGenres() {
        // Return hardcoded genres for now - can be made dynamic later
        List<String> genres = List.of("Pop", "Rock", "Jazz", "Classical", "Electronic", "Hip-Hop", "Country", "Blues");
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/artists")
    public ResponseEntity<List<String>> getAllArtists() {
        // Return hardcoded artists for now - can be made dynamic later
        List<String> artists = List.of("Artist A", "Artist B", "Artist C", "Sample Artist");
        return ResponseEntity.ok(artists);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<MusicDto>> getFeaturedMusic() {
        // Get first 8 music tracks as featured
        Page<Music> featuredMusic = musicService.getAllMusicPaginated(0, 8);
        List<MusicDto> featuredMusicDto = featuredMusic.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(featuredMusicDto);
    }

    @PostMapping("/upload")
    public ResponseEntity<MusicDto> uploadMusic(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") String price,
            @RequestParam("genre") String genre,
            @RequestParam("artist") String artist,
            @RequestParam(value = "albumName", required = false) String albumName,
            @RequestParam("releaseYear") String releaseYear) {

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Create upload directory if it doesn't exist
            String uploadDir = "src/main/resources/static/uploads/music/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create a new Music entity
            Music music = new Music();
            music.setName(name);
            music.setDescription(description);
            music.setPrice(new BigDecimal(price));
            music.setCategory(genre);
            music.setGenre(genre);
            music.setArtistUsername(artist);
            music.setAlbumName(albumName);
            music.setReleaseYear(Integer.parseInt(releaseYear));
            music.setAudioFilePath("/uploads/music/" + uniqueFilename);
            music.setOriginalFileName(originalFilename);

            // Save the music and return it
            return ResponseEntity.ok(convertToDto(musicService.saveMusic(music)));
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/by-artist/{artistUsername}")
    public ResponseEntity<Page<MusicDto>> getMusicByArtist(
            @PathVariable String artistUsername,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Page<Music> musicPage = musicService.getMusicByArtistPaginated(artistUsername, page, size);
            Page<MusicDto> musicDtoPage = musicPage.map(this::convertToDto);
            return ResponseEntity.ok(musicDtoPage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/artists/{artistUsername}/count")
    public ResponseEntity<Map<String, Object>> getArtistMusicCount(@PathVariable String artistUsername) {
        try {
            long count = musicService.countMusicByArtist(artistUsername);
            return ResponseEntity.ok(Map.of("count", count, "artist", artistUsername));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private MusicDto convertToDto(Music music) {
        return new MusicDto(
                music.getId(),
                music.getName(),
                music.getDescription(),
                music.getPrice(),
                music.getImageUrl(),
                music.getAudioFilePath(),
                music.getCategory(),
                music.getArtistUsername() != null ? music.getArtistUsername() : "Unknown Artist",
                music.getAlbumName() != null ? music.getAlbumName() : "Unknown Album",
                music.getGenre(),
                music.getReleaseYear(),
                music.getCreatedAt(),
                music.getAverageRating() != null ? music.getAverageRating().doubleValue() : 0.0,
                music.getTotalReviews()
        );
    }
}
