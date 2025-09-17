package com.music.musicstore.api;

import com.music.musicstore.dto.MusicDto;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.services.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/music")
@CrossOrigin(origins = "http://localhost:3000")
public class MusicApiController {

    private final MusicService musicService;

    @Autowired
    public MusicApiController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public ResponseEntity<Page<MusicDto>> getAllMusic(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
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

    @GetMapping("/{id}")
    public ResponseEntity<MusicDto> getMusicById(@PathVariable Long id) {
        Optional<Music> music = musicService.getMusicById(id);
        if (music.isPresent()) {
            return ResponseEntity.ok(convertToDto(music.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
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
            @RequestParam("album") String album,
            @RequestParam("releaseYear") String releaseYear) {

        try {
            // Create a new Music entity
            Music music = new Music();
            music.setName(name);
            music.setDescription(description);
            music.setPrice(new BigDecimal(price));
            music.setCategory(genre);
            music.setAlbum(album);
            music.setGenre(genre);
            music.setReleaseYear(Integer.parseInt(releaseYear));

            // Save the music and return it
            return ResponseEntity.ok(convertToDto(musicService.saveMusic(music)));
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
                music.getArtist() != null ? music.getArtist().getUsername() : "Unknown Artist",
                music.getAlbum(),
                music.getGenre(),
                music.getReleaseYear(),
                music.getCreatedAt()
        );
    }
}
