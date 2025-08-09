package com.music.musicstore.controllers;

import com.music.musicstore.services.MusicService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import java.util.Optional;

import com.music.musicstore.models.Music;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping("/list")
    public String getAllMusic(Model model){
        List<Music> musicList =  musicService.getAllMusic();
        model.addAttribute("musicList", musicList);
        return "musicList successfully loaded";
    }

    @GetMapping("/{id}")
    public String getMusicById(@PathVariable Long id, Model model){
        Optional<Music> music = musicService.getMusicById(id);
        model.addAttribute("music", music);
        return "music successfully loaded";
    }

    @GetMapping("/delete/{id}")
    public String deleteMusicById(@PathVariable Long id, RedirectAttributes redirectAttributes){
        musicService.deleteMusicById(id);
        redirectAttributes.addFlashAttribute("message", "Music successfully deleted");
        return "redirect:/music/list";
    }

}
