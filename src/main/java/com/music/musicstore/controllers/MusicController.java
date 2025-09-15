package com.music.musicstore.controllers;

import com.music.musicstore.models.users.Artist;
import com.music.musicstore.models.users.Customer;
import com.music.musicstore.models.music.Music;
import com.music.musicstore.repositories.ArtistRepository;
import com.music.musicstore.repositories.MusicRepository;
import com.music.musicstore.services.FileStorageService;
import com.music.musicstore.services.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;
    private final MusicRepository musicRepository;
    private final FileStorageService fileStorageService;
    private final ArtistRepository artistRepository;
    private static final String UPLOAD_DIR = "src/main/resources/static/musics/";

    @Autowired
    public MusicController(MusicService musicService, ArtistRepository artistRepository, MusicRepository musicRepository, FileStorageService fileStorageService) {
        this.musicService = musicService;
        this.musicRepository = musicRepository;
        this.fileStorageService = fileStorageService;
        this.artistRepository = artistRepository;
    }

    @GetMapping("/{id}")
    public String viewMusic(@PathVariable Long id, Model model){
        Optional<Music> music = musicService.getMusicById(id);
        if(music.isPresent()){
            model.addAttribute("music", music.get());
            if (music.get().getFilePath() != null && !music.get().getFilePath().isEmpty()) {
                model.addAttribute("filePath", music.get().getFilePath());
            } else {
                model.addAttribute("filePath", "default.mp3"); // Default file if not set
            }
            return "music/view"; // Return the view name
        }
        return "music/notfound"; // Return a not found view if music is absent
    }

    @GetMapping("/")
    public String getAllMusic(
            @RequestParam (defaultValue = "0") int page,
            @RequestParam (defaultValue = "10") int size,
            @RequestParam (required = false) String genre,
            @RequestParam (required = false) String artist,
            @RequestParam (required = false) String search,
            @RequestParam (required = false) String category,
            Model model,
            @AuthenticationPrincipal Customer customer
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("name").descending());
        Page<Music> musicPage;
        if (search != null && !search.isEmpty()) {
            musicPage = musicService.searchProducts(search, pageRequest);
            model.addAttribute("search", search);
        } else if (genre != null && !genre.isEmpty()) {
            musicPage = musicService.searchProductsByGenre(genre, pageRequest);
            model.addAttribute("genre", genre);
        } else if (artist != null && !artist.isEmpty()) {
            musicPage = musicService.searchProductsByArtist(artist, pageRequest);
            model.addAttribute("artist", artist);
        } else if (category != null && !category.isEmpty()) {
            musicPage = musicService.searchProductsByCategory(category, pageRequest);
            model.addAttribute("category", category);
        } else {
            musicPage = musicService.getAllMusic(pageRequest);
        }

        model.addAttribute("musicPage", musicPage);
        return "music";
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ARTIST')")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("title") String title,
                                             @RequestParam("artist") Long artistId,
                                             @RequestParam("genre") String genre) {
        try {
            Artist artist = artistRepository.findById(artistId).orElseThrow(() -> new RuntimeException("Artist not found with id: " + artistId));
            String fileName = fileStorageService.storeFile(file);

            Music music = new Music();
            music.setName(title);
            music.setArtist(artist);
            music.setGenre(genre);
            music.setAudioFilePath(fileName);
            music.setOriginalFileName(file.getOriginalFilename());

            musicRepository.save(music);

            return ResponseEntity.ok("Successfully uploaded file");

        }
        catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload file: " + e.getMessage());
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body("Failed to upload file: " + e.getMessage());
        }
    }

    @PostMapping("/{id}")
    public String updateMusic(@PathVariable Long id, Music music){
        musicService.updateMusic(music);
        return "redirect:/music/" + id;
    }

    @GetMapping("/delete/{id}")
    public String deleteMusic(@PathVariable Long id){
        musicService.deleteMusic(id);
        return "redirect:/music";
    }
}
