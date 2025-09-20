package com.music.musicstore.services;

import com.music.musicstore.models.music.Music;
import com.music.musicstore.repositories.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class DataInitializationService implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    @Autowired
    private MusicRepository musicRepository;

    @Override
    public void run(String... args) {
        if (musicRepository.count() == 0) {
            logger.info("Database is empty. Initializing with sample music data...");
            initializeMusicData();
        } else {
            logger.info("Database already contains {} music records. Skipping initialization.", musicRepository.count());
        }
    }

    private void initializeMusicData() {
        List<Music> sampleMusic = Arrays.asList(
            createMusic(
                "A Thousand Greetings",
                "Motivational nasheed that uplifts the soul and connects with divine spirituality",
                new BigDecimal("9.99"),
                "muhammad_al_muqit",
                "Spiritual Collection",
                2023,
                "/uploads/music/A THOUSAND GREETINGS - MOTIVATIONAL NASHEED - MUHAMMAD AL MUQIT .mp3",
                "A THOUSAND GREETINGS - MOTIVATIONAL NASHEED - MUHAMMAD AL MUQIT .mp3",
                "https://example.com/images/thousand-greetings.jpg"
            ),

            createMusic(
                "I Rise",
                "Powerful motivational nasheed that inspires strength and perseverance through faith",
                new BigDecimal("8.99"),
                "muhammad_al_muqit",
                "Motivation Series",
                2022,
                "/uploads/music/I Rise - Motivational Nasheed - By Muhammad al Muqit - YouTube.mp3",
                "I Rise - Motivational Nasheed - By Muhammad al Muqit - YouTube.mp3",
                "https://example.com/images/i-rise.jpg"
            ),

            createMusic(
                "Qad Kafani Ilmu Rabbi",
                "Beautiful classical nasheed expressing deep spiritual knowledge and divine wisdom",
                new BigDecimal("10.99"),
                "traditional_artist",
                "Classical Collection",
                2021,
                "/uploads/music/Qad Kafani Ilmu Rabbi Official Video - YouTube.mp3",
                "Qad Kafani Ilmu Rabbi Official Video - YouTube.mp3",
                "https://example.com/images/qad-kafani.jpg"
            ),

            createMusic(
                "Shukran Laka Rabbi",
                "Gratitude nasheed expressing thankfulness and appreciation to the Creator",
                new BigDecimal("7.99"),
                "grateful_voice",
                "Gratitude Album",
                2023,
                "/uploads/music/Shukran Laka Rabbi - YouTube.mp3",
                "Shukran Laka Rabbi - YouTube.mp3",
                "https://example.com/images/shukran-laka.jpg"
            ),

            createMusic(
                "Sins Nasheed",
                "Reflective nasheed about repentance, forgiveness and spiritual purification",
                new BigDecimal("9.49"),
                "reflective_soul",
                "Spiritual Reflection",
                2022,
                "/uploads/music/Sins Nasheed - YouTube.mp3",
                "Sins Nasheed - YouTube.mp3",
                "https://example.com/images/sins-nasheed.jpg"
            ),

            createMusic(
                "Tabsirah (Slowed Reverb)",
                "Soothing slowed version of the beloved nasheed with beautiful reverb effects",
                new BigDecimal("11.99"),
                "muhammad_al_muqit",
                "Remix Collection",
                2024,
                "/uploads/music/Tabsirah تبصرة Slowed Reverb Soothing Nasheed By Muhammad Al Muq.mp3",
                "Tabsirah تبصرة Slowed Reverb Soothing Nasheed By Muhammad Al Muq.mp3",
                "https://example.com/images/tabsirah-slowed.jpg"
            ),

            createMusic(
                "Taweel Al Shawq",
                "Passionate nasheed expressing deep longing and spiritual yearning",
                new BigDecimal("8.49"),
                "yearning_heart",
                "Emotional Journey",
                2023,
                "/uploads/music/Taweel Al Shawq - YouTube.mp3",
                "Taweel Al Shawq - YouTube.mp3",
                "https://example.com/images/taweel-al-shawq.jpg"
            ),

            createMusic(
                "The Book of Allah is My Constitution",
                "Powerful nasheed declaring love and commitment to Islamic principles and values",
                new BigDecimal("12.99"),
                "muhammad_al_muqit",
                "Faith Declaration",
                2023,
                "/uploads/music/The Book Of Allah is My Constitution - Powerful Nasheed Muhammad.mp3",
                "The Book Of Allah is My Constitution - Powerful Nasheed Muhammad.mp3",
                "https://example.com/images/book-of-allah.jpg"
            )
        );

        try {
            musicRepository.saveAll(sampleMusic);
            logger.info("Successfully initialized database with {} music records", sampleMusic.size());
        } catch (Exception e) {
            logger.error("Error initializing music data", e);
            throw new RuntimeException("Failed to initialize music data", e);
        }
    }

    private Music createMusic(String name, String description, BigDecimal price,
                             String artistUsername, String albumName, Integer releaseYear,
                             String audioFilePath, String originalFileName, String imageUrl) {
        Music music = new Music();
        music.setName(name);
        music.setDescription(description);
        music.setPrice(price);
        music.setCategory("Nasheed");
        music.setArtistUsername(artistUsername);
        music.setAlbumName(albumName);
        music.setGenre("Islamic");
        music.setReleaseYear(releaseYear);
        music.setAudioFilePath(audioFilePath);
        music.setOriginalFileName(originalFileName);
        music.setImageUrl(imageUrl);
        music.setCreatedAt(LocalDateTime.now());
        music.setUpdatedAt(LocalDateTime.now());
        return music;
    }
}
