package com.music.musicstore.controllers;

import com.music.musicstore.models.Customer;
import com.music.musicstore.models.Music;
import com.music.musicstore.services.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;
    private static final String UPLOAD_DIR = "src/main/resources/static/musics/";

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
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

    @PostMapping("/{id}")
    public String updateMusic(@PathVariable Long id, Music music){
        musicService.updateMusic(music);
        return "redirect:/music/" + id;
    }

    @PostMapping("/upload")
    public String uploadMusic(@RequestParam("file") Music music){
        musicService.saveMusic(music);
        return "redirect:/music";
    }

    @GetMapping("/delete/{id}")
    public String deleteMusic(@PathVariable Long id){
        musicService.deleteMusic(id);
        return "redirect:/music";
    }
}
