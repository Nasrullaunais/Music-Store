package com.music.musicstore.controllers;

import com.music.musicstore.services.MusicService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final MusicService musicService;

    public HomeController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        var page = musicService.getAllMusic(PageRequest.of(0, 20, Sort.by("createdAt").descending()));
        model.addAttribute("musicPage", page);
        return "music";
    }
}
